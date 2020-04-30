package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;

import com.android.volley.Header;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.NoCache;

import java.util.List;

public class FakeRequestQueue extends RequestQueue {
    public FakeRequestQueue(Context context, int responseStatusCode, List<Header> responseHeaders) {
        super(new NoCache(), new BasicNetwork(new FakeHttpStack(context, responseStatusCode, responseHeaders)));
    }
}