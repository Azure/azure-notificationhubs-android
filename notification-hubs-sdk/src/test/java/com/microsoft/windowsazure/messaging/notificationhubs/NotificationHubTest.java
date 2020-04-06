package com.microsoft.windowsazure.messaging.notificationhubs;

import org.junit.Test;

import static org.junit.Assert.*;

public class NotificationHubTest {

    @Test
    public void setInstanceListener() {
    }

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
}