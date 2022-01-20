package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.amazon.device.messaging.ADMMessageHandlerJobBase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AnhAdmMessageHandlerJobBase extends ADMMessageHandlerJobBase
{
    public AnhAdmMessageHandlerJobBase()
    {
        super();
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i("ANH", "message received from ADM");
    }

    @Override
    protected void onRegistrationError(Context context, String errMessage) {
        Log.e("ANH", "ADM registration error: " + errMessage);

        try {
            InputStream is = context.getAssets().open("api_key.txt");
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(isr);
            String apiKey = reader.readLine();
            Log.d("ANH", "We think your API Key: " + apiKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
