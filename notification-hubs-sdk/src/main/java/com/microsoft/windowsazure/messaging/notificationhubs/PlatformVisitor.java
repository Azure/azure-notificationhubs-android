package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;
import android.content.SharedPreferences;

import com.microsoft.windowsazure.messaging.R;

public class PlatformVisitor implements InstallationVisitor {
    private static final String PREFERENCE_KEY = "platform";
    private final SharedPreferences mPreferences;

    public PlatformVisitor(Context context) {
        mPreferences = context.getSharedPreferences(context.getString(R.string.installation_enrichment_file_key), Context.MODE_PRIVATE);
    }

    /**
     * Updates an {@link Installation} with the selected platform
     * @param subject The {@link Installation} that should be modified to include more detail.
     */
    @Override
    public void visitInstallation(Installation subject) {
        String selectedPlatform = getPlatform();
        if (selectedPlatform == null) {
            return;
        }
        
        subject.setPlatform(selectedPlatform);
    }

    /**
     * Updates the platform that will be applied to future {see enrichInstallation} calls.
     * @param platform The new platform to apply.
     */
    public void setPlatform(String platform) {
        mPreferences.edit().putString(PREFERENCE_KEY, platform).apply();
    }

    /**
     * Fetches the current Platform.
     * @return The current string that identifies the platform for this device. Null if
     *         it hasn't been initialized yet.
     */
    public String getPlatform() {
        return mPreferences.getString(PREFERENCE_KEY, null);
    }
}
