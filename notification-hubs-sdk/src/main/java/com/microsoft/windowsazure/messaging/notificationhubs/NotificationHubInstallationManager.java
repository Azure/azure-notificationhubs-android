package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

/**
 * Responsible for informing Azure Notification Hubs of changes to when this device should receive
 * notifications.
 */
class NotificationHubInstallationManager implements InstallationManager {

    private final String mHubName;
    private final ConnectionString mConnectionString;

    public NotificationHubInstallationManager(String hubName, String connectionString) {
        mHubName = hubName;
        mConnectionString = ConnectionString.parse(connectionString);
    }

    /**
     * Updates a backend with the updated Installation information for this device.
     *
     * @param installation The record to update.
     * @return A future, with the Installation ID as the value.
     */
    @Override
    public void saveInstallation(Context context, Installation installation) {
        RequestQueue queue = Volley.newRequestQueue(context.getApplicationContext());
        // TODO: Build a PUT Installation Request, and fire it off to the Notification Hubs Backend.
    }


}
