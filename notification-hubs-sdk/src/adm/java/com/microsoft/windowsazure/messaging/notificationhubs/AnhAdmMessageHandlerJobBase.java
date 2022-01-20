package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.amazon.device.messaging.ADMMessageHandlerJobBase;

public class AnhAdmMessageHandlerJobBase extends ADMMessageHandlerJobBase
{

    private final NotificationHub mHub;

    public AnhAdmMessageHandlerJobBase()
    {
        this(NotificationHub.getInstance());
    }

    public AnhAdmMessageHandlerJobBase(NotificationHub hub)
    {
        super();
        mHub = hub;
    }


    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i("ANH", "message received from ADM");
        mHub.getInstanceListener().onPushNotificationReceived(context.getApplicationContext(), intent);
    }

    @Override
    protected void onRegistrationError(Context context, String errMessage) {
        Log.e("ANH", "ADM registration error: " + errMessage);
    }

    @Override
    protected void onRegistered(Context context, String s) {
        Log.i("ANH", "registered for notifications with ADM");
    }

    @Override
    protected void onUnregistered(Context context, String s) {
        Log.i("ANH", "ADM registration ID invalidated");
    }
}
