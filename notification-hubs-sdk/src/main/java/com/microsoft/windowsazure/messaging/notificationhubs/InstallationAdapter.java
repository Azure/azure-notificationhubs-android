package com.microsoft.windowsazure.messaging.notificationhubs;

/**
 * Defines the operations that must implemented in order to communicate with a backend that is
 * keeps track of registered devices.
 *
 * The default implementation of this interface is the {@link NotificationHubInstallationAdapter},
 * which will keep the Notification Hubs backend up-to-date.
 */
public interface InstallationAdapter {
    /**
     * Updates a backend with the updated Installation information for this device.
     * @param installation The record to update.
     */
    void saveInstallation(Installation installation, Listener onSuccess, ErrorListener onFailure);

    /**
     * Defines the callback that should be invoked when an Installation is successfully processed by
     * the backend.
     */
    interface Listener{
        void onInstallationSaved(Installation i);
    }

    /**
     * Defines the callback that should be invoked when an Installation fails to be processed by the
     * backend.
     */
    interface ErrorListener {
        void onInstallationSaveError(Exception e);
    }
}
