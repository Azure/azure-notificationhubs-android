package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;
import android.content.SharedPreferences;

import com.microsoft.windowsazure.messaging.R;

import java.util.Calendar;
import java.util.Date;

/**
 * Assigns a given moment-in-time as an expiration to instances of {@link Installation} that it
 * visits.
 */
class ExpirationVisitor implements InstallationVisitor {
    private final SharedPreferences mPreferences;
    private static final String EXPIRATION_PREFERENCE_KEY = "expiration";

    /**
     * Creates a new ExpirationVisitor, which will mark each {@link Installation} that it sees as
     * being valid for the default amount of time.
     *
     * @param context The application context that this instance should extract preferences from.
     */
    public ExpirationVisitor(Context context) {
        mPreferences = context.getSharedPreferences(context.getString(R.string.installation_enrichment_file_key), Context.MODE_PRIVATE);
    }

    /**
     * Updates an Installation to include an expiration field that will be communicated to the backend.
     * @param subject The {@link Installation} that should be modified to include more detail.
     */
    @Override
    public void visitInstallation(Installation subject) {
        subject.setExpiration(getExpiration());
    }

    /**
     * Fetches the current point in time that should be applied as the expiration for instances of {@link Installation}.
     * @return An initialized Date if any expiration has been set, null otherwise.
     */
    public Date getExpiration() {
        final long emptyExpiration = Long.MIN_VALUE;
        long rawExpiration = mPreferences.getLong(EXPIRATION_PREFERENCE_KEY, emptyExpiration);
        if (rawExpiration == emptyExpiration) {
            return null;
        }
        return new Date(rawExpiration);
    }

    /**
     * Sets the current point in time that should be applied to future instances of {@link Installation}.
     * @param expiration The moment in time that should be applied as a the expiration, or `null` if
     *                   no such time
     */
    public void setExpiration(Date expiration) {
        if (expiration == null) {
            mPreferences.edit().remove(EXPIRATION_PREFERENCE_KEY).apply();
            return;
        }

        long rawExpiration = expiration.getTime();
        mPreferences.edit().putLong(EXPIRATION_PREFERENCE_KEY, rawExpiration).apply();
    }
}

