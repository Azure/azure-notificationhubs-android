package com.microsoft.windowsazure.messaging;

import android.content.Context;

import com.microsoft.windowsazure.messaging.notificationhubs.IdAssignmentEnricher;
import com.microsoft.windowsazure.messaging.notificationhubs.Installation;
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationHubInstallationManager;

import org.junit.Test;

import java.util.UUID;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

public class NotificationHubInstallationManagerTest {
    @Test
    public void saveInstallation() {
        String nhName = "myHub";
        Context context = getInstrumentation().getTargetContext();
        Installation installation = new Installation();
        IdAssignmentEnricher idAssignmentEnricher = new IdAssignmentEnricher(UUID.randomUUID().toString());
        idAssignmentEnricher.enrichInstallation(installation);

        NotificationHubInstallationManager manager = new NotificationHubInstallationManager("nh-hub-test-v1-new-endpoint-final-try-nh", "Endpoint=sb://runners-nh-int7-sn1-001.notificationhub.int7.windows-int.net/;SharedAccessKeyName=DefaultFullSharedAccessSignature;SharedAccessKey=7JfAts0S2IdMBsVlZQcsGmoqN98DqhlgdJb/AYwdCmA=");
        manager.saveInstallation(context, installation);
        System.out.println();
    }
}