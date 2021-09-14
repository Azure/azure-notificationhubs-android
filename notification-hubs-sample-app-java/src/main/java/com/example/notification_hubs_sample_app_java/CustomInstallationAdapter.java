package com.example.notification_hubs_sample_app_java;

import android.content.Context;

import com.android.volley.toolbox.Volley;
import com.microsoft.windowsazure.messaging.notificationhubs.Installation;
import com.microsoft.windowsazure.messaging.notificationhubs.InstallationAdapter;
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationHubInstallationAdapter;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashSet;
import java.util.Set;

/**
 * This class wraps the base installation adapter
 */
public class CustomInstallationAdapter implements InstallationAdapter {

    private final NotificationHubInstallationAdapter baseInstallationAdapter;
    private final CustomConnectionString connectionString;
    private final String hubName;
    private final Context context;

    public CustomInstallationAdapter(Context context, String hubName, String connectionString) {
        baseInstallationAdapter = new NotificationHubInstallationAdapter(context, hubName, connectionString);
        this.context = context;
        this.connectionString = CustomConnectionString.parse(connectionString);
        this.hubName = hubName;
    }

    /**
     * Updates a backend with the updated Installation information for this device.
     *
     * @param installation            The record to update.
     * @param onInstallationSaved     Installation saved listener.
     * @param onInstallationSaveError Installation save error listener.
     */
    @Override
    public void saveInstallation(Installation installation, Listener onInstallationSaved, ErrorListener onInstallationSaveError) {
        CustomInstallationGetRequest getRequest = new CustomInstallationGetRequest(connectionString, hubName, installation, null, response -> {
            try {
                // TODO: Interpret from the Installation from the server
                JSONArray jsonTags = response.optJSONArray("tags");
                Set<String> tags = new HashSet<>();
                for (int i = 0; i < jsonTags.length(); i++) {
                    tags.add(jsonTags.getString(i));
                }

                // TODO: Update the installation
                installation.addTags(tags);
            } catch (JSONException jsonException) {
                onInstallationSaveError.onInstallationSaveError(jsonException);
                return;
            }

            // Save to the regular backend
            baseInstallationAdapter.saveInstallation(installation, onInstallationSaved, onInstallationSaveError);
        }, error -> onInstallationSaveError.onInstallationSaveError(error));

        Volley.newRequestQueue(context).add(getRequest);
    }
}
