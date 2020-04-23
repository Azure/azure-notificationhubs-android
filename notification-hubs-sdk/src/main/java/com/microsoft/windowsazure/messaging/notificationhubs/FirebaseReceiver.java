package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.microsoft.windowsazure.messaging.R;

public final class FirebaseReceiver extends FirebaseMessagingService {

    private static final String DEVICE_TOKEN_KEY = "DeviceToken";

    private SharedPreferences mFirebasePreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        final String preferencesFile = getString(R.string.firebase_preference_file_key);
        mFirebasePreferences = this.getSharedPreferences(
                preferencesFile,
                Context.MODE_PRIVATE);

        if (!mFirebasePreferences.contains(DEVICE_TOKEN_KEY)) {
            FirebaseInstanceId.getInstance()
                    .getInstanceId()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()){
                            Log.e("ANH", "unable to fetch FirebaseInstanceId");
                            return;
                        }
                        FirebaseReceiver.this.setDeviceToken(task.getResult().getToken());
                    });
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        NotificationHub.relayMessage(remoteMessage);
    }

    @Override
    public void onNewToken(@NonNull String s) {
        setDeviceToken(s);
    }

    /**
     * Converts from a {@link RemoteMessage} to a {@link NotificationMessage}.
     * @param remoteMessage The message intended for this device, as delivered by Firebase.
     * @return A fully instantiated {@link NotificationMessage}.
     */
    static NotificationMessage getNotificationMessage(RemoteMessage remoteMessage) {
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        return new NotificationMessage(
                notification.getTitle(),
                notification.getBody(),
                remoteMessage.getData());
    }

    public void setDeviceToken(String token){
        NotificationHub.setPushChannel(token);
        mFirebasePreferences.edit().putString(DEVICE_TOKEN_KEY, token);
    }

    /**
     * Fetches the current Push Channel.
     * @return The current string that identifies this device as Push notification receiver. Null if
     *         it hasn't been initialized yet.
     */
    public String getDeviceToken() {
        String retval = NotificationHub.getPushChannel();
        if (retval == null) {
            retval = mFirebasePreferences.getString(DEVICE_TOKEN_KEY, null);
        }
        return retval;
    }
}
