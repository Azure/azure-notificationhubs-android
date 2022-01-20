package com.microsoft.windowsazure.messaging.notificationhubs;

public class AdmHelper {

    public static final String ADM_CLASSNAME = "com.amazon.device.messaging.ADM";
    public static final String ADMV2_HANDLER = "com.amazon.device.messaging.ADMMessageHandlerJobBase";

    public static final boolean IS_ADM_AVAILABLE;
    public static final boolean IS_ADM_V2;

    private static boolean isClassAvailable(final String className)
    {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    static {
        IS_ADM_AVAILABLE = isClassAvailable(ADM_CLASSNAME);
        IS_ADM_V2 = isClassAvailable(ADMV2_HANDLER);
    }
}
