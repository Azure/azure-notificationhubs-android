package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.core.graphics.drawable.IconCompat;

import com.microsoft.windowsazure.messaging.R;

public class PushChannelVisitor implements InstallationVisitor {
    private static final String PREFERENCE_KEY = "pushChannel";
    private final SharedPreferences mPreferences;

    public PushChannelVisitor(Context context) {
        mPreferences = context.getSharedPreferences(context.getString(R.string.installation_enrichment_file_key), Context.MODE_PRIVATE);
    }

    /**
     * Updates an {@link Installation} with the unique identifier that marks this device for
     * notification delivery purposes.
     * @param subject The {@link Installation} that should be modified to include more detail.
     */
    @Override
    public void visitInstallation(Installation subject) {
        subject.setPushChannel(getPushChannel());
    }

    /**
     * Updates the unique identifier that will be applied to future {see enrichInstallation} calls.
     * @param channel The new unique identifier to apply.
     */
    public void setPushChannel(String channel) {
        mPreferences.edit().putString(PREFERENCE_KEY, channel).apply();
    }

    /**
     * Fetches the current Push Channel.
     * @return The current string that identifies this device as Push notification receiver. Null if
     *         it hasn't been initialized yet.
     */
    public String getPushChannel() {
        return mPreferences.getString(PREFERENCE_KEY, null);
    }
}
