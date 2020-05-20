package com.microsoft.windowsazure.messaging.notificationhubs;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class ConnectionStringTest {
    private static final String SHARED_ACCESS_KEY_NAME_KEY = "SharedAccessKeyName";
    private static final String ENDPOINT_KEY = "Endpoint";
    private static final String SHARED_ACCESS_KEY = "SharedAccessKey";

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void parseConnectionStringHappyPath() {
        String rawConnectionString = "Endpoint=sb://marstr-fcm-tutorials.servicebus.windows.net/;SharedAccessKeyName=DefaultListenSharedAccessSignature;SharedAccessKey=9maoDX5iLZqhfK7hhqK5qSIzoY4TecAyAgL0R+w48Gc=";
        ConnectionString connectionString = new ConnectionString(
                        "sb://marstr-fcm-tutorials.servicebus.windows.net/",
                        "DefaultListenSharedAccessSignature",
                        "9maoDX5iLZqhfK7hhqK5qSIzoY4TecAyAgL0R+w48Gc=");

        assertEquals(connectionString, ConnectionString.parse(rawConnectionString));
    }

    @Test
    public void parseConnectionEndpointMissing() {
        String rawConnectionString = "Endpoint=;SharedAccessKeyName=DefaultListenSharedAccessSignature;SharedAccessKey=9maoDX5iLZqhfK7hhqK5qSIzoY4TecAyAgL0R+w48Gc=";
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage(ENDPOINT_KEY);

        ConnectionString connectionString = ConnectionString.parse(rawConnectionString);
    }

    @Test()
    public void parseConnectionSharedAccessKeyNameMissing() {
        String rawConnectionString = "Endpoint=sb://marstr-fcm-tutorials.servicebus.windows.net/;SharedAccessKeyName;SharedAccessKey=9maoDX5iLZqhfK7hhqK5qSIzoY4TecAyAgL0R+w48Gc=";
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage(SHARED_ACCESS_KEY_NAME_KEY);

        ConnectionString connectionString = ConnectionString.parse(rawConnectionString);
    }

    @Test()
    public void parseConnectionSharedAccessKeyMissing() {
        String rawConnectionString = "Endpoint=sb://marstr-fcm-tutorials.servicebus.windows.net/;SharedAccessKeyName=DefaultListenSharedAccessSignature;SharedAccessKey=";
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage(SHARED_ACCESS_KEY);

        ConnectionString connectionString = ConnectionString.parse(rawConnectionString);
    }
}