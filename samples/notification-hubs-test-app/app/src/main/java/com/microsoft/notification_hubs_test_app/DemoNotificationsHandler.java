package com.microsoft.notification_hubs_test_app;

import android.app.NotificationManager;
import android.content.Context;

import com.google.firebase.messaging.RemoteMessage;
import com.microsoft.windowsazure.notifications.NotificationsHandler;

public class DemoNotificationsHandler extends NotificationsHandler {
    public static final int NOTIFICATION_ID = 1;

    private NotificationManager mNotificationManager;

    @Override
    public void onReceive(Context context, RemoteMessage remoteMessage) {
        if (mNotificationManager == null) {
            mNotificationManager = context.getSystemService(NotificationManager.class);
        }

        NotificationData notificationData = new NotificationData(remoteMessage);

        mNotificationManager.notify(NOTIFICATION_ID, notificationData.createNotification(context));

        if (MainActivity.isVisible) {
            MainActivity.mainActivity.ToastNotify(notificationData.getMessage());
        }
    }
}