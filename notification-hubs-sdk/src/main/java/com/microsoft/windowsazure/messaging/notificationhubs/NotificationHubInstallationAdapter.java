package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.ClientError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Header;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Responsible for informing Azure Notification Hubs of changes to when this device should receive
 * notifications.
 */
public class NotificationHubInstallationAdapter implements InstallationAdapter {
    private static final long DEFAULT_INSTALLATION_EXPIRATION_MILLIS = 1000L * 60L * 60L * 24L * 90L;
    private static final RetryPolicy sDoNotRetry;
    private static final Set<Integer> sRetriableStatusCodes;
    private static final String INSTALLATION_PUT_TAG = "installationPutRequest";

    private final String mHubName;
    private final ConnectionString mConnectionString;
    private final RequestQueue mRequestQueue;
    private final long mInstallationExpirationWindow;
    private final ScheduledExecutorService mScheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> mOutstandingRetry;


    public NotificationHubInstallationAdapter(Context context, String hubName, String connectionString) {
        this(context, hubName, connectionString, DEFAULT_INSTALLATION_EXPIRATION_MILLIS);
    }

    NotificationHubInstallationAdapter(Context context, String hubName, String connectionString, long installationExpirationWindow) {
        mHubName = hubName;
        mConnectionString = ConnectionString.parse(connectionString);
        mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
        mInstallationExpirationWindow = installationExpirationWindow;
    }

    static {
        sDoNotRetry = new DefaultRetryPolicy(1000, 0, 1);
        sRetriableStatusCodes = new HashSet<Integer>();
        sRetriableStatusCodes.add(500); // Internal Server Error
        sRetriableStatusCodes.add(503); // Service Unavailable
        sRetriableStatusCodes.add(504); // Gateway Timeout
        sRetriableStatusCodes.add(403); // Forbidden (legacy throttling code)
        sRetriableStatusCodes.add(408); // Client Timeout
        sRetriableStatusCodes.add(429); // Too Many Requests
    }

    /**
     * Updates a backend with the updated Installation information for this device.
     *
     * @param installation The record to update.
     */
    @Override
    public void saveInstallation(final Installation installation, final Listener onInstallationSaved, final ErrorListener onInstallationSaveError) {
        addExpiration(installation);
        cancelOutstandingUpdates();
        new RetrySession(installation, 3, onInstallationSaved, onInstallationSaveError).submit();
    }

    /**
     * Ensures that the provided {@link Installation} has an expiration.
     * @param target The instance of {@link Installation} which may not have an expiration.
     */
    void addExpiration(Installation target) {
        if (target.getExpiration() != null) {
            return;
        }

        Date expiration = new Date();
        expiration = new Date(expiration.getTime() + mInstallationExpirationWindow);

        target.setExpiration(expiration);
    }

    void cancelOutstandingUpdates() {
        synchronized (NotificationHubInstallationAdapter.this) {
            if (mOutstandingRetry != null) {
                mOutstandingRetry.cancel(true);
            }
            mRequestQueue.cancelAll(INSTALLATION_PUT_TAG);
        }
    }

    static boolean isRetriable(VolleyError error) {
        if (error instanceof NetworkError || error instanceof TimeoutError) {
            return true;
        }

        if (error.networkResponse != null ){
            if(sRetriableStatusCodes.contains(error.networkResponse.statusCode)){
                return true;
            }
        }

        return false;
    }

    /**
     * Generates InstallationPutRequests, and continually submits them serially to the Volley
     * RequestQueue until either a successful response is received, or a set number of retries has
     * elapsed.
     */
    private class RetrySession implements Response.ErrorListener, Response.Listener<JSONObject> {
        private final int mMaxRetries;
        private int mRetry;
        private final Installation mInstallation;
        private final InstallationAdapter.Listener mOnSuccess;
        private final InstallationAdapter.ErrorListener mOnFailure;
        private final long mDefaultWaitTime;

        public RetrySession(Installation installation, int maxRetries, InstallationAdapter.Listener onSuccess, InstallationAdapter.ErrorListener onFailure) {
            mMaxRetries = maxRetries;
            mInstallation = installation;
            mOnSuccess = onSuccess;
            mOnFailure = onFailure;
            mRetry = 0;
            mDefaultWaitTime = 1000;
        }

        /**
         * Creates a new InstallationPutRequest, adds it the the RequestQueue.
         */
        private void submit() {
            InstallationPutRequest request = new InstallationPutRequest(
                    NotificationHubInstallationAdapter.this.mConnectionString,
                    NotificationHubInstallationAdapter.this.mHubName,
                    mInstallation,
                    this,
                    this);
            request.addMarker(INSTALLATION_PUT_TAG);
            request.setRetryPolicy(sDoNotRetry);
            synchronized (NotificationHubInstallationAdapter.this) {
                mOutstandingRetry = null;
                mRequestQueue.add(request);
            }
        }

        /**
         * Called when a successful response is received.
         *
         * @param response The JSON Object returned by the server (if any).
         */
        @Override
        public void onResponse(JSONObject response) {
            mOnSuccess.onInstallationSaved(mInstallation);
        }

        /**
         * Callback method that an error has been occurred with the provided error code and optional
         * user-readable message.
         *
         * @param error The reason the Installation was not saved with the backend.
         */
        @Override
        public void onErrorResponse(VolleyError error) {
            mRetry++;
            if(!isRetriable(error) || mRetry > mMaxRetries) {
                mOnFailure.onInstallationSaveError(convertVolleyException(error));
                return;
            }

            long waitTimeMillis;
            NetworkResponse response = error.networkResponse;
            String rawRetryAfter = null;

            if (response != null) {
                rawRetryAfter = getRetryAfter(response);
            }

            if (response == null) {
                waitTimeMillis = mDefaultWaitTime;
            } else if(rawRetryAfter != null) {
                waitTimeMillis = parseRetryAfterValue(rawRetryAfter);
            } else if (response.statusCode == 429 || response.statusCode == 403) {
                waitTimeMillis = 10 * 1000;
            } else {
                waitTimeMillis = mDefaultWaitTime;
            }
            synchronized (NotificationHubInstallationAdapter.this) {
                mOutstandingRetry = mScheduler.schedule(new Runnable() {
                    @Override
                    public void run() {
                        submit();
                    }
                }, waitTimeMillis, TimeUnit.MILLISECONDS);
            }
        }
    }

    /**
     * In order to shield customers from potential breaking changes if we were to move away from
     * Volley, or if Volley were to introduce a breaking change, we wrap exceptions in our own
     * types.
     *
     * @param error The problem encountered by Volley.
     * @return An exception safe to hand to application code.
     */
    static Exception convertVolleyException(VolleyError error) {
        if (error instanceof AuthFailureError) {
            return new AuthorizationException((AuthFailureError)error);
        } else if (error instanceof ClientError) {
            return new ClientException((ClientError) error);
        } else if (error instanceof ServerError) {
            return new ServerException((ServerError) error);
        } else if (error instanceof NetworkError) {
                return new IOException(error.getMessage(), error.getCause());
        } else if(error instanceof ParseError) {
            return new IOException(error.getMessage(), error.getCause());
        } else if (error instanceof TimeoutError) {
            return new IOException(error.getMessage(), error.getCause());
        }
        return new Exception(error);
    }

    /**
     * Fetches the value sent as the "Retry-After" value.
     * @param response The response the server sent, which may or may not include a Retry-After header.
     * @return The raw value returned as a Retry-After header. If the header is not present, null is returned.
     */
    static String getRetryAfter(NetworkResponse response){
        for(Header header : response.allHeaders) {
            String name = header.getName();
            if (name.equalsIgnoreCase("Retry-After")) {
                return header.getValue();
            }
        }
        return null;
    }

    /**
     * Fetches the number of milliseconds, if any, that we were told to wait by the server.
     * @param retryAfter The value of a Retry-After header.
     * @return The number of milliseconds to wait. If there is no Retry-After header, a negative
     * value is returned.
     * @throws UnsupportedOperationException If Retry-After is provided in an unrecognized format.
     */
    static long parseRetryAfterValue(String retryAfter) {
        // TODO: support parsing DateTime passed in Retry-After header.

        try {
            return 1000 * Long.parseLong(retryAfter);
        } catch (NumberFormatException e) {
            throw new UnsupportedOperationException("Retry-After must be communicated as a number of seconds", e);
        }
    }
}


