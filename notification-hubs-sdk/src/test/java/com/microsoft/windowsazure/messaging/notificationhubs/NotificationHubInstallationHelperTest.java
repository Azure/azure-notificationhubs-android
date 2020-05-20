package com.microsoft.windowsazure.messaging.notificationhubs;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class NotificationHubInstallationHelperTest {
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void parseEndpointUrlHappyPath() {
        String rawEndpoint = "sb://marstr-fcm-tutorials.servicebus.windows.net/";
        String sbEndpoint = "marstr-fcm-tutorials.servicebus.windows.net";

        assertEquals(sbEndpoint, NotificationHubInstallationHelper.parseSbEndpoint(rawEndpoint));
    }

    @Test
    public void parseEndpointUrlNotMatchesPattern() {
        String rawEndpoint = "nh://marstr-fcm-tutorials.servicebus.windows.net/";

        exceptionRule.expect(IllegalArgumentException.class);

        String parsedEndpoint = NotificationHubInstallationHelper.parseSbEndpoint(rawEndpoint);
    }
}
