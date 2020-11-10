package com.microsoft.windowsazure.messaging.notificationhubs;

import java.util.Map;

/**
 * Wraps the notifications that may be delivered via any channel.
 */
public interface NotificationMessage {
    /**
     * Fetches the primary text associated with a Notification.
     * @return The title of the notification.
     */
    String getTitle();

    /**
     * Fetches the secondary text associated with a notification.
     * @return The body of the notification.
     */
    String getBody();

    /**
     * Key/value pairs associated with the notification.
     * @return Fields associated with this notification.
     */
    Map<String, String> getData();
}
