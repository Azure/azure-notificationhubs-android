package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Header;
import com.android.volley.Request;
import com.android.volley.toolbox.BaseHttpStack;
import com.android.volley.toolbox.HttpResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

class FakeHttpStack extends BaseHttpStack {

    private static final int SIMULATED_DELAY_MS = 500;
    private final Context context;
    private final List<Header> responseHeaders;
    private int responseStatusCode;

    FakeHttpStack(Context context, int responseStatusCode, List<Header> responseHeaders) {
        this.context = context;
        this.responseStatusCode = responseStatusCode;
        this.responseHeaders = responseHeaders;
    }

    @Override
    public HttpResponse executeRequest(Request<?> request, Map<String, String> stringStringMap)
            throws IOException, AuthFailureError {
        try {
            Thread.sleep(SIMULATED_DELAY_MS);
        } catch (InterruptedException e) {
        }

        HttpResponse response
                = new HttpResponse(responseStatusCode, responseHeaders);
        return response;
    }
}