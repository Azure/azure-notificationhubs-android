package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;
import android.content.SharedPreferences;

import com.microsoft.windowsazure.messaging.R;

import java.util.Map;
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
    private ScheduledFuture<?> mSaveSchedFuture;
    private Map<String, ScheduledFuture<?>> mDeleteSchedFutures;
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
    public void saveInstallation(final Installation installation, final SaveListener onInstallationSaved, final ErrorListener onInstallationSaveError) {
        if (mSaveSchedFuture != null && !mSaveSchedFuture.isDone()) {
            mSaveSchedFuture.cancel(true);
        }

        int recentHash = mPreferences.getInt(PREFERENCE_KEY, 0);
        if (recentHash != 0 && recentHash == installation.hashCode()) {
            return;
        }

        mSaveSchedFuture = mScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    mInstallationAdapter.saveInstallation(installation, onInstallationSaved, onInstallationSaveError);
                    mPreferences.edit().putInt(PREFERENCE_KEY, installation.hashCode()).apply();
                } catch (Exception e) {
                    onInstallationSaveError.onInstallationOperationError(e);
                }
            }
        }, mInterval, TimeUnit.MILLISECONDS);
    }

    /**
     * Updates a backend to remove the references to the specified {@link Installation}.
     *
     * @param id                        The unique identifier associated with the {@link Installation} to be removed.
     * @param onInstallationDeleted     A callback which will be invoked if the {@link Installation} is
     *                                  successfully deleted.
     * @param onInstallationDeleteError A callback which will be invoked if the {@link Installation}
     */
    @Override
    public void deleteInstallation(final String id, final DeleteListener onInstallationDeleted, final ErrorListener onInstallationDeleteError) {
        ScheduledFuture<?> idFuture = mDeleteSchedFutures.get(id);

        if (idFuture != null && !idFuture.isDone()) {
            idFuture.cancel(true);
        }

        idFuture = mScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    mInstallationAdapter.deleteInstallation(id, onInstallationDeleted, onInstallationDeleteError);
                    mDeleteSchedFutures.remove(id);
                } catch (Exception e) {
                    onInstallationDeleteError.onInstallationOperationError(e);
                }
            }
        }, mInterval, TimeUnit.MILLISECONDS);
    }
}