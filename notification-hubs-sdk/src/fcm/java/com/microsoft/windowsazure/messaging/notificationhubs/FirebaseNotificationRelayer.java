package com.microsoft.windowsazure.messaging.notificationhubs;

import com.google.firebase.messaging.RemoteMessage;

public class FirebaseNotificationRelayer implements NotificationRelayer{
    @Override
    public void relayNotification(Context context,RemoteMessage message, Iterable<NotificationListener> notificationListeners) {
        for (NotificationListener notificationlistener : notificationListeners) {
            notificationlistener.
        }
    }
}
