package com.microsoft.windowsazure.messaging.notificationhubs;

import com.android.volley.NetworkResponse;

import java.util.Map;

/**
 * Indicates rejection by the Azure Notification Hub backend.
 */
public class NotificationHubException extends Exception {
    private final int mResponseStatusCode;
    private final byte[] mResponseBody;
    private final Map<String, String> mResponseHeaders;

    NotificationHubException(NetworkResponse networkResponse) {
        super("Azure Notification Hub request failed with status " + networkResponse.statusCode + ": " + new String(networkResponse.data));
        mResponseBody = networkResponse.data;
        mResponseStatusCode = networkResponse.statusCode;
        mResponseHeaders = networkResponse.headers;
    }

    /**
     * Gets the status code that was returned by the server.
     * @return The HTTP status code that was received from the Azure Notification Hub backend.
     */
    public int getStatusCode() {
        return mResponseStatusCode;
    }

    /**
     * Fetches the body of an error message as populated by the Azure Notification Hub backend.
     * @return Error details.
     */
    public String getResponseBody() {
        return new String(mResponseBody);
    }

    /**
     * Fetch headers sent with the error response.
     * @return HTTP Headers.
     */
    public Map<String, String> getResponseHeaders() {
        return mResponseHeaders;
    }
}
