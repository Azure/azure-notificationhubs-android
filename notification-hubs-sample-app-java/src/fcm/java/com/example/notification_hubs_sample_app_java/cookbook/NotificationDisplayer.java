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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A default implementation of a NotificationListener, which renders notifications that are received
 * in the foreground the way Firebase renders them when your application is in the background.
 */
public class NotificationDisplayer implements NotificationListener {
    private static final NotificationChannel DEFAULT_NOTIFICATION_CHANNEL;

    private int mNotificationId = 0;
    private Set<String> mExistingChannelIds;
    private final String mDefaultChannelId;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Match the default channel that Firebase would create, if it was handed a notification
            // that didn't have a channel assignment.
            DEFAULT_NOTIFICATION_CHANNEL = new NotificationChannel(
                    "fcm_fallback_notification_channel",
                    "Miscellaneous",
                    NotificationManager.IMPORTANCE_DEFAULT);
        } else {
            DEFAULT_NOTIFICATION_CHANNEL = null;
        }
    }

    /**
     * Creates an instance of a {@link NotificationListener} that displays notification messages
     * when they are received.
     */
    public NotificationDisplayer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mDefaultChannelId = DEFAULT_NOTIFICATION_CHANNEL.getId();
        } else {
            mDefaultChannelId = "";
        }
    }

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
            channelId = ""; // The concept of channels weren't introduced until O, so this value will be ignored.
        } else if (channelId == null || !getExistingChannelIds(context).contains(channelId)) {
            channelId = mDefaultChannelId;
        }

        if (channelId.equals(mDefaultChannelId)) {
            assertDefaultChannelCreated(context);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentTitle(rawNotification.getTitle())
                .setContentText(rawNotification.getBody())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(mNotificationId++, builder.build());
    }

    /**
     * Fetch the IDs of the notification categories currently associated with this app.
     * @param context The application context for the currently running application.
     * @return A distinct set of channel IDs.
     */
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

    /**
     * Creates the Miscellaneous channel, if it does not already exist.
     * @param context The application context for the currently running application.
     */
    private void assertDefaultChannelCreated(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Set<String> existingChannelIds = getExistingChannelIds(context);

            if (existingChannelIds.contains(DEFAULT_NOTIFICATION_CHANNEL.getId())){
                return;
            }

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager == null) {
                return;
            }
            notificationManager.createNotificationChannel(DEFAULT_NOTIFICATION_CHANNEL);
        }
    }
}
