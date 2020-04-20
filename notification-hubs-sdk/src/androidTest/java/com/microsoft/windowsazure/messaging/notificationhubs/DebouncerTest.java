package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;

import androidx.test.filters.SmallTest;

import org.junit.Before;
import org.junit.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

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
    private final int testTimeout = debouncerDelayInMillisec + 1000;

    @Before
    public void Before() {
        installation = new Installation();
        installation.setInstallationId("id_first");
        installation.setPushChannel("pushChannel");
        installation.addTags(Stream.of("tag1", "tag2", "tag3").collect(Collectors.toList()));

        installation_second = new Installation();
        installation_second.setInstallationId("id_second");
        installation_second.setPushChannel("pushChannel");

        installation_third = new Installation();
        installation_third.setInstallationId("id_third");
        installation_third.setPushChannel("pushChannel");
    }

    @Test
    public void DebouncerDoesNotInvokeSaveImmediately() {
        NotificationHubInstallationManager nhInstallationManager = mock(NotificationHubInstallationManager.class);
        DebounceInstallationManager debouncer = new DebounceInstallationManager(nhInstallationManager);
        debouncer.saveInstallation(context, installation);
        verify(nhInstallationManager, times(0)).saveInstallation(context, installation);
    }

    @Test
    public void DebouncerInvokesSaveAfterDelayHappyPath() throws InterruptedException {
        NotificationHubInstallationManager nhInstallationManager = mock(NotificationHubInstallationManager.class);
        DebounceInstallationManager debouncer = new DebounceInstallationManager(nhInstallationManager);
        debouncer.saveInstallation(context, installation);
        Thread.sleep(testTimeout);
        verify(nhInstallationManager, times(1)).saveInstallation(context, installation);
    }

    @Test
    public void DebouncerInvokesSaveForMostRecent() throws InterruptedException {
        NotificationHubInstallationManager nhInstallationManager = mock(NotificationHubInstallationManager.class);
        DebounceInstallationManager debouncer = new DebounceInstallationManager(nhInstallationManager);
        debouncer.saveInstallation(context, installation);
        debouncer.saveInstallation(context, installation_second);
        debouncer.saveInstallation(context, installation_third);
        Thread.sleep(testTimeout);
        verify(nhInstallationManager, times(0)).saveInstallation(context, installation);
        verify(nhInstallationManager, times(0)).saveInstallation(context, installation_second);
        verify(nhInstallationManager, times(1)).saveInstallation(context, installation_third);
    }

    @Test
    public void DebouncerInvokesSaveTwice() throws InterruptedException {
        NotificationHubInstallationManager nhInstallationManager = mock(NotificationHubInstallationManager.class);
        DebounceInstallationManager debouncer = new DebounceInstallationManager(nhInstallationManager);
        debouncer.saveInstallation(context, installation);
        Thread.sleep(testTimeout);
        debouncer.saveInstallation(context, installation_second);
        Thread.sleep(testTimeout);
        verify(nhInstallationManager, times(1)).saveInstallation(context, installation);
        verify(nhInstallationManager, times(1)).saveInstallation(context, installation_second);
    }

    @Test
    public void DebouncerInvokesRestartsDelay() throws InterruptedException {
        NotificationHubInstallationManager nhInstallationManager = mock(NotificationHubInstallationManager.class);
        DebounceInstallationManager debouncer = new DebounceInstallationManager(nhInstallationManager);
        debouncer.saveInstallation(context, installation);
        Thread.sleep(debouncerDelayInMillisec - 1000);
        // Invoke second call during delay, scheduler should be restarted, delay 2seconds
        debouncer.saveInstallation(context, installation_second);
        Thread.sleep(1000);
        verify(nhInstallationManager, times(0)).saveInstallation(context, installation);
        verify(nhInstallationManager, times(0)).saveInstallation(context, installation_second);
        Thread.sleep(1000);
        // After 2 seconds recent installation should be saved
        verify(nhInstallationManager, times(0)).saveInstallation(context, installation);
        verify(nhInstallationManager, times(1)).saveInstallation(context, installation_second);
    }

    @Test
    public void DebouncerDoesNotInvokeSaveForSameInstallation() throws InterruptedException {
        NotificationHubInstallationManager nhInstallationManager = mock(NotificationHubInstallationManager.class);
        DebounceInstallationManager debouncer = new DebounceInstallationManager(nhInstallationManager);
        debouncer.saveInstallation(context, installation);
        Thread.sleep(testTimeout);
        debouncer.saveInstallation(context, installation);
        Thread.sleep(testTimeout);
        verify(nhInstallationManager, times(1)).saveInstallation(context, installation);
    }
}
