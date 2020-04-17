package com.microsoft.windowsazure.messaging.notificationhubs;

import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ConnectionStringTest {

    @Test
    @Ignore
    public void parse() {
        Map<String, ConnectionString> testCases = new HashMap<String, ConnectionString>();
        testCases.put(
                "Endpoint=sb://marstr-fcm-tutorials.servicebus.windows.net/;SharedAccessKeyName=DefaultListenSharedAccessSignature;SharedAccessKey=9maoDX5iLZqhfK7hhqK5qSIzoY4TecAyAgL0R+w48Gc=",
                new ConnectionString(
                        "sb://marstr-fcm-tutorials.servicebus.windows.net/",
                        "DefaultListenSharedAccessSignature",
                        "9maoDX5iLZqhfK7hhqK5qSIzoY4TecAyAgL0R+w48Gc="));

        for (Map.Entry<String, ConnectionString> tc: testCases.entrySet()) {
            assertEquals(tc.getValue(), ConnectionString.parse(tc.getKey()));
        }
    }
}