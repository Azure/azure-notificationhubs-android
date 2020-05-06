/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.windowsazure.messaging.notificationhubs.http;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.VisibleForTesting;

import java.net.UnknownHostException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.microsoft.windowsazure.messaging.notificationhubs.http.DefaultHttpClient.X_MS_RETRY_AFTER_MS_HEADER;

/**
 * Decorator managing retries.
 */
public class HttpClientRetryer extends HttpClientDecorator {

    /**
     * Retry intervals to use, array index is to use the value for each retry. When we used all the array values, we give up and forward the last error.
     */
    @VisibleForTesting
    static final long[] RETRY_INTERVALS = new long[]{
            TimeUnit.SECONDS.toMillis(10),
            TimeUnit.MINUTES.toMillis(5),
            TimeUnit.MINUTES.toMillis(20)
    };

    /**
     * Android "timer" using the main thread loop.
     */
    private final Handler mHandler;

    /**
     * Random object for interval randomness.
     */
    private final Random mRandom = new Random();

    /**
     * Init with default retry policy.
     *
     * @param decoratedApi API to decorate.
     */
    HttpClientRetryer(HttpClient decoratedApi) {
        this(decoratedApi, new Handler(Looper.getMainLooper()));
    }

    /**
     * Init.
     *
     * @param decoratedApi API to decorate.
     * @param handler      handler for timed retries.
     */
    @VisibleForTesting
    HttpClientRetryer(HttpClient decoratedApi, Handler handler) {
        super(decoratedApi);
        mHandler = handler;
    }

    @Override
    public ServiceCall callAsync(String url, String method, Map<String, String> headers, CallTemplate callTemplate, ServiceCallback serviceCallback) {

        /* Wrap the call with the retry logic and call delegate. */
        RetryableCall retryableCall = new RetryableCall(mDecoratedApi, url, method, headers, callTemplate, serviceCallback);
        retryableCall.run();
        return retryableCall;
    }

    /**
     * Retry wrapper logic.
     */
    @VisibleForTesting
    private class RetryableCall extends HttpClientCallDecorator {

        /**
         * Current retry counter. 0 means its the first try.
         */
        private int mRetryCount;

        RetryableCall(HttpClient decoratedApi, String url, String method, Map<String, String> headers, CallTemplate callTemplate, ServiceCallback serviceCallback) {
            super(decoratedApi, url, method, headers, callTemplate, serviceCallback);
        }

        @Override
        public synchronized void cancel() {
            mHandler.removeCallbacks(this);
            super.cancel();
        }

        @Override
        public void onCallFailed(Exception e) {
            if (mRetryCount < RETRY_INTERVALS.length && HttpUtils.isRecoverableError(e)) {
                long delay = 0;
                if (e instanceof HttpException) {
                    HttpException httpException = (HttpException) e;
                    String retryAfterMs = httpException.getHttpResponse().getHeaders().get(X_MS_RETRY_AFTER_MS_HEADER);
                    if (retryAfterMs != null) {
                        delay = Long.parseLong(retryAfterMs);
                    }
                }
                if (delay == 0) {
                    delay = RETRY_INTERVALS[mRetryCount++] / 2;
                    delay += mRandom.nextInt((int) delay);
                }
                String message = "Try #" + mRetryCount + " failed and will be retried in " + delay + " ms";
                if (e instanceof UnknownHostException) {
                    message += " (UnknownHostException)";
                }
                Log.w("ANH", message, e);
                mHandler.postDelayed(this, delay);
            } else {
                mServiceCallback.onCallFailed(e);
            }
        }
    }
}