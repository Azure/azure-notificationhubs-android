package com.microsoft.windowsazure.messaging.notificationhubs;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NotificationHubInstallationHelperTest {
    @Test
    public void parseEndpointUrlHappyPath() {
        String rawEndpoint = "sb://marstr-fcm-tutorials.servicebus.windows.net/";
        String sbEndpoint = "marstr-fcm-tutorials.servicebus.windows.net";

        assertEquals(sbEndpoint, NotificationHubInstallationHelper.parseSbEndpoint(rawEndpoint));
    }

    @Test
    public void parseEndpointUrlNotMatchesPattern() {
        String rawEndpoint = "nh://marstr-fcm-tutorials.servicebus.windows.net/";
        String sbEndpoint = "marstr-fcm-tutorials.servicebus.windows.net";

        assertEquals("", NotificationHubInstallationHelper.parseSbEndpoint(rawEndpoint));
    }
}
