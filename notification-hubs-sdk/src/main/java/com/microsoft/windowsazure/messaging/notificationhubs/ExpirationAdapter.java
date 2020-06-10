package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;
import android.content.SharedPreferences;

import com.microsoft.windowsazure.messaging.R;

import java.util.Date;
import java.util.Objects;

/**
 * A InstallationAdapter decorator which works in tandem with an {@link ExpirationVisitor} to ensure
 * that Installations have an appropriate expiration associated with them. It handles all of the
 * logic surrounding whether it is appropriate or not to renew an {@link Installation} with a
 * backend.
 *
 * To make its determination, some of the factors it will consider are:
 *  - Have there been any recent changes to the Installation?
 *  - Will the Installation be expiring soon?
 */
class ExpirationAdapter implements InstallationAdapter {
    private final SharedPreferences mSharedPreferences;
    private final InstallationAdapter mDecoratedAdapter;
    private final ExpirationVisitor mExpirationVisitor;
    private long mInstallationValidWindow;
    private long mNearExpirationWindow;

    /**
     * The number of milliseconds in a day for the sake of arithmetic.
     */
    static final long DAY_MILLIS = 24 * 60 * 60 * 1000;

    /**
     * The number of milliseconds that an {@link Installation} should be considered valid by
     * default.
     */
    static final long INSTALLATION_VALID_WINDOW = 30 * DAY_MILLIS;

    /**
     * The number of milliseconds that define when an {@link Installation} is near enough expiring
     * to justify renewing it by default.
     */
    static final long INSTALLATION_NEAR_EXPIRATION_WINDOW = 7 * DAY_MILLIS;

    /**
     * The key associated with the hash for determining if any other properties of an {@link Installation}
     * have changed.
     */
    static final String EXPIRATION_INSTALLATION_HASH_KEY = "expirationHash";

    /**
     * The key associated with the most recently used expiration.
     */
    static final String PREVIOUS_EXPIRATION_KEY = "previousExpiration";

    /**
     * Creates a new instance of {@link ExpirationAdapter} which will use the provided dependencies
     * as it determines whether or not to
     * @param context
     * @param decoratedAdapter
     * @param expirationVisitor
     */
    public ExpirationAdapter(Context context, InstallationAdapter decoratedAdapter, ExpirationVisitor expirationVisitor) {
        mDecoratedAdapter = decoratedAdapter;
        mExpirationVisitor = expirationVisitor;
        mSharedPreferences = context.getSharedPreferences(context.getString(R.string.installation_enrichment_file_key), Context.MODE_PRIVATE);
        mInstallationValidWindow = INSTALLATION_VALID_WINDOW;
        mNearExpirationWindow = INSTALLATION_NEAR_EXPIRATION_WINDOW;
    }

    /**
     * Updates a backend with the updated Installation information for this device.
     *
     * @param installation            The record to update.
     * @param onInstallationSaved     The callback that will be invoked when an {@link Installation}
     *                                was successfully saved.
     * @param onInstallationSaveError The callback that will be invoked when there is a problem
     *                                saving an {@link Installation}.
     */
    @Override
    public void saveInstallation(Installation installation, Listener onInstallationSaved, ErrorListener onInstallationSaveError) {
        if (
            installation.getExpiration() == getPreviousExpiration() && // If a separate component is trying to update the expiration, don't interfere.
            (
                installation.getExpiration() == null || // If no one has applied an expiration, we should.
                hasInstallationChanged(installation) || // We won't be adding a new call to the backend, so we may as well update the expiration.
                nearOrPastExpiration(installation) // Nothing else has changed, but the application is running and the Installation will expire soon.
            )
        ) {
            Date expiration = getExpirationDate();
            installation.setExpiration(expiration);
            mExpirationVisitor.setExpiration(expiration);
        }

        int hash = getInstallationHash(installation);
        mSharedPreferences.edit().putInt(EXPIRATION_INSTALLATION_HASH_KEY, hash).apply();
        mSharedPreferences.edit().putLong(PREVIOUS_EXPIRATION_KEY, installation.getExpiration().getTime()).apply();
        mDecoratedAdapter.saveInstallation(installation, onInstallationSaved, onInstallationSaveError);
    }

    private boolean hasInstallationChanged(Installation installation) {
        final int notSeen = Integer.MIN_VALUE;
        int previousHash = mSharedPreferences.getInt(EXPIRATION_INSTALLATION_HASH_KEY, notSeen);
        if (previousHash == notSeen) {
            return installation == null;
        }
        if (installation == null) {
            return true;
        }

        int expectedHash = getInstallationHash(installation);
        return expectedHash != previousHash;
    }

    private Date getPreviousExpiration() {
        final long notPresent = Long.MIN_VALUE;
        long previousExpiration = mSharedPreferences.getLong(PREVIOUS_EXPIRATION_KEY, notPresent);

        if (previousExpiration == notPresent) {
            return null;
        }
        return new Date(previousExpiration);
    }

    /**
     * Determines whether or not an {@link Installation} is near enough its expiration to renew it.
     * @param installation The {@link Installation} to be checked.
     * @return True if the given {@link Installation} is expired or is within the `mNearExpirationWindow`.
     */
    boolean nearOrPastExpiration(Installation installation) {
        return nearOrPastExpiration(installation, new Date());
    }

    /**
     * Determines whether or not an {@link Installation} is near enough its expiration to renew it.
     * This method may be used for testing, it may be used to keep date/times consistent.
     *
     * @param installation The {@link Installation} to be checked.
     * @param now The moment in time that should be considered the current time.
     *
     * @return True if the given {@link Installation} is expired or is within the `mNearExpirationWindow`.
     */
    boolean nearOrPastExpiration(Installation installation, Date now) {
        Date currentExpiration = installation.getExpiration();
        if (currentExpiration == null) {
            return false;
        }

        return currentExpiration.getTime() - now.getTime() <= mNearExpirationWindow;
    }

    /**
     * Generates a hash of an {@link Installation} that only includes components that are relevant
     * to applying a new expiration. Note this is an independent evaluation from the more generic
     * {@link DebounceInstallationAdapter}, because it may or may not include a previously used
     * expirationTime in its evaluation.
     *
     * @param subject The {@link Installation} which may need a new expiration time.
     * @return A semi-unique number associated with this hash for the purposes of deduplication.
     */
    public static int getInstallationHash(Installation subject) {
        // Expiration is the only deliberately omitted property of Installation below. We are
        // trying to determine if the `Installation` is about to get changed anyway, regardless of
        // what we do to it.
        return Objects.hash(
                subject.getTags(),
                subject.getTemplates(),
                subject.getInstallationId(),
                subject.getPushChannel()
        );
    }

    /**
     * Finds the moment an {@link Installation} should expire, assuming it is created now.
     * @return The moment the @{link Installation} should expire.
     */
    Date getExpirationDate() {
        return getExpirationDate(new Date());
    }

    /**
     * Given the beginning of a window where an {@link Installation} should be considered valid,
     * find the moment when it would be expired.
     * @param windowStart The beginning of the window where an {@link Installation} should be
     *                    considered valid.
     * @return The moment the @{link Installation} should expire.
     */
    Date getExpirationDate(Date windowStart) {
        return new Date(windowStart.getTime() + mInstallationValidWindow);
    }
}
