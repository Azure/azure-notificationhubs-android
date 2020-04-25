package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;
import android.content.SharedPreferences;
import com.microsoft.windowsazure.messaging.R;
import java.util.UUID;

class IdAssignmentVisitor implements InstallationVisitor {
    private static final String PREFERENCE_KEY = "installationId";
    private final SharedPreferences mPreferences;

    public IdAssignmentVisitor(Context context) {
        mPreferences = context.getSharedPreferences(context.getString(R.string.installation_enrichment_file_key), Context.MODE_PRIVATE);
    }

    /**
     * Ensures that an {@link Installation} has a unique identifier associated with it before it is
     * sent to the server.
     *
     * In no unique id has been set by the time this enrichment has been called, a UUID will be
     * generated and used as the unique identifier. That UUID is preserved between calls to this
     * method.
     *
     * @param subject The {@link Installation} to be updated to include an InstallationId.
     */
    @Override
    public void visitInstallation(Installation subject) {
        if(subject.getInstallationId() == null) {
            String id = this.getInstallationId();
            if (id == null) {
                id = UUID.randomUUID().toString();
                this.setInstallationId(id);
            }
            subject.setInstallationId(id);
        }
    }

    /**
     * Fetches the unique id that should be applied to installations from this device.
     * @return The unique id to associate with this device, null if it has not been set.
     */
    public String getInstallationId() {
        return mPreferences.getString(PREFERENCE_KEY, null);
    }

    /**
     * Updates the UUID that will be associated with future calls to `enrichInstallation`.
     * @param installationId The unique identifier to apply.
     */
    public void setInstallationId(String installationId) {
        mPreferences.edit().putString(PREFERENCE_KEY, installationId).apply();
    }
}
