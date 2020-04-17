package com.microsoft.windowsazure.messaging.notificationhubs;

public abstract class DebounceInstallationManager implements InstallationManager {

    protected InstallationManager installationManager;

    public DebounceInstallationManager(InstallationManager installationManager) {
        super();
        this.installationManager = installationManager;
    }
}
