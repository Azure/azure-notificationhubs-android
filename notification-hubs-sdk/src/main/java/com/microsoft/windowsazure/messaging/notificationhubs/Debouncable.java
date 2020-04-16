package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;

import java.util.List;

/**
 * Decorates an arbitrary InstallationManager, responsible for
 * batching several operation requested in quick succession into single operation
 * filtering out same requests.
 */
public class Debouncable extends DebounceInstallationManager {
    /**
     * Read from config
     */
    private byte mDelayInSeconds;

    /**
     * List of recently sent installations
     */
    private List<Installation> mRecentlySent;

    /**
     * Batch of installations
     */
    private List<Installation> mBatch;

    public Debouncable(InstallationManager mInstallationManager) {
        super(mInstallationManager);
    }

    @Override
    public void saveInstallation(Context context, Installation installation) {
        // verify installation is not in mRecentlySent
        // do not send requests immediately
        this.mInstallationManager.saveInstallation(context, installation);
    }
}
