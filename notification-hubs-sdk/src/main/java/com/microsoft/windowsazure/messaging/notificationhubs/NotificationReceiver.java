package com.microsoft.windowsazure.messaging.notificationhubs;

import androidx.annotation.NonNull;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public final class NotificationReceiver extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        NotificationHub.relayMessage(remoteMessage);
    }

    @Override
    public void onNewToken(@NonNull String s) {
        NotificationHub.setPushChannel(s);
    }
}
