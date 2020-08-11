package com.microsoft.windowsazure.messaging.notificationhubs;

import com.android.volley.AuthFailureError;

/**
 * Indicates that invalid credentials were presented to the Azure Notification Hub backend.
 */
public class AuthorizationException extends NotificationHubException {
    AuthorizationException(AuthFailureError error) {
        super(error.networkResponse);
    }
}
