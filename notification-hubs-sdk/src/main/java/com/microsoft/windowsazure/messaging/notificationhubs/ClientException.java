package com.microsoft.windowsazure.messaging.notificationhubs;

import com.android.volley.ClientError;

/**
 * Indicates that this device sent a malformed request to the Azure Notification Hub backend.
 */
public class ClientException extends NotificationHubException {
    ClientException(ClientError error) {
        super(error.networkResponse);
    }
}
