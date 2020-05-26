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
    void saveInstallation(Installation installation, SaveListener onInstallationSaved, ErrorListener onInstallationSaveError);

    /**
     * Updates a backend to remove the references to the specified {@link Installation}.
     * @param id The unique identifier associated with the {@link Installation} to be removed.
     * @param onInstallationDeleted A callback which will be invoked if the {@link Installation} is
     *                              successfully deleted.
     * @param onInstallationDeleteError A callback which will be invoked if the {@link Installation}
     *                                  was not able to be deleted.
     */
    void deleteInstallation(String id, DeleteListener onInstallationDeleted, ErrorListener onInstallationDeleteError);

    /**
     * Defines the callback that should be invoked when an {@link Installation} is successfully
     * saved by the backend.
     */
    interface SaveListener {
        /**
         * Invoked when an {@link Installation} is saved.
         * @param i The record that is now saved on the backend.
         */
        void onInstallationSaved(Installation i);
    }

    /**
     * Defines the callback that should be invoked when an {@link Installation} is successfully
     * deleted by the backend.
     */
    interface DeleteListener {
        /**
         * Invoked when an {@link Installation} is deleted.
         * @param id The id of the record that was just deleted.
         */
        void onInstallationDeleted(String id);
    }

    /**
     * Defines the callback that should be invoked when an Installation fails to be processed by the
     * backend.
     */
    interface ErrorListener {
        void onInstallationOperationError(Exception e);
    }
}
