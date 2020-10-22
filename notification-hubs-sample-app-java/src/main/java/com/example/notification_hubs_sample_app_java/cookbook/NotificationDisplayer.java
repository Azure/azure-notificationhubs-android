package com.example.notification_hubs_sample_app_java.cookbook;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.notification_hubs_sample_app_java.R;
import com.microsoft.windowsazure.messaging.notificationhubs.FirebaseMessage;
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationListener;
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationMessage;

public class NotificationDisplayer implements NotificationListener {
    private int notificationId = 0;
    /**
     * Called from UI thread whenever a push notification is either clicked from the System Tray or
     * when a push is received.
     *
     * @param context A reference to the {@link Application} context that
     * @param message Notification message
     */
    @Override
    public synchronized void onPushNotificationReceived(Context context, NotificationMessage message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "MY_CHANNEL")
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentTitle(message.getTitle())
                .setContentText(message.getBody())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        createNotificationChannel(context);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId++, builder.build());
    }


    private void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel Name";// getString(R.string.channel_name);
            String description = "Channel Description";// getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("MY_CHANNEL", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
