package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.filters.SmallTest;

import com.microsoft.windowsazure.messaging.R;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

    private final InstallationAdapter.SaveListener logSuccessSaveListener = new InstallationAdapter.SaveListener() {
        @Override
        public void onInstallationSaved(Installation i) {
            System.out.println("Successfully Saved");
        }
    };

    private final InstallationAdapter.DeleteListener logSuccessDeleteListener = new InstallationAdapter.DeleteListener() {
        @Override
        public void onInstallationDeleted(String id) {
            System.out.println("Successfully Deleted");
        }
    };

    private final InstallationAdapter.ErrorListener logFailureListener = new InstallationAdapter.ErrorListener() {
        @Override
        public void onInstallationOperationError(Exception e) {
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
        debouncer.saveInstallation(installation, logSuccessSaveListener, logFailureListener);
        verify(nhInstallationManager, times(0)).saveInstallation(installation, logSuccessSaveListener, logFailureListener);
    }

    @Test
    public void DebouncerInvokesSaveAfterDelayHappyPath() throws InterruptedException {
        NotificationHubInstallationAdapter nhInstallationManager = mock(NotificationHubInstallationAdapter.class);
        DebounceInstallationAdapter debouncer = new DebounceInstallationAdapter(context, nhInstallationManager);
        debouncer.saveInstallation(installation, logSuccessSaveListener, logFailureListener);
        Thread.sleep(debouncerDelayPlusSecond);
        verify(nhInstallationManager, times(1)).saveInstallation(installation, logSuccessSaveListener, logFailureListener);
    }

    @Test
    public void DebouncerInvokesSaveForMostRecent() throws InterruptedException {
        NotificationHubInstallationAdapter nhInstallationManager = mock(NotificationHubInstallationAdapter.class);
        DebounceInstallationAdapter debouncer = new DebounceInstallationAdapter(context, nhInstallationManager);
        debouncer.saveInstallation(installation, logSuccessSaveListener, logFailureListener);
        debouncer.saveInstallation(installation_second, logSuccessSaveListener, logFailureListener);
        debouncer.saveInstallation(installation_third, logSuccessSaveListener, logFailureListener);
        Thread.sleep(debouncerDelayPlusSecond);
        verify(nhInstallationManager, times(0)).saveInstallation(installation, logSuccessSaveListener, logFailureListener);
        verify(nhInstallationManager, times(0)).saveInstallation(installation_second, logSuccessSaveListener, logFailureListener);
        verify(nhInstallationManager, times(1)).saveInstallation(installation_third, logSuccessSaveListener, logFailureListener);
    }

    @Test
    public void DebouncerInvokesSaveTwice() throws InterruptedException {
        NotificationHubInstallationAdapter nhInstallationManager = mock(NotificationHubInstallationAdapter.class);
        DebounceInstallationAdapter debouncer = new DebounceInstallationAdapter(context, nhInstallationManager);
        debouncer.saveInstallation(installation, logSuccessSaveListener, logFailureListener);
        Thread.sleep(debouncerDelayPlusSecond);
        debouncer.saveInstallation(installation_second, logSuccessSaveListener, logFailureListener);
        Thread.sleep(debouncerDelayPlusSecond);
        verify(nhInstallationManager, times(1)).saveInstallation(installation, logSuccessSaveListener, logFailureListener);
        verify(nhInstallationManager, times(1)).saveInstallation(installation_second, logSuccessSaveListener, logFailureListener);
    }

    @Test
    public void DebouncerRestartsDelay() throws InterruptedException {
        NotificationHubInstallationAdapter nhInstallationManager = mock(NotificationHubInstallationAdapter.class);
        DebounceInstallationAdapter debouncer = new DebounceInstallationAdapter(context, nhInstallationManager);
        debouncer.saveInstallation(installation, logSuccessSaveListener, logFailureListener);
        Thread.sleep(debouncerDelayInMillisec - 1000);
        // Invoke second call during delay, scheduler should be restarted, delay 2seconds
        debouncer.saveInstallation(installation_second, logSuccessSaveListener, logFailureListener);
        Thread.sleep(1000);
        verify(nhInstallationManager, times(0)).saveInstallation(installation, logSuccessSaveListener, logFailureListener);
        verify(nhInstallationManager, times(0)).saveInstallation(installation_second, logSuccessSaveListener, logFailureListener);
        Thread.sleep(1000);
        // After 2 seconds recent installation should be saved
        verify(nhInstallationManager, times(0)).saveInstallation(installation, logSuccessSaveListener, logFailureListener);
        verify(nhInstallationManager, times(1)).saveInstallation(installation_second, logSuccessSaveListener, logFailureListener);
    }

    @Test
    public void DebouncerDoesNotInvokeSaveForSameInstallation() throws InterruptedException {
        NotificationHubInstallationAdapter nhInstallationManager = mock(NotificationHubInstallationAdapter.class);
        DebounceInstallationAdapter debouncer = new DebounceInstallationAdapter(context, nhInstallationManager);
        debouncer.saveInstallation(installation, logSuccessSaveListener, logFailureListener);
        Thread.sleep(debouncerDelayPlusSecond);
        debouncer.saveInstallation(installation, logSuccessSaveListener, logFailureListener);
        Thread.sleep(debouncerDelayPlusSecond);
        verify(nhInstallationManager, times(1)).saveInstallation(installation, logSuccessSaveListener, logFailureListener);
    }

    @Test
    public void DebouncerSavesRecentToSharedPreferences() throws InterruptedException {
        NotificationHubInstallationAdapter nhInstallationManager = mock(NotificationHubInstallationAdapter.class);
        DebounceInstallationAdapter debouncer = new DebounceInstallationAdapter(context, nhInstallationManager);
        debouncer.saveInstallation(installation, logSuccessSaveListener, logFailureListener);
        Thread.sleep(debouncerDelayPlusSecond);

        String PREFERENCE_KEY = "recentInstallation";
        SharedPreferences mPreferences = context.getSharedPreferences(context.getString(R.string.installation_enrichment_file_key), Context.MODE_MULTI_PROCESS);
        int recentHash = mPreferences.getInt(PREFERENCE_KEY,0);

        assertTrue(recentHash == installation.hashCode());
    }

    @Test
    public void DebouncerCallsMultipleInstallationDelete() throws InterruptedException {
        NotificationHubInstallationAdapter nhInstallationAdapter = mock(NotificationHubInstallationAdapter.class);
        DebounceInstallationAdapter debouncer = new DebounceInstallationAdapter(context, nhInstallationAdapter);

        String firstId = installation.getInstallationId();
        String secondId = installation_second.getInstallationId();

        debouncer.deleteInstallation(firstId, logSuccessDeleteListener, logFailureListener);
        debouncer.deleteInstallation(secondId, logSuccessDeleteListener, logFailureListener);
        Thread.sleep(debouncerDelayPlusSecond);
        verify(nhInstallationAdapter, times(1)).deleteInstallation(firstId, logSuccessDeleteListener, logFailureListener);
        verify(nhInstallationAdapter, times(1)).deleteInstallation(secondId, logSuccessDeleteListener, logFailureListener);
    }

    @Test
    public void DebouncerCallsInstallationDeleteOnce() throws InterruptedException {
        NotificationHubInstallationAdapter nhInstallationAdapter = mock(NotificationHubInstallationAdapter.class);
        DebounceInstallationAdapter debouncer = new DebounceInstallationAdapter(context, nhInstallationAdapter);

        String firstId = installation.getInstallationId();

        debouncer.deleteInstallation(firstId, logSuccessDeleteListener, logFailureListener);
        debouncer.deleteInstallation(firstId, logSuccessDeleteListener, logFailureListener);
        Thread.sleep(debouncerDelayPlusSecond);
        verify(nhInstallationAdapter, times(1)).deleteInstallation(firstId, logSuccessDeleteListener, logFailureListener);
    }
}
