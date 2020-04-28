package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Synchronizes updates to the tags or other installation aware items on the device
 * when the device comes back online.
 */
public class NetworkStatusReceiver extends BroadcastReceiver {

    private final NotificationHub mNotificationHub;

    public NetworkStatusReceiver() {
        mNotificationHub = NotificationHub.getInstance();
    }

    public NetworkStatusReceiver(NotificationHub nh) {
        mNotificationHub = nh;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            try {
                mNotificationHub.reinstallInstance();
            } catch (Exception e) {

            }
        }
    }
}
