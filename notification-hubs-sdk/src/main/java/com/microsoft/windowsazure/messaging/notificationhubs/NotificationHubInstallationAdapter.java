package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Responsible for informing Azure Notification Hubs of changes to when this device should receive
 * notifications.
 */
public class NotificationHubInstallationAdapter implements InstallationAdapter {
    private static final long DEFAULT_INSTALLATION_EXPIRATION_MILLIS = 1000L * 60L * 60L * 24L * 90L;
    private static final RetryPolicy sDoNotRetry;
    private static final Set<Integer> sRetriableStatusCodes;

    private final String mHubName;
    private final ConnectionString mConnectionString;
    private final RequestQueue mRequestQueue;
    private final long mInstallationExpirationWindow;


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
        new RequestUsher(installation, 3, onInstallationSaved, onInstallationSaveError).submit();
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

    static boolean isRetriable(VolleyError error) {
        if (error instanceof NetworkError) {
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
     * Responsible for creating
     */
    private class RequestUsher implements Response.ErrorListener, Response.Listener<JSONObject> {
        private final int mMaxRetries;
        private int mRetry;
        private final Installation mInstallation;
        private final InstallationAdapter.Listener mOnSuccess;
        private final InstallationAdapter.ErrorListener mOnFailure;

        public RequestUsher(Installation installation, int maxRetries, InstallationAdapter.Listener onSuccess, InstallationAdapter.ErrorListener onFailure) {
            mMaxRetries = maxRetries;
            mInstallation = installation;
            mOnSuccess = onSuccess;
            mOnFailure = onFailure;
            mRetry = 0;
        }

        private void submit() {
            InstallationPutRequest request = new InstallationPutRequest(
                    NotificationHubInstallationAdapter.this.mConnectionString,
                    NotificationHubInstallationAdapter.this.mHubName,
                    mInstallation,
                    this,
                    this);

            request.setRetryPolicy(sDoNotRetry);

            mRequestQueue.add(request);
        }

        /**
         * Called when a response is received.
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
                // TODO: wrap this error with ours to isolate ourselves from Volley as a dependency.
                mOnFailure.onInstallationSaveError(error);
                return;
            }

            submit();
        }
    }
}


