package com.microsoft.windowsazure.messaging.notificationhubs;

import com.android.volley.ServerError;

/**
 * Indicates that Azure Notification Hub was unavailable, encountered an internal server error,
 * or something else indicating
 */
public class ServerException extends NotificationHubException {
    ServerException(ServerError error) {
        super(error.networkResponse);
    }
}
