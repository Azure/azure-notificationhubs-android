package com.microsoft.windowsazure.messaging.notificationhubs;

import com.microsoft.windowsazure.messaging.notificationhubs.async.NotificationHubFuture;

public interface InstallationManager {
    NotificationHubFuture<String> saveInstallation(Installation installation);
}
