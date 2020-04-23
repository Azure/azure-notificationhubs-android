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

    private INotificationHub _notificationHub;

    public NetworkStatusReceiver() {
        _notificationHub = NotificationHub.getInstance();
    }

    public NetworkStatusReceiver(INotificationHub nh) {
        _notificationHub = nh;
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
                _notificationHub.reinstallInstance();
            } catch (Exception e) {

            }
        }
    }
}
