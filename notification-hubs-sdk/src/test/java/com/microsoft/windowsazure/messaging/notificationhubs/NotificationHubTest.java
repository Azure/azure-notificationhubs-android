package com.microsoft.windowsazure.messaging.notificationhubs;

import android.app.Activity;
import android.os.Bundle;
import com.google.firebase.messaging.RemoteMessage;
import org.junit.Test;
import static org.junit.Assert.*;

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
}