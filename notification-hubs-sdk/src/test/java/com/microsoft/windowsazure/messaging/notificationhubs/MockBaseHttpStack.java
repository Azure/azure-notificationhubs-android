package com.microsoft.windowsazure.messaging.notificationhubs;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.BaseHttpStack;
import com.android.volley.toolbox.HttpResponse;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;

public class MockBaseHttpStack extends BaseHttpStack {

    private static final int DEFAULT_DELAY_MILLISECONDS = 0;

    private final int mDelayMilliseconds;
    private final HttpResponse mResponse;
    private final IOException mIOException;
    private final AuthFailureError mAuthFailureError;

    public MockBaseHttpStack(@NotNull HttpResponse response) {
        this(response, DEFAULT_DELAY_MILLISECONDS);
    }

    public MockBaseHttpStack(@NotNull HttpResponse response, int delayMilliseconds) {
        this(response, null, null, delayMilliseconds);
    }

    public MockBaseHttpStack(@NotNull IOException e) {
        this(e, DEFAULT_DELAY_MILLISECONDS);
    }

    public MockBaseHttpStack(@NotNull IOException e, int delayMilliseonds) {
        this(null, e, null, delayMilliseonds);
    }

    public MockBaseHttpStack(@NotNull AuthFailureError e) {
        this(e, DEFAULT_DELAY_MILLISECONDS);
    }

    public MockBaseHttpStack(@NotNull AuthFailureError e, int delayMilliseconds) {
        this(null, null, e, delayMilliseconds);
    }

    private MockBaseHttpStack(HttpResponse response, IOException exception, AuthFailureError authError, int delayMilliseconds) {
        mResponse = response;
        mIOException = exception;
        mAuthFailureError = authError;
        mDelayMilliseconds = delayMilliseconds;
    }

    /**
     * Performs an HTTP request with the given parameters.
     *
     * <p>A GET request is sent if request.getPostBody() == null. A POST request is sent otherwise,
     * and the Content-Type header is set to request.getPostBodyContentType().
     *
     * @param request           the request to perform
     * @param additionalHeaders additional headers to be sent together with {@link
     *                          Request#getHeaders()}
     * @return the {@link HttpResponse}
     * @throws SocketTimeoutException if the request times out
     * @throws IOException            if another I/O error occurs during the request
     * @throws AuthFailureError       if an authentication failure occurs during the request
     */
    @Override
    public HttpResponse executeRequest(Request<?> request, Map<String, String> additionalHeaders) throws IOException, AuthFailureError {
        try {
            Thread.sleep(mDelayMilliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (mResponse != null) {
            return mResponse;
        } else if(mAuthFailureError != null) {
            throw mAuthFailureError;
        } else if (mIOException != null) {
            throw mIOException;
        } else{
            throw new UnsupportedOperationException();
        }
    }
}
