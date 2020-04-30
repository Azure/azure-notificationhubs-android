package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;

public interface NotificationListener {
    /**
     * Called from UI thread whenever a push notification is either clicked from the System Tray or
     * when a push is received.
     *
     * @param context A reference to the {@link android.app.Application} context that
     * @param message
     */
    void onPushNotificationReceived(Context context, INotification message);
}
