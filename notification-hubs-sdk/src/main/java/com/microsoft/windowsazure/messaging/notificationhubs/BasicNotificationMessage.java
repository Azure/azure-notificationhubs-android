package com.microsoft.windowsazure.messaging.notificationhubs;

import androidx.annotation.NonNull;

import java.util.Map;

/**
 * A basic implementation of NotificationMessage.
 */
class BasicNotificationMessage implements NotificationMessage {

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

    /**
     * Instantiates a basic notification.
     * @param title The primary text associated with this notification.
     * @param message The secondary text associated with this notification.
     * @param data The key/value pairs of data associated with this notification.
     */
    public BasicNotificationMessage(String title, String message, @NonNull Map<String, String> data) {
        mTitle = title;
        mMessage = message;
        mCustomData = data;
    }

    /**
     * Fetches the primary text associated with a Notification.
     *
     * @return The title of the notification.
     */
    @Override
    public String getTitle() {
        return mTitle;
    }

    /**
     * Fetches the secondary text associated with a notification.
     *
     * @return The body of the notification.
     */
    @Override
    public String getBody() {
        return mMessage;
    }

    /**
     * Key/value pairs associated with the notification.
     *
     * @return Fields associated with this notification.
     */
    @Override
    public Map<String, String> getData() {
        return mCustomData;
    }
}
