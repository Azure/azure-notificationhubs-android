package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;

public interface InstallationAdapter {
    /**
     * Updates a backend with the updated Installation information for this device.
     * @param installation The record to update.
     */
    void saveInstallation(Context context, Installation installation);
}
