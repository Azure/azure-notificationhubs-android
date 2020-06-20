package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.filters.SmallTest;

import com.microsoft.windowsazure.messaging.R;

import org.junit.Before;
import org.junit.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@SmallTest
public class DebouncerTest {
    private Context context = getInstrumentation().getTargetContext();
    private Installation installation;
    private Installation installation_second;
    private Installation installation_third;
    private final int debouncerDelayInMillisec = 2000;
    private final int debouncerDelayPlusSecond = debouncerDelayInMillisec + 1000;

    private final InstallationAdapter.Listener logSuccessListener = new InstallationAdapter.Listener() {
        @Override
        public void onInstallationSaved(Installation i) {
            System.out.println("Success");
        }
    };

    private final InstallationAdapter.ErrorListener logFailureListener = new InstallationAdapter.ErrorListener() {
        @Override
        public void onInstallationSaveError(Exception e) {
            System.out.println("Failed");
        }
    };

    @Before
    public void Before() {
        installation = new Installation();
        installation.setInstallationId("id_first");
        installation.setPushChannel("pushChannel");
        installation.addTag("tag1");
        installation.addTag("tag2");
        installation.addTag("tag3");

        installation_second = new Installation();
        installation_second.setInstallationId("id_second");
        installation_second.setPushChannel("pushChannel");

        installation_third = new Installation();
        installation_third.setInstallationId("id_third");
        installation_third.setPushChannel("pushChannel");
    }

    @Test
    public void DebouncerDoesNotInvokeSaveImmediately() {
        NotificationHubInstallationAdapter nhInstallationManager = mock(NotificationHubInstallationAdapter.class);
        DebounceInstallationAdapter debouncer = new DebounceInstallationAdapter(context, nhInstallationManager);
        debouncer.saveInstallation(installation, logSuccessListener, logFailureListener);
        verify(nhInstallationManager, times(0)).saveInstallation(installation, logSuccessListener, logFailureListener);
    }

    @Test
    public void DebouncerInvokesSaveAfterDelayHappyPath() throws InterruptedException {
        NotificationHubInstallationAdapter nhInstallationManager = mock(NotificationHubInstallationAdapter.class);
        DebounceInstallationAdapter debouncer = new DebounceInstallationAdapter(context, nhInstallationManager);
        debouncer.saveInstallation(installation, logSuccessListener, logFailureListener);
        Thread.sleep(debouncerDelayPlusSecond);
        verify(nhInstallationManager, times(1)).saveInstallation(installation, logSuccessListener, logFailureListener);
    }

    @Test
    public void DebouncerInvokesSaveForMostRecent() throws InterruptedException {
        NotificationHubInstallationAdapter nhInstallationManager = mock(NotificationHubInstallationAdapter.class);
        DebounceInstallationAdapter debouncer = new DebounceInstallationAdapter(context, nhInstallationManager);
        debouncer.saveInstallation(installation, logSuccessListener, logFailureListener);
        debouncer.saveInstallation(installation_second, logSuccessListener, logFailureListener);
        debouncer.saveInstallation(installation_third, logSuccessListener, logFailureListener);
        Thread.sleep(debouncerDelayPlusSecond);
        verify(nhInstallationManager, times(0)).saveInstallation(installation, logSuccessListener, logFailureListener);
        verify(nhInstallationManager, times(0)).saveInstallation(installation_second, logSuccessListener, logFailureListener);
        verify(nhInstallationManager, times(1)).saveInstallation(installation_third, logSuccessListener, logFailureListener);
    }

    @Test
    public void DebouncerInvokesSaveTwice() throws InterruptedException {
        NotificationHubInstallationAdapter nhInstallationManager = mock(NotificationHubInstallationAdapter.class);
        DebounceInstallationAdapter debouncer = new DebounceInstallationAdapter(context, nhInstallationManager);
        debouncer.saveInstallation(installation, logSuccessListener, logFailureListener);
        Thread.sleep(debouncerDelayPlusSecond);
        debouncer.saveInstallation(installation_second, logSuccessListener, logFailureListener);
        Thread.sleep(debouncerDelayPlusSecond);
        verify(nhInstallationManager, times(1)).saveInstallation(installation, logSuccessListener, logFailureListener);
        verify(nhInstallationManager, times(1)).saveInstallation(installation_second, logSuccessListener, logFailureListener);
    }

    @Test
    public void DebouncerRestartsDelay() throws InterruptedException {
        NotificationHubInstallationAdapter nhInstallationManager = mock(NotificationHubInstallationAdapter.class);
        DebounceInstallationAdapter debouncer = new DebounceInstallationAdapter(context, nhInstallationManager);
        debouncer.saveInstallation(installation, logSuccessListener, logFailureListener);
        Thread.sleep(debouncerDelayInMillisec - 1000);
        // Invoke second call during delay, scheduler should be restarted, delay 2seconds
        debouncer.saveInstallation(installation_second, logSuccessListener, logFailureListener);
        Thread.sleep(1000);
        verify(nhInstallationManager, times(0)).saveInstallation(installation, logSuccessListener, logFailureListener);
        verify(nhInstallationManager, times(0)).saveInstallation(installation_second, logSuccessListener, logFailureListener);
        Thread.sleep(1000);
        // After 2 seconds recent installation should be saved
        verify(nhInstallationManager, times(0)).saveInstallation(installation, logSuccessListener, logFailureListener);
        verify(nhInstallationManager, times(1)).saveInstallation(installation_second, logSuccessListener, logFailureListener);
    }

    @Test
    public void DebouncerDoesNotInvokeSaveForSameInstallation() throws InterruptedException {
        NotificationHubInstallationAdapter nhInstallationManager = mock(NotificationHubInstallationAdapter.class);
        DebounceInstallationAdapter debouncer = new DebounceInstallationAdapter(context, nhInstallationManager);
        debouncer.saveInstallation(installation, logSuccessListener, logFailureListener);
        Thread.sleep(debouncerDelayPlusSecond);
        debouncer.saveInstallation(installation, logSuccessListener, logFailureListener);
        Thread.sleep(debouncerDelayPlusSecond);
        verify(nhInstallationManager, times(1)).saveInstallation(installation, logSuccessListener, logFailureListener);
    }

    @Test
    public void DebouncerSavesRecentToSharedPreferences() throws InterruptedException {
        NotificationHubInstallationAdapter nhInstallationManager = mock(NotificationHubInstallationAdapter.class);
        DebounceInstallationAdapter debouncer = new DebounceInstallationAdapter(context, nhInstallationManager);
        debouncer.saveInstallation(installation, logSuccessListener, logFailureListener);
        Thread.sleep(debouncerDelayPlusSecond);

        SharedPreferences mPreferences = context.getSharedPreferences(context.getString(R.string.installation_enrichment_file_key), Context.MODE_MULTI_PROCESS);
        int recentHash = mPreferences.getInt(DebounceInstallationAdapter.LAST_ACCEPTED_HASH_KEY,0);

        assertTrue(recentHash == installation.hashCode());
    }
}
