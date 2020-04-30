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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

@SmallTest
public class NetworkStatusReceiverTest {
    private NetworkStatusReceiver mReceiver;
    ConnectivityManager cm;
    Context context;
    NetworkInfo networkInfo;

    @Before
    public void setUp() {
        cm = mock(ConnectivityManager.class);
        context = mock(Context.class);
        when(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(cm);
        networkInfo = mock(NetworkInfo.class);
        when(cm.getActiveNetworkInfo()).thenReturn(networkInfo);
    }

    @Test
    public void ReceiverReinstallsWhenOnline() {
        // Setup
        when(networkInfo.isConnectedOrConnecting()).thenReturn(true);
        NotificationHub nh = new NotificationHub();
        NotificationHub nhSpy = spy(nh);
        doNothing().when(nhSpy).beginInstanceInstallationUpdate();
        mReceiver = new NetworkStatusReceiver(nhSpy);

        // Exercise
        mReceiver.onReceive(context, new Intent(ConnectivityManager.CONNECTIVITY_ACTION));

        // Verify
        verify(nhSpy, times(1)).beginInstanceInstallationUpdate();
    }

    @Test
    public void ReceiverDoesNotReinstallWhenOffline() {
        // Setup
        when(networkInfo.isConnectedOrConnecting()).thenReturn(false);
        NotificationHub nh = new NotificationHub();
        NotificationHub nhSpy = spy(nh);
        doNothing().when(nhSpy).beginInstanceInstallationUpdate();
        mReceiver = new NetworkStatusReceiver(nhSpy);

        // Exercise
        mReceiver.onReceive(context, new Intent(ConnectivityManager.CONNECTIVITY_ACTION));

        // Verify
        verify(nhSpy, times(0)).beginInstanceInstallationUpdate();
    }
}
