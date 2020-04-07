package com.microsoft.windowsazure.messaging.notificationhubs;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class NotificationHubTest {

    @Test
    public void useInstanceMiddleware() {
        NotificationHub specimen = new NotificationHub();
        final int[] callCount = new int[]{0, 0};

        specimen.useInstanceMiddleware(next -> {
            callCount[0]++;
            assertNotNull("Next InstallationEnricher should never be null.", next);
            return subject -> {
                callCount[1]++;
                next.enrichInstallation(subject);
                assertNotNull("Installation to be enriched should never be null", subject);
            };
        });

        specimen.reinstallInstance();

        assertEquals("Middleware should have been invoked exactly once.", 1, callCount[0]);
        assertEquals("InstallationEnricher should have been invoked exactly once.", 1, callCount[1]);
    }

    @Test
    public void testUseInstanceMiddleware() {
        NotificationHub specimen = new NotificationHub();

        final int[] callCount = new int[]{0, 0, 0, 0};

        specimen.useInstanceMiddleware(next -> {
            assertEquals("Middleware should be called as a stack", 1, callCount[2]);
            callCount[0]++;
            return subject -> {
                assertEquals("Installation Enrichers are called in the order the middleware was added", 0, callCount[3]);
                callCount[1]++;
                next.enrichInstallation(subject);
            };
        });

        specimen.useInstanceMiddleware(next -> {
            assertEquals("Middleware should be called as a stack", 0, callCount[0]);
            callCount[2]++;
            return new InstallationEnricher() {
                @Override
                public void enrichInstallation(Installation subject) {
                    assertEquals("Installation Enrichers are called in the order the middleware was added", 1, callCount[1]);
                    callCount[3]++;
                    next.enrichInstallation(subject);
                }
            };
        });

        specimen.reinstallInstance();

        for (int x: callCount) {
            assertEquals("each method should be called exactly once", 1, x);
        }
    }

    @Test
    public void addInstanceTag() {
    }

    @Test
    public void addInstanceTags() {
        NotificationHub specimen = new NotificationHub();

        final String tag1 = "tag1";
        final String tag2 = "tag2";
        final String tag3 = "tag3";

        final Set<String> firstBatch = new HashSet<String>();
        firstBatch.add(tag1);
        firstBatch.add(tag2);

        final Set<String> secondBatch = new HashSet<String>();
        secondBatch.add(tag2);
        secondBatch.add(tag3);

        final Set<String> thirdBatch = new HashSet<String>();
        thirdBatch.add(tag1);
        thirdBatch.add(tag3);

        assertTrue(specimen.addInstanceTags(firstBatch));
        assertTrue(specimen.addInstanceTags(secondBatch));
        assertFalse(specimen.addInstanceTags(thirdBatch));
    }

    @Test
    public void removeInstanceTag() {
    }

    @Test
    public void removeInstanceTags() {
    }

    @Test
    public void getInstanceTags() {
    }

    @Test
    public void clearInstanceTags() {
    }
}