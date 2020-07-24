package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.filters.SmallTest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Semaphore;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

@SmallTest
public class DebouncerTest {
    private Installation installation;
    private Installation installation_second;
    private Installation installation_third;

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
    public void DebouncerInvokesSaveAfterDelayHappyPath() throws InterruptedException {
        final long test_interval_ms = 500;
        final Semaphore safeToAdvance = new Semaphore(0);
        InstallationAdapter nhInstallationManager = new InstallationAdapter() {
            @Override
            public void saveInstallation(Installation installation, Listener onInstallationSaved, ErrorListener onInstallationSaveError) {
                safeToAdvance.release();
                onInstallationSaved.onInstallationSaved(installation);
            }
        };
        DebounceInstallationAdapter debouncer = new DebounceInstallationAdapter(nhInstallationManager, test_interval_ms, new MockSharedPreferences());
        long startTime = System.currentTimeMillis();
        debouncer.saveInstallation(installation, logSuccessListener, logFailureListener);
        safeToAdvance.acquire();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        Assert.assertTrue("Debouncer must wait the requested amount of time after receiving an installation.", duration >= test_interval_ms);
    }

    @Test
    public void DebouncerInvokesSaveForMostRecent() throws InterruptedException {
        final Set<Installation> received = new HashSet<Installation>();
        final Semaphore safeToProceed = new Semaphore(0);
        final InstallationAdapter.Listener success = new InstallationAdapter.Listener() {
            @Override
            public void onInstallationSaved(Installation i) {
                safeToProceed.release();
            }
        };

        InstallationAdapter downstream = new InstallationAdapter() {
            @Override
            public void saveInstallation(Installation installation, Listener onInstallationSaved, ErrorListener onInstallationSaveError) {
                synchronized (received) {
                    received.add(installation);
                }
                onInstallationSaved.onInstallationSaved(installation);
            }
        };
        DebounceInstallationAdapter debouncer = new DebounceInstallationAdapter(downstream, 250, new MockSharedPreferences());
        debouncer.saveInstallation(installation, success, logFailureListener);
        debouncer.saveInstallation(installation_second, success, logFailureListener);
        debouncer.saveInstallation(installation_third, success, logFailureListener);
        safeToProceed.acquire();

        Assert.assertEquals("There must be precisely one accepted installation.",1, received.size());
        Assert.assertTrue("The single accepted installation must be the last one sent.", received.contains(installation_third));
    }

    @Test
    public void DebouncerInvokesSaveTwice() throws InterruptedException {
        final Set<Installation> received = new HashSet<Installation>();
        final Semaphore safeToProceed = new Semaphore(0);
        InstallationAdapter downstream = new InstallationAdapter() {
            @Override
            public void saveInstallation(Installation installation, Listener onInstallationSaved, ErrorListener onInstallationSaveError) {
                synchronized (received) {
                    received.add(installation);
                }
                safeToProceed.release();
                onInstallationSaved.onInstallationSaved(installation);
            }
        };

        DebounceInstallationAdapter debouncer = new DebounceInstallationAdapter(downstream, 250, new MockSharedPreferences());
        debouncer.saveInstallation(installation, logSuccessListener, logFailureListener);
        safeToProceed.acquire();
        debouncer.saveInstallation(installation_second, logSuccessListener, logFailureListener);
        safeToProceed.acquire();

        Assert.assertEquals(2, received.size());
        Assert.assertTrue(received.contains(installation));
        Assert.assertTrue(received.contains(installation_second));
    }

    @Test
    public void DebouncerRestartsDelay() throws InterruptedException {
        final long test_interval_ms = 500;
        final long partial_test_interval_ms = test_interval_ms / 2;

        final Semaphore safeToProceed = new Semaphore(0);
        InstallationAdapter downstream = new InstallationAdapter() {
            @Override
            public void saveInstallation(Installation installation, Listener onInstallationSaved, ErrorListener onInstallationSaveError) {
                safeToProceed.release();
                onInstallationSaved.onInstallationSaved(installation);
            }
        };
        DebounceInstallationAdapter debouncer = new DebounceInstallationAdapter(downstream, test_interval_ms, new MockSharedPreferences());

        long startTime = System.currentTimeMillis();
        debouncer.saveInstallation(installation, logSuccessListener, logFailureListener);
        Thread.sleep(partial_test_interval_ms);
        // Invoke second call during delay, scheduler should be restarted, delay 2seconds
        debouncer.saveInstallation(installation_second, logSuccessListener, logFailureListener);
        long endTime = System.currentTimeMillis();
        long duration = endTime = startTime;

        Assert.assertTrue("Debouncer should have waited at least (test_interval + partial_test_interval) milliseconds.", duration >= (test_interval_ms + partial_test_interval_ms));
    }

    @Test
    public void DebouncerSavesRecentToSharedPreferences() throws InterruptedException {
        final SharedPreferences storage = new MockSharedPreferences();
        final long test_interval_ms = 100;
        final Semaphore safeToProceed = new Semaphore(0);

        InstallationAdapter.Listener success = new InstallationAdapter.Listener() {
            @Override
            public void onInstallationSaved(Installation i) {
                safeToProceed.release();
            }
        };

        InstallationAdapter downstream = new InstallationAdapter() {
            @Override
            public void saveInstallation(Installation installation, Listener onInstallationSaved, ErrorListener onInstallationSaveError) {
                onInstallationSaved.onInstallationSaved(installation);
            }
        };

        DebounceInstallationAdapter debouncer = new DebounceInstallationAdapter(downstream, test_interval_ms, storage);
        debouncer.saveInstallation(installation, success, logFailureListener);
        safeToProceed.acquire();

        int recentHash = storage.getInt(DebounceInstallationAdapter.LAST_ACCEPTED_HASH_KEY,0);

        Assert.assertEquals(recentHash, installation.hashCode());
    }

    @Test
    public void DebounceResilientToInstallationAdapterModifications() throws InterruptedException {
        final long test_interval = 100;
        SharedPreferences storage = new MockSharedPreferences();
        Random r = new Random();
        final Installation modifiableInstallation = new Installation();
        modifiableInstallation.setPushChannel("faux_push_channel");
        modifiableInstallation.setInstallationId("id_" + r.nextLong());
        int hashBeforeAdapter = modifiableInstallation.hashCode();
        final Semaphore adapterStatus = new Semaphore(0);

        InstallationAdapter.Listener success = new InstallationAdapter.Listener() {
            @Override
            public void onInstallationSaved(Installation i) {
                adapterStatus.release();
            }
        };

        InstallationAdapter saboteur = new InstallationAdapter() {
            @Override
            public void saveInstallation(Installation installation, Listener onInstallationSaved, ErrorListener onInstallationSaveError) {
                Assert.assertSame(modifiableInstallation, installation); // If this is failing, the test is malformed. Consider fixing the test, not the Debouncer.
                installation.setExpiration(new Date());
                onInstallationSaved.onInstallationSaved(installation);
            }
        };

        DebounceInstallationAdapter debouncer = new DebounceInstallationAdapter(saboteur, test_interval, storage);
        debouncer.saveInstallation(modifiableInstallation, success, logFailureListener);
        adapterStatus.acquire();

        int hashAfterAdapter = modifiableInstallation.hashCode();
        int savedHash = storage.getInt(DebounceInstallationAdapter.LAST_ACCEPTED_HASH_KEY, 0);

        Assert.assertNotEquals("Test is built to assume hash is modified", hashBeforeAdapter, hashAfterAdapter);
        Assert.assertEquals("The hash which is saved by the DebouncerAdapter should not be influenced by Adapters that come after it.", hashBeforeAdapter, savedHash);
    }
}
