package com.microsoft.windowsazure.messaging.notificationhubs;

import android.app.Activity;

public interface NotificationListener {
    /**
     * Called from UI thread whenever a push notification is either clicked from the System Tray or
     * when a push is received.
     *
     * @param activity Current activity when the Notification is received, or clicked.
     * @param message
     */
    void onPushNotificationReceived(Activity activity, NotificationMessage message);
}
