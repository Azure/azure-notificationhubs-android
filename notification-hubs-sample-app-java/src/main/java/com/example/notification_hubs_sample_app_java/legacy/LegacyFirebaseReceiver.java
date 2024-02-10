package com.example.notification_hubs_sample_app_java.legacy;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.microsoft.windowsazure.messaging.NotificationHub;

public final class LegacyFirebaseReceiver extends FirebaseMessagingService {

    /**
     * Creates a new instance that will inform the static-global-instance of {@link NotificationHub}
     * when a new message is received.
     */
    public LegacyFirebaseReceiver() { }

    /**
     * Loads all resources that the service will need to execute.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        LegacyNotificationHubManager legacyHubManager = LegacyNotificationHubManager.getInstance();
        if (!legacyHubManager.isInitialized()) {
            legacyHubManager.initialize(this.getApplicationContext());
        }
    }

    /**
     * Callback for the system when a new RemoteMessage is received.
     * @param remoteMessage the newly acquired message.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        System.out.println("Message received");
    }

    /**
     * Callback for when Firebase assigns a new unique identifier for pushing notifications to this
     * application on this device.
     * @param s The new unique identifier for this application.
     */
    @Override
    public void onNewToken(@NonNull String s) {
        LegacyNotificationHubManager.getInstance().register(s);
    }
}