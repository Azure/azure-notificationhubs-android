package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;
import android.content.SharedPreferences;

import com.microsoft.windowsazure.messaging.R;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Protects the {@link InstallationAdapter} from rapid changes to the current Installation, as well
 * as weeding out calls that match the last request that was sent to the server.
 */
public class DebounceInstallationAdapter implements InstallationAdapter {

    static final String LAST_ACCEPTED_HASH_KEY = "lastAcceptedHash";
    static final String LAST_ACCEPTED_TIMESTAMP_KEY= "lastAcceptedTimestamp";
    private static final long DEFAULT_DEBOUNCE_INTERVAL = 2000L; // Two seconds
    private static final long DEFAULT_INSTALLATION_STALE_MILLIS = 1000L * 60L * 60L * 24L; // One day's worth of milliseconds

    private final ScheduledExecutorService mScheduler = Executors.newScheduledThreadPool(1);
    private InstallationAdapter mInstallationAdapter;
    private long mInterval;
    private ScheduledFuture<?> mSchedFuture;
    private SharedPreferences mPreferences;
    private long mInstallationStaleMillis;

    /**
     * Creates a new instance which decorates a given {@link InstallationAdapter} with all default
     * settings.
     * @param context The Application context that can be used for caching information about
     *                previous sessions.
     * @param installationAdapter The adapter that should be invoked once after the waiting period
     *                            is complete.
     */
    public DebounceInstallationAdapter(Context context, InstallationAdapter installationAdapter) {
        this(context, installationAdapter, DEFAULT_DEBOUNCE_INTERVAL);
    }

    /**
     * Creates a new instance which decorates a given {@link InstallationAdapter}, and waits at least
     * a specified number of milliseconds between calls to the server.
     *
     * @param context The Application context that can be used for caching information about
     *                previous sessions.
     * @param installationAdapter The adapter that should be invoked once after the waiting period
     *                            is complete.
     * @param interval The number of milliseconds to wait for further changes to accumulate before
     *                 passing the {@link Installation} to the next adapter.
     */
    public DebounceInstallationAdapter(Context context, InstallationAdapter installationAdapter, long interval) {
        this(
            installationAdapter,
            interval,
            context.getSharedPreferences(context.getString(R.string.installation_enrichment_file_key), Context.MODE_MULTI_PROCESS)
        );
    }

    DebounceInstallationAdapter(InstallationAdapter installationAdapter, long interval, SharedPreferences sharedPreferences) {
        super();
        mInstallationAdapter = installationAdapter;
        mInterval = interval;
        mInstallationStaleMillis = DEFAULT_INSTALLATION_STALE_MILLIS;
        mPreferences = sharedPreferences;
    }

    /**
     * Sets the maximum amount of time that this instance will wait before allowing what would have
     * otherwise been a duplicate {@link Installation} through.
     * @param millis The number of milliseconds before an {@link Installation} should be considered stale.
     */
    void setInstallationStaleWindow(long millis) {
        mInstallationStaleMillis = millis;
    }

    @Override
    public synchronized void saveInstallation(final Installation installation, final Listener onInstallationSaved, final ErrorListener onInstallationSaveError) {
        if (mSchedFuture != null && !mSchedFuture.isDone()) {
            mSchedFuture.cancel(true);
        }

        final int currentHash = installation.hashCode();
        int recentHash = getLastAcceptedHash();

        boolean sameAsLastAccepted = recentHash != 0 && recentHash == currentHash;
        final long currentTime = new Date().getTime();
        boolean lastAcceptedIsRecent =  currentTime < getLastAcceptedTimestamp() + mInstallationStaleMillis;

        if (sameAsLastAccepted && lastAcceptedIsRecent) {
            return;
        }

        mSchedFuture = mScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    Listener completed = new Listener() {
                        @Override
                        public void onInstallationSaved(Installation i) {
                            mPreferences.edit().putInt(LAST_ACCEPTED_HASH_KEY, currentHash).apply();
                            mPreferences.edit().putLong(LAST_ACCEPTED_TIMESTAMP_KEY, currentTime).apply();
                            onInstallationSaved.onInstallationSaved(i);
                        }
                    };
                    mInstallationAdapter.saveInstallation(installation, completed, onInstallationSaveError);
                } catch (Exception e) {
                    onInstallationSaveError.onInstallationSaveError(e);
                }
            }
        }, mInterval, TimeUnit.MILLISECONDS);
    }

    private long getLastAcceptedTimestamp() {
        return mPreferences.getLong(LAST_ACCEPTED_TIMESTAMP_KEY, Long.MIN_VALUE);
    }

    private int getLastAcceptedHash() {
        return mPreferences.getInt(LAST_ACCEPTED_HASH_KEY, 0);
    }
}