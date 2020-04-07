package com.microsoft.windowsazure.messaging.notificationhubs;

import com.microsoft.windowsazure.messaging.notificationhubs.async.NotificationHubConsumer;
import com.microsoft.windowsazure.messaging.notificationhubs.async.NotificationHubFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class NoopInstallationManager implements InstallationManager {
    @Override
    public NotificationHubFuture<String> saveInstallation(Installation installation) {
       // TODO
        return null;
    }
}
