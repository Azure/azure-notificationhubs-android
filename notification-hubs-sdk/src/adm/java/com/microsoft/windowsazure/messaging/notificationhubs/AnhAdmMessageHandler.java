package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Intent;
import android.util.Log;

import com.amazon.device.messaging.ADMMessageHandlerBase;

public final class AnhAdmMessageHandler extends ADMMessageHandlerBase {

    private final NotificationHub mHub;

    public AnhAdmMessageHandler()
    {
        this(NotificationHub.getInstance());
    }

    public AnhAdmMessageHandler(NotificationHub hub)
    {
        super(AnhAdmMessageHandler.class.getName());
        mHub = hub;
    }

    @Override
    protected void onMessage(final Intent intent) {
        Log.i("ANH", "message received from ADM");
        mHub.getInstanceListener().onPushNotificationReceived(mHub.getApplication(), intent);
    }

    @Override
    protected void onRegistrationError(final String errMessage) {
        Log.e("ANH", "ADM registration error: " + errMessage);
    }

    @Override
    protected void onRegistered(final String registrationId) {
        Log.i("ANH", "registered for notifications with ADM");
    }

    @Override
    protected void onUnregistered(final String s) {
        Log.i("ANH", "ADM registration ID invalidated");
    }
}
