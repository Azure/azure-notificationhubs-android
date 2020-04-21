package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.microsoft.windowsazure.messaging.R;

public class NetworkStatusReceiver extends BroadcastReceiver {

    private boolean isConnected = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager conn =  (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            if (!isConnected){
                //TODO: implement reinstall
            }
            isConnected = networkInfo.isConnected();

            Toast.makeText(context, "Connection is exist", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Lost connection", Toast.LENGTH_LONG).show();
            isConnected = false;
        }
    }
}
