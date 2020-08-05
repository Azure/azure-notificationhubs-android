package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;
import android.content.SharedPreferences;
import com.microsoft.windowsazure.messaging.R;

/**
 * Associates the User ID with any Installation that it visits.
 */
class UserIdVisitor implements InstallationVisitor {
    private static final String PREFERENCE_KEY = "userId";
    private final SharedPreferences mPreferences;

    public UserIdVisitor(Context context) {
        mPreferences = context.getSharedPreferences(context.getString(R.string.installation_enrichment_file_key), Context.MODE_PRIVATE);
    }

    /**
     *
     * @param subject The {@link Installation} to be updated to include an UserId.
     */
    @Override
    public void visitInstallation(Installation subject) {
        subject.setUserId(getUserId());
    }

    /**
     * Fetches the current UserId.
     * @return The unique id, null if it has not been set.
     */
    public String getUserId() {
        return mPreferences.getString(PREFERENCE_KEY, null);
    }

    /**
     * Updates the unique identifier that will be associated with future calls to `visitInstallation`.
     *
     * @param userId The unique identifier to apply.
     * @return True if user id was updated, False if current value is equal to the new value
     */
    public boolean setUserId(String userId) {
        if((userId != null && !userId.equals(getUserId()))
        || (userId == null && getUserId() != null)) {
            mPreferences.edit().putString(PREFERENCE_KEY, userId).apply();
            return true;
        }
        return false;
    }
}
