package com.microsoft.windowsazure.messaging.notificationhubs;

import androidx.test.filters.SmallTest;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

@SmallTest
public class ExpirationAdapterTest {

    @Test
    public void nearOrPastExpiration() {
        ExpirationAdapter subject = new ExpirationAdapter(
                getInstrumentation().getTargetContext(),
                null,
                null);

        Date now = new Date(0);

        Map<Date, Boolean> testCases = new HashMap<Date, Boolean>();
        testCases.put(new Date(0), true);
        testCases.put(new Date(-10), true);
        testCases.put(new Date(ExpirationAdapter.INSTALLATION_NEAR_EXPIRATION_WINDOW), true);
        testCases.put(new Date(ExpirationAdapter.INSTALLATION_NEAR_EXPIRATION_WINDOW / 2), true);
        testCases.put(new Date(ExpirationAdapter.INSTALLATION_NEAR_EXPIRATION_WINDOW + 1), false);
        testCases.put(new Date(ExpirationAdapter.INSTALLATION_NEAR_EXPIRATION_WINDOW * 2), false);

        for (Map.Entry<Date, Boolean> tc: testCases.entrySet()) {
            Installation i = new Installation();
            i.setExpiration(tc.getKey());
            boolean got = subject.nearOrPastExpiration(i, now);

            Assert.assertEquals( "Now: " + now.getTime() + " Expires: " + tc.getKey().getTime() + " Expected: " + tc.getValue(), tc.getValue(), got);
        }
    }

    @Test
    public void getInstallationHashIsDeterministic() {

        Installation subject = new Installation();
        subject.setExpiration(new Date(662457600000L));
        subject.setPushChannel("pushChannel1");

        int expected = ExpirationAdapter.getInstallationHash(subject);

        for (int i = 0; i < 15; i++) {
            Assert.assertEquals(expected, ExpirationAdapter.getInstallationHash(subject));
        }
    }

    @Test
    public void getInstallationHashDoesNotConsiderExpiration() {
        final String sharedPushChannel = "sharedPushChannel";

        Installation installation1 = new Installation();
        installation1.setExpiration(new Date(662457600000L));
        installation1.setPushChannel(sharedPushChannel);
        int hash1 = ExpirationAdapter.getInstallationHash(installation1);

        Installation installation2 = new Installation();
        installation2.setExpiration(new Date(743756400000L));
        installation2.setPushChannel(sharedPushChannel);
        int hash2 = ExpirationAdapter.getInstallationHash(installation2);

        Assert.assertEquals(hash1, hash2);
    }
}