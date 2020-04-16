package com.microsoft.windowsazure.messaging.notificationhubs;

public abstract class DebounceInstallationManager implements InstallationManager {

    protected InstallationManager mInstallationManager;

    public DebounceInstallationManager(InstallationManager mInstallationManager) {
        super();
        this.mInstallationManager = mInstallationManager;
    }
}
