package com.microsoft.windowsazure.messaging.notificationhubs;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public final class FirebaseReceiver extends FirebaseMessagingService {

    @Override
    public void onCreate() {
        super.onCreate();


        if (NotificationHub.getPushChannel() == null) {
            FirebaseInstanceId.getInstance()
                    .getInstanceId()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()){
                            Log.e("ANH", "unable to fetch FirebaseInstanceId");
                            return;
                        }
                        NotificationHub.setPushChannel(task.getResult().getToken());
                    });
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        NotificationHub.relayMessage(getNotificationMessage(remoteMessage));
    }

    @Override
    public void onNewToken(@NonNull String s) {
        NotificationHub.setPushChannel(s);
    }

    /**
     * Converts from a {@link RemoteMessage} to a {@link NotificationMessage}.
     * @param remoteMessage The message intended for this device, as delivered by Firebase.
     * @return A fully instantiated {@link NotificationMessage}.
     */
    static NotificationMessage getNotificationMessage(RemoteMessage remoteMessage) {
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        String title = null;
        String body = null;
        if (notification != null) {
            title = notification.getTitle();
            body = notification.getBody();
        }
        return new NotificationMessage(
                title,
                body,
                remoteMessage.getData());
    }
}
