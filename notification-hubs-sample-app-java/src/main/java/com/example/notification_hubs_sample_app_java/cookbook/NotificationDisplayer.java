package com.example.notification_hubs_sample_app_java.cookbook;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.notification_hubs_sample_app_java.R;
import com.google.firebase.messaging.RemoteMessage;
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationListener;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotificationDisplayer implements NotificationListener {
    private int mNotificationId = 0;

    private Set<String> mExistingChannelIds;

    /**
     * Called from UI thread whenever a push notification is either clicked from the System Tray or
     * when a push is received.
     *
     * @param context A reference to the {@link Application} context.
     * @param message Notification message
     */
    @Override
    public synchronized void onPushNotificationReceived(Context context, RemoteMessage message) {
        RemoteMessage.Notification rawNotification = message.getNotification();

        if (rawNotification == null) {
            // Don't render data messages.
            return;
        }

        String channelId = rawNotification.getChannelId();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            channelId = "other"; // The concept of channels weren't introduced until O, so this value will be ignored.
        } else if (channelId == null || !getExistingChannelIds(context).contains(channelId)) {
            channelId = NotificationChannel.DEFAULT_CHANNEL_ID;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentTitle(rawNotification.getTitle())
                .setContentText(rawNotification.getBody())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        createNotificationChannel(context);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(mNotificationId++, builder.build());
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

    private Set<String> getExistingChannelIds(Context context) {
        if (mExistingChannelIds != null) {
            return mExistingChannelIds;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if(notificationManager == null) {
                mExistingChannelIds = Collections.emptySet();
            } else {
                mExistingChannelIds = new HashSet<>();
                List<NotificationChannel> existingChannels = notificationManager.getNotificationChannels();
                for (NotificationChannel channel: existingChannels) {
                    mExistingChannelIds.add(channel.getId());
                }
            }
        } else {
            mExistingChannelIds = Collections.emptySet();
        }

        return mExistingChannelIds;
    }
}
