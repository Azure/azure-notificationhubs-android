package com.microsoft.windowsazure.messaging.notificationhubs;

import android.util.Log;

import com.amazon.device.messaging.ADM;
import com.amazon.device.messaging.development.ADMManifest;

class NotificationHubExtension {
    public static String PLATFORM = "adm";

    public static void fetchPushChannel(final NotificationHub hub)
    {
        ADMManifest.checkManifestAuthoredProperly(hub.getApplication());

        final ADM adm = new ADM(hub.getApplication());
        String registrationId = adm.getRegistrationId();
        if(registrationId == null)
        {
            Log.d("ANH", "Calling for ADM registration");
            adm.startRegister();
        }
        else {
            hub.setInstancePushChannel(registrationId);
        }
    }
}
