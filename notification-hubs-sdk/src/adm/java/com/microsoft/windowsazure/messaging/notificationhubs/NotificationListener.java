package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;
import android.content.Intent;

public interface NotificationListener {
    /**
     * Called from UI thread whenever a push notification is either clicked from the System Tray or
     * when a push is received.
     *
     * @param context A reference to the {@link android.app.Application} for lifecycle management.
     * @param message Object delivered by the Push Notification Service.
     */
    void onPushNotificationReceived(Context context, Intent message);
}
