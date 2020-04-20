package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;
import android.content.SharedPreferences;

import com.microsoft.windowsazure.messaging.R;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DebounceInstallationManager implements InstallationManager {

    private static final String PREFERENCE_KEY = "recentInstallation";
    private final ScheduledExecutorService mScheduler = Executors.newScheduledThreadPool(1);
    private InstallationManager mInstallationManager;
    private long mInterval;
    private ScheduledFuture<?> mSchedFuture;
    private SharedPreferences mPreferences;

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

    private void setPreferences(Context context) {
        mPreferences = context.getSharedPreferences(String.valueOf(R.string.installation_enrichment_file_key), Context.MODE_MULTI_PROCESS);
    }

    @Override
    public void saveInstallation(Context context, Installation installation) {
        if (mPreferences == null) {
            setPreferences(context);
        }

        if (mSchedFuture != null && !mSchedFuture.isDone()) {
            mSchedFuture.cancel(true);
        }

        int recentHash = mPreferences.getInt(PREFERENCE_KEY, 0);
        if (recentHash != 0 && recentHash == installation.hashCode()) {
            return;
        }

        mSchedFuture = mScheduler.schedule(() ->
        {
            try {
                mInstallationManager.saveInstallation(context, installation);
                mPreferences.edit().putInt(PREFERENCE_KEY, installation.hashCode()).apply();
            } catch (Exception e) {

            }
        }, mInterval, TimeUnit.MILLISECONDS);
    }
}