package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.microsoft.windowsazure.messaging.R;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Protects the {@link InstallationAdapter} from rapid changes to the current Installation, as well
 * as weeding out calls that match the last request that was sent to the server.
 */
public class DebounceInstallationAdapter implements InstallationAdapter {

    private static final String PREFERENCE_KEY = "recentInstallation";
    private final ScheduledExecutorService mScheduler = Executors.newScheduledThreadPool(1);
    private InstallationAdapter mInstallationAdapter;
    private long mInterval;
    private ScheduledFuture<?> mSchedFuture;
    private SharedPreferences mPreferences;

    public DebounceInstallationAdapter(Context context, InstallationAdapter installationAdapter) {
        this(context, installationAdapter, 2000);
    }

    public DebounceInstallationAdapter(Context context, InstallationAdapter installationAdapter, long interval) {
        super();
        mInstallationAdapter = installationAdapter;
        mInterval = interval;
        mPreferences = context.getSharedPreferences(context.getString(R.string.installation_enrichment_file_key), Context.MODE_MULTI_PROCESS);
    }


    @Override
    public void saveInstallation(final Installation installation, final Listener onSuccess, final ErrorListener onFailure) {
        if (mSchedFuture != null && !mSchedFuture.isDone()) {
            mSchedFuture.cancel(true);
        }

        int recentHash = mPreferences.getInt(PREFERENCE_KEY, 0);
        if (recentHash != 0 && recentHash == installation.hashCode()) {
            return;
        }

        mSchedFuture = mScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    mInstallationAdapter.saveInstallation(installation, onSuccess, onFailure);
                    mPreferences.edit().putInt(PREFERENCE_KEY, installation.hashCode()).apply();
                } catch (Exception e) {
                    onFailure.onInstallationSaveError(e);
                }
            }
        }, mInterval, TimeUnit.MILLISECONDS);
    }
}