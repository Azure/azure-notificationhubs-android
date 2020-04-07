package com.microsoft.windowsazure.messaging.notificationhubs.async;

public interface NotificationHubConsumer<T> {
    void accept(T t);
}
