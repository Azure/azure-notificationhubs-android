package com.microsoft.windowsazure.messaging.notificationhubs;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class PushChannelValidationAdapterTest {

    @Test
    public void saveInstallation() {
        final Installation installationWithPushChannel = new Installation();
        installationWithPushChannel.setPushChannel("deviceId1");

        final List<Boolean> encounteredSuccess = new ArrayList<Boolean>();

        PushChannelValidationAdapter subject = new PushChannelValidationAdapter(new InstallationAdapter() {
            @Override
            public void saveInstallation(Installation installation, Listener onInstallationSaved, ErrorListener onInstallationSaveError) {
                onInstallationSaved.onInstallationSaved(installation);
                encounteredSuccess.add(true);
            }
        }, -1);

        subject.saveInstallation(
                installationWithPushChannel,
                new InstallationAdapter.Listener() {
                    @Override
                    public void onInstallationSaved(Installation i) {
                        Assert.assertSame(installationWithPushChannel, i);
                    }
                },
                new InstallationAdapter.ErrorListener() {
                    @Override
                    public void onInstallationSaveError(Exception e) {
                        Assert.fail("Should not have a save error when Installation is valid");
                    }
                });

        Assert.assertTrue("Saved callback must have been invoked at least once.", encounteredSuccess.size() > 0);
    }

    @Test
    public void saveInstallation_noRetryFailure() {
        Installation installationWithoutPushChannel = new Installation();
        final List<Boolean> encounteredError = new ArrayList<Boolean>();

        PushChannelValidationAdapter subject = new PushChannelValidationAdapter(new InstallationAdapter() {
            @Override
            public void saveInstallation(Installation installation, Listener onInstallationSaved, ErrorListener onInstallationSaveError) {
                onInstallationSaved.onInstallationSaved(installation);
            }
        }, -1);

        subject.saveInstallation(
                installationWithoutPushChannel,
                new InstallationAdapter.Listener() {
                    @Override
                    public void onInstallationSaved(Installation i) {
                        Assert.fail("Installations without PushChannel should never pass as successful");
                    }
                },
                new InstallationAdapter.ErrorListener() {
                    @Override
                    public void onInstallationSaveError(Exception e) {
                        Assert.assertTrue("When retries are exhausted, an IllegalArgumentException should be raised", e instanceof IllegalArgumentException);
                        encounteredError.add(true);
                    }
                });

        Assert.assertTrue("Error must be encountered at least once.", encounteredError.size() > 0);
    }

    @Test
    public void saveInstallation_failureAfterRetries() {
        final int RETRY_COUNT = 3;

        final List<Boolean> encounteredError = new ArrayList<Boolean>();

        Installation installationWithoutPushChannel = new Installation();

        PushChannelValidationAdapter subject = new PushChannelValidationAdapter(new InstallationAdapter() {
            @Override
            public void saveInstallation(Installation installation, Listener onInstallationSaved, ErrorListener onInstallationSaveError) {
                onInstallationSaved.onInstallationSaved(installation);
            }
        }, RETRY_COUNT);

        for(int i = 0; i <= RETRY_COUNT; i++) {
            final int currentI = i;
            subject.saveInstallation(
                    installationWithoutPushChannel,
                    new InstallationAdapter.Listener() {
                        @Override
                        public void onInstallationSaved(Installation i) {
                            Assert.fail("Installations without PushChannel should never pass as successful");
                        }
                    },
                    new InstallationAdapter.ErrorListener() {
                        @Override
                        public void onInstallationSaveError(Exception e) {
                            Assert.assertEquals("Error callback should only be invoked after the retry count is exhausted.", RETRY_COUNT, currentI);
                            Assert.assertTrue("When retries are exhausted, an IllegalArgumentException should be raised", e instanceof IllegalArgumentException);
                            encounteredError.add(true);
                        }
                    });
        }

        Assert.assertTrue("Error callback must have been invoked at least once.", encounteredError.size() > 0);
    }
}