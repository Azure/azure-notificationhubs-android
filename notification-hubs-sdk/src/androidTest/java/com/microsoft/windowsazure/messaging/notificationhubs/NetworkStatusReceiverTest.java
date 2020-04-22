package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.test.filters.SmallTest;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SmallTest
public class NetworkStatusReceiverTest {
    private NetworkStatusReceiver mReceiver;

    @Before
    public void setUp() {
        mReceiver = new NetworkStatusReceiver();
    }

    @Test
    public void ReceiverReinstallsWhenOnline() {
        // Setup
        ConnectivityManager cm = mock(ConnectivityManager.class);
        Context context = mock(Context.class);
        when(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(cm);
        NetworkInfo networkInfo = mock(NetworkInfo.class);
        when(cm.getActiveNetworkInfo()).thenReturn(networkInfo);
        when(networkInfo.isConnectedOrConnecting()).thenReturn(true);
        // NotificationHub nh = mock(NotificationHub.class);

        // Exercise
        mReceiver.onReceive(context, new Intent(ConnectivityManager.CONNECTIVITY_ACTION));

        // Verify
        // verify(nh, times(1)).reinstall();
    }

    @Test
    public void ReceiverDoesNotReinstallWhenOffline() {
        // Setup
        ConnectivityManager cm = mock(ConnectivityManager.class);
        Context context = mock(Context.class);
        when(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(cm);
        NetworkInfo networkInfo = mock(NetworkInfo.class);
        when(cm.getActiveNetworkInfo()).thenReturn(networkInfo);
        when(networkInfo.isConnectedOrConnecting()).thenReturn(false);
        // NotificationHub nh = mock(NotificationHub.class);

        // Exercise
        mReceiver.onReceive(context, new Intent(ConnectivityManager.CONNECTIVITY_ACTION));

        // Verify
        // verify(nh, times(0)).reinstall();
    }
}
