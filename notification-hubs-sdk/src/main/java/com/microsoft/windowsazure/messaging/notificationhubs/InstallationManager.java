package com.microsoft.windowsazure.messaging.notificationhubs;

import com.microsoft.windowsazure.messaging.notificationhubs.async.NotificationHubFuture;

public interface InstallationManager {
    /**
     * Updates a backend with the updated Installation information for this device.
     * @param installation The record to update.
     * @return A future, with the Installation ID as the value.
     */
    NotificationHubFuture<String> saveInstallation(Installation installation);
}
