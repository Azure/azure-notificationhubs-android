package com.microsoft.notificationhubs.fcmtutorialapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.microsoft.windowsazure.messaging.NotificationHub;
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationListener;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyFirebaseService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseService";

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
    public MyFirebaseService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mDefaultChannelId = DEFAULT_NOTIFICATION_CHANNEL.getId();
        } else {
            mDefaultChannelId = "";
        }
    }

    @Override
    public void onNewToken(@NonNull String fcmToken) {

        super.onNewToken(fcmToken);
        Log.d(TAG, "Refreshing FCM Registration Token");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String resultString;
        String regID;
        String storedToken = sharedPreferences.getString("FCMtoken", "");

        try {
            Log.d(TAG, "FCM Registration Token: " + fcmToken);

            // Storing the registration ID that indicates whether the generated token has been
            // sent to your server. If it is not stored, send the token to your server,
            // otherwise your server should have already received the token.
            if (((regID=sharedPreferences.getString("registrationID", null)) == null)){

                NotificationHub hub = new NotificationHub(NotificationSettings.HubName,
                        NotificationSettings.HubListenConnectionString, this);
                Log.d(TAG, "Attempting a new registration with NH using FCM token : " + fcmToken);
                regID = hub.register(fcmToken).getRegistrationId();

                // If you want to use tags...
                // Refer to : https://azure.microsoft.com/documentation/articles/notification-hubs-routing-tag-expressions/
                // regID = hub.register(token, "tag1,tag2").getRegistrationId();

                resultString = "New NH Registration Successfully - RegId : " + regID;
                Log.d(TAG, resultString);

                sharedPreferences.edit().putString("registrationID", regID ).apply();
                sharedPreferences.edit().putString("FCMtoken", fcmToken).apply();
            }

            // Check if the token may have been compromised and needs refreshing.
            else if (!(storedToken).equals(fcmToken)) {

                NotificationHub hub = new NotificationHub(NotificationSettings.HubName,
                        NotificationSettings.HubListenConnectionString, this);
                Log.d(TAG, "NH Registration refreshing with token : " + fcmToken);
                regID = hub.register(fcmToken).getRegistrationId();

                // If you want to use tags...
                // Refer to : https://azure.microsoft.com/documentation/articles/notification-hubs-routing-tag-expressions/
                // regID = hub.register(token, "tag1,tag2").getRegistrationId();

                resultString = "New NH Registration Successfully - RegId : " + regID;
                Log.d(TAG, resultString);

                sharedPreferences.edit().putString("registrationID", regID).apply();
                sharedPreferences.edit().putString("FCMtoken", fcmToken).apply();
            }

            else {
                resultString = "Previously Registered Successfully - RegId : " + regID;
            }
        } catch (Exception e) {
            Log.e(TAG, resultString="Failed to complete registration", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
        }

        // Notify UI that registration has completed.
        if (MainActivity.isVisible) {
            MainActivity.mainActivity.ToastNotify(resultString);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Context context = this.getApplicationContext();

        RemoteMessage.Notification rawNotification = remoteMessage.getNotification();

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

            if (existingChannelIds.contains(DEFAULT_NOTIFICATION_CHANNEL.getId())) {
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