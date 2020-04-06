package com.microsoft.windowsazure.messaging.notificationhubs;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class NotificationMessage {

    /**
     * Notification title. Will be null when Notification is received from click in system tray.
     */
    private final String mTitle;

    /**
     * Notification message. Will be null when Notification is received from click in system tray.
     */
    private final String mMessage;

    /**
     * Key/value pairs sent with a Push Notification.
     */
    private final Map<String, String> mCustomData;

    public NotificationMessage(String title, String message, @NonNull Map<String, String> customData) {
        mTitle = title;
        mMessage = message;
        mCustomData = customData;
    }

    public NotificationMessage(RemoteMessage message) {
        RemoteMessage.Notification notification = message.getNotification();
        mTitle = notification.getTitle();
        mMessage = notification.getBody();

        mCustomData = message.getData();
    }

    /**
     * @return
     */
    public String getTitle() {
        return mTitle;
    }

    public String getMessage() {
        return mMessage;
    }

    /**
     * Get the data fields that were sent with this message.
     * @return A collection of key/value pairs that were sent with this message. Can be empty, but not null.
     */
    public Map<String, String> getCustomData(){
        return mCustomData;
    }
}
