package com.microsoft.windowsazure.messaging.notificationhubs;

/**
 * This class shields backends from {@link Installation} records that are not targetable. Any record
 * that has a unique device ID (PushChannel) is allowed through without interference.
 */
public class PushChannelValidationAdapter implements InstallationAdapter {
    /**
     * The number of save attempts to ignore, if no value is specified to the constructor.
     */
    public static final int DEFAULT_MAX_RETRIES = 100;

    private final InstallationAdapter mDecoratedAdapter;
    private final int mMaxRetries;
    private int mRetryCount;

    /**
     * Creates a new instance of PushChannelValidationAdapter.
     * @param decoratedAdapter The adapter that will be called if the {@link Installation} passes
     *                         validation.
     */
    public PushChannelValidationAdapter(InstallationAdapter decoratedAdapter) {
        this(decoratedAdapter, DEFAULT_MAX_RETRIES);
    }

    /**
     * Creates a new instance of PushChannelValidationAdapter.
     * @param decoratedAdapter The adapter that will be called if the {@link Installation} passes
     *                         validation.
     * @param maxRetries The maximum number of save requests to ignore from failed validation,
     *                   before invoking the onInstallationSaveError callback.
     */
    public PushChannelValidationAdapter(InstallationAdapter decoratedAdapter, int maxRetries){
        mMaxRetries = maxRetries;
        mRetryCount = 0;
        mDecoratedAdapter = decoratedAdapter;
    }

    /**
     * Updates a backend with the updated Installation information for this device.
     *
     * @param installation            The record to update.
     * @param onInstallationSaved     Installation saved listener.
     * @param onInstallationSaveError Installation save error listener.
     */
    @Override
    public void saveInstallation(Installation installation, Listener onInstallationSaved, ErrorListener onInstallationSaveError) {
        String pushChannel = installation.getPushChannel();
        if (pushChannel == null || pushChannel.isEmpty()) {
            // No reason to proceed with save attempt if there's no PushChannel.
            if (mRetryCount++ >= mMaxRetries) {
                // It can take a moment to retrieve the Push Channel from the operating system. The
                // first few times we encounter an Installation without a PushChannel, we should just
                // ignore the operation altogether.
                mRetryCount = 0;
                onInstallationSaveError.onInstallationSaveError(new IllegalArgumentException("After " + mRetryCount + " retry attempts, Installation does not have a PushChannel."));
            }
            return;
        }

        mRetryCount = 0;
        mDecoratedAdapter.saveInstallation(installation, onInstallationSaved, onInstallationSaveError);
    }
}
