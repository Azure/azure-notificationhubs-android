package com.microsoft.windowsazure.messaging.notificationhubs;

import com.amazon.device.messaging.ADMMessageReceiver;

public class AnhAdmMessageReceiver extends ADMMessageReceiver {
    public AnhAdmMessageReceiver()
    {
        super(AnhAdmMessageHandler.class);
        registerJobServiceClass(AnhAdmMessageHandlerJobBase.class, 1324124);
    }
}
