package com.microsoft.windowsazure.messaging.notificationhubs;

import androidx.annotation.NonNull;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public final class FirebaseReceiver extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        NotificationHub.relayMessage(remoteMessage);
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
        return new NotificationMessage(
                notification.getTitle(),
                notification.getBody(),
                remoteMessage.getData());
    }
}
