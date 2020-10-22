package com.microsoft.windowsazure.messaging.notificationhubs;

import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class FirebaseMessage implements NotificationMessage {

    private final RemoteMessage mUnderlyer;

    public FirebaseMessage(RemoteMessage message) {
        mUnderlyer = message;
    }

    /**
     * Fetches the RemoteMessage that was delivered by Firebase.
     */
    public RemoteMessage getRemoteMessage() {
        return mUnderlyer;
    }

    @Override
    public String getTitle() {
        RemoteMessage.Notification notification = mUnderlyer.getNotification();
        if (notification == null) {
            return null;
        }
        return notification.getTitle();
    }

    @Override
    public String getBody() {
        RemoteMessage.Notification notification = mUnderlyer.getNotification();
        if (notification == null) {
            return null;
        }
        return notification.getBody();
    }

    @Override
    public Map<String, String> getData() {
        return mUnderlyer.getData();
    }
}
