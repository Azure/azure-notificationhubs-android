package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;
import com.google.firebase.messaging.RemoteMessage;

public interface NotificationRelayer {
    void relayNotification(Context context, RemoteMessage message, Iterable<NotificationListener> notificationListeners);
}
