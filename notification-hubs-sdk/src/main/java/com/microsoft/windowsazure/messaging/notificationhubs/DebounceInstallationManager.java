package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DebounceInstallationManager implements InstallationManager {

    protected InstallationManager mInstallationManager;

    private long mInterval;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public DebounceInstallationManager(InstallationManager installationManager) {
        super();
        this.mInstallationManager = installationManager;
        mInterval = 2000;
    }

    public DebounceInstallationManager(InstallationManager installationManager, long interval) {
        super();
        this.mInstallationManager = installationManager;
        mInterval = interval;
    }

    private ScheduledFuture<?> schedFuture;

    public void shutdown() {
        scheduler.shutdownNow();
    }

    @Override
    public void saveInstallation(Context context, Installation installation) {
        if (schedFuture != null && !schedFuture.isDone()) {
            schedFuture.cancel(true);
        }

        // verify if not equal to recent:
        // get recent from shared preferences and if ShPref.RecentInstallation.Equals(installation)
        // do nothing otherwise schedule
        schedFuture = scheduler.schedule(() ->
        {
            mInstallationManager.saveInstallation(context, installation);
        }, mInterval, TimeUnit.MILLISECONDS);
    }
}