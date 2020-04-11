package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;

public interface InstallationManager {
    /**
     * Updates a backend with the updated Installation information for this device.
     * @param installation The record to update.
     * @return A future, with the Installation ID as the value.
     */
    void saveInstallation(Context context, Installation installation);
}
