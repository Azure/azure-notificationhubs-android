/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.windowsazure.messaging.notificationhubs;

import android.net.TrafficStats;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.HttpsURLConnection;

import static com.microsoft.windowsazure.messaging.notificationhubs.DefaultHttpClient.CHARSET_NAME;
import static com.microsoft.windowsazure.messaging.notificationhubs.DefaultHttpClient.CONTENT_ENCODING_KEY;
import static com.microsoft.windowsazure.messaging.notificationhubs.DefaultHttpClient.CONTENT_ENCODING_VALUE;
import static com.microsoft.windowsazure.messaging.notificationhubs.DefaultHttpClient.CONTENT_TYPE_KEY;
import static com.microsoft.windowsazure.messaging.notificationhubs.DefaultHttpClient.CONTENT_TYPE_VALUE;
import static com.microsoft.windowsazure.messaging.notificationhubs.DefaultHttpClient.METHOD_POST;
import static com.microsoft.windowsazure.messaging.notificationhubs.DefaultHttpClient.METHOD_PUT;
import static com.microsoft.windowsazure.messaging.notificationhubs.HttpUtils.READ_BUFFER_SIZE;
import static com.microsoft.windowsazure.messaging.notificationhubs.HttpUtils.THREAD_STATS_TAG;
import static com.microsoft.windowsazure.messaging.notificationhubs.HttpUtils.WRITE_BUFFER_SIZE;
import static com.microsoft.windowsazure.messaging.notificationhubs.HttpUtils.createHttpsConnection;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Async task for default HTTP client.
 */
class DefaultHttpClientCallTask extends AsyncTask<Void, Void, Object> {

    /**
     * Default string builder capacity.
     */
    private static final int DEFAULT_STRING_BUILDER_CAPACITY = 16;

    /**
     * Minimum payload length in bytes to use gzip.
     */
    private static final int MIN_GZIP_LENGTH = 1400;

    private final String mUrl;

    private final String mMethod;

    private final Map<String, String> mHeaders;

    private final HttpClient.CallTemplate mCallTemplate;

    private final ServiceCallback mServiceCallback;

    private final Tracker mTracker;

    private final boolean mCompressionEnabled;

    DefaultHttpClientCallTask(String url, String method, Map<String, String> headers, HttpClient.CallTemplate callTemplate, ServiceCallback serviceCallback, Tracker tracker, boolean compressionEnabled) {
        mUrl = url;
        mMethod = method;
        mHeaders = headers;
        mCallTemplate = callTemplate;
        mServiceCallback = serviceCallback;
        mTracker = tracker;
        mCompressionEnabled = compressionEnabled;
    }

    private static InputStream getInputStream(HttpsURLConnection httpsURLConnection) throws IOException {
        int status = httpsURLConnection.getResponseCode();
        if (status >= 200 && status < 400) {
            return httpsURLConnection.getInputStream();
        } else {
            return httpsURLConnection.getErrorStream();
        }
    }

    /**
     * Write payload to output stream.
     */
    private void writePayload(OutputStream out, byte[] payload) throws IOException {
        for (int i = 0; i < payload.length; i += WRITE_BUFFER_SIZE) {
            out.write(payload, i, min(payload.length - i, WRITE_BUFFER_SIZE));
            if (isCancelled()) {
                break;
            }
        }
    }

    /**
     * Dump response stream to a string.
     */
    private String readResponse(HttpsURLConnection httpsURLConnection) throws IOException {

        /*
         * Though content length header value is less than actual payload length (gzip), we want to init
         * buffer with a reasonable start size to optimize (default is 16 and is way too low for this
         * use case).
         */
        StringBuilder builder = new StringBuilder(max(httpsURLConnection.getContentLength(), DEFAULT_STRING_BUILDER_CAPACITY));
        InputStream stream = getInputStream(httpsURLConnection);

        //noinspection TryFinallyCanBeTryWithResources
        try {
            Reader reader = new InputStreamReader(stream, CHARSET_NAME);
            char[] buffer = new char[READ_BUFFER_SIZE];
            int len;
            while ((len = reader.read(buffer)) > 0) {
                builder.append(buffer, 0, len);
                if (isCancelled()) {
                    break;
                }
            }
            return builder.toString();
        } finally {
            stream.close();
        }
    }

    /**
     * Do http call.
     */
    private HttpResponse doHttpCall() throws Exception {
        URL url = new URL(mUrl);
        HttpsURLConnection httpsURLConnection = createHttpsConnection(url);
        try {

            /* Build payload now if POST. */
            httpsURLConnection.setRequestMethod(mMethod);
            String payload = null;
            byte[] binaryPayload = null;
            boolean shouldCompress = false;
            boolean isPostOrPut = mMethod.equals(METHOD_POST) || mMethod.equals(METHOD_PUT);
            if (isPostOrPut && mCallTemplate != null) {

                /* Get bytes, check if large enough to compress. */
                payload = mCallTemplate.buildRequestBody();
                binaryPayload = payload.getBytes(CHARSET_NAME);
                shouldCompress = mCompressionEnabled && binaryPayload.length >= MIN_GZIP_LENGTH;

                /* If no content type specified, assume json. */
                if (!mHeaders.containsKey(CONTENT_TYPE_KEY)) {
                    mHeaders.put(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE);
                }
            }

            /* If about to compress, add corresponding header. */
            if (shouldCompress) {
                mHeaders.put(CONTENT_ENCODING_KEY, CONTENT_ENCODING_VALUE);
            }

            /* Send headers. */
            for (Map.Entry<String, String> header : mHeaders.entrySet()) {
                httpsURLConnection.setRequestProperty(header.getKey(), header.getValue());
            }
            if (isCancelled()) {
                return null;
            }

            /* Call back before the payload is sent. */
            if (mCallTemplate != null) {
                mCallTemplate.onBeforeCalling(url, mHeaders);
            }

            /* Send payload. */
            if (binaryPayload != null) {
                /* Compress payload if large enough to be worth it. */
                if (shouldCompress) {
                    ByteArrayOutputStream gzipBuffer = new ByteArrayOutputStream(binaryPayload.length);
                    GZIPOutputStream gzipStream = new GZIPOutputStream(gzipBuffer);
                    gzipStream.write(binaryPayload);
                    gzipStream.close();
                    binaryPayload = gzipBuffer.toByteArray();
                }

                /* Send payload on the wire. */
                httpsURLConnection.setDoOutput(true);
                httpsURLConnection.setFixedLengthStreamingMode(binaryPayload.length);
                OutputStream out = httpsURLConnection.getOutputStream();

                //noinspection TryFinallyCanBeTryWithResources
                try {
                    writePayload(out, binaryPayload);
                } finally {
                    out.close();
                }
            }
            if (isCancelled()) {
                return null;
            }

            /* Read response. */
            int status = httpsURLConnection.getResponseCode();
            String response = readResponse(httpsURLConnection);
            Map<String, String> responseHeaders = new HashMap<>();
            for (Map.Entry<String, List<String>> header : httpsURLConnection.getHeaderFields().entrySet()) {
                responseHeaders.put(header.getKey(), header.getValue().iterator().next());
            }
            HttpResponse httpResponse = new HttpResponse(status, response, responseHeaders);

            /* Accept all 2xx codes. */
            if (status >= 200 && status < 300) {
                return httpResponse;
            }

            /* Generate exception on failure. */
            throw new HttpException(httpResponse);
        } finally {

            /* Release connection. */
            httpsURLConnection.disconnect();
        }
    }

    @Override
    protected Object doInBackground(Void... params) {

        /* Do tag socket to avoid strict mode issue. */
        TrafficStats.setThreadStatsTag(THREAD_STATS_TAG);
        try {
            return doHttpCall();
        } catch (Exception e) {
            return e;
        } finally {
            TrafficStats.clearThreadStatsTag();
        }
    }

    @Override
    protected void onPreExecute() {
        mTracker.onStart(this);
    }

    @Override
    protected void onPostExecute(Object result) {
        mTracker.onFinish(this);
        if (result instanceof Exception) {
            mServiceCallback.onCallFailed((Exception) result);
        } else {
            HttpResponse response = (HttpResponse) result;
            mServiceCallback.onCallSucceeded(response);
        }
    }

    @Override
    protected void onCancelled(Object result) {

        /* Handle the result even if it was cancelled. */
        if (result instanceof HttpResponse || result instanceof HttpException) {
            onPostExecute(result);
        } else {
            mTracker.onFinish(this);
        }
    }

    /**
     * The callback used for maintain ongoing call tasks.
     */
    interface Tracker {

        /**
         * Called before the http call operation.
         *
         * @param task The http call.
         */
        void onStart(DefaultHttpClientCallTask task);

        /**
         * Called after the http call operation.
         *
         * @param task The http call.
         */
        void onFinish(DefaultHttpClientCallTask task);
    }
}