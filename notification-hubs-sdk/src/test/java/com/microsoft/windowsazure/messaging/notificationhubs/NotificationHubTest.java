package com.microsoft.windowsazure.messaging.notificationhubs;

import org.junit.Ignore;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class NotificationHubTest {

    @Test
    public void useInstanceVisitor() {
        NotificationHub specimen = new NotificationHub();
        final int[] callCount = new int[]{0};

        specimen.useInstanceVisitor(
           subject -> {
                callCount[0]++;
                assertNotNull("Installation to be visited should never be null", subject);
           });

        specimen.reinstallInstance();

        assertEquals("InstallationEnricher should have been invoked exactly once.", 1, callCount[0]);
    }

    @Test
    public void testUseInstanceMiddleware() {
        NotificationHub specimen = new NotificationHub();
        final String INCORRECT_ORDER_MESSAGE = "Installation visitors should be called in the order they were added";
        final int[] callCount = new int[]{0, 0};

        specimen.useInstanceVisitor(subject -> {
                assertEquals(INCORRECT_ORDER_MESSAGE, 0, callCount[1]);
                callCount[0]++;
            });

        specimen.useInstanceVisitor(subject -> {
            assertEquals(INCORRECT_ORDER_MESSAGE, 1, callCount[0]);
            callCount[1]++;
        });

        specimen.reinstallInstance();

        for (int x: callCount) {
            assertEquals("each visitor should be called exactly once", 1, x);
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