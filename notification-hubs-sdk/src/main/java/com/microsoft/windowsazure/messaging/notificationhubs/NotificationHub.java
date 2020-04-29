package com.microsoft.windowsazure.messaging.notificationhubs;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;

import com.microsoft.windowsazure.messaging.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A Singleton controller that wraps all interactions with Firebase Cloud Messaging and Azure
 * Notification Hubs.
 */
public final class NotificationHub {
    private static NotificationHub sInstance;

    private NotificationListener mListener;
    private final List<InstallationVisitor> mVisitors;
    private PushChannelVisitor mPushChannelVisitor;
    private TagVisitor mTagVisitor;
    private IdAssignmentVisitor mIdAssignmentVisitor;

    private InstallationAdapter mManager;
    private Application mApplication;
    private Class mReceiver;

    private boolean mIsEnabled = true;
    private SharedPreferences mPreferences;
    private static final String IS_ENABLED_PREFERENCE_KEY = "isEnabled";

    NotificationHub() {
        mVisitors = new ArrayList<>();
    }

    /**
     * Fetches the single instance of NotificationHub that has been created to suit the current
     * environment.
     * @return A shared instance of the NotificationHub class.
     */
    public static synchronized NotificationHub getInstance() {
        if (sInstance == null) {
            sInstance = new NotificationHub();
        }
        return sInstance;
    }

    /**
     * Initialize the single global instance of {@link NotificationHub} and configure to associate
     * this device with an Azure Notification Hub.
     * @param application The application that will own the lifecycle and resources that NotificationHub
     *                needs access to.
     * @param hubName The name of the Notification Hub that will broadcast notifications to this
     *                device.
     * @param connectionString The Listen-only AccessPolicy that grants this device the ability to
     *                         receive notifications.
     */
    public static void initialize(Application application, String hubName, String connectionString) {
        initialize(application, new DebounceInstallationAdapter(new NotificationHubInstallationAdapter(
                hubName,
                connectionString)));
    }

    /**
     * Initialize the single global instance of {@link NotificationHub} and configure to associate
     * this device with a custom backend that will store device references for future broadcasts.
     *
     * This is useful when your backend will exclusively use Notification Hub's direct send
     * functionality.
     * @param application The application that will own the lifecycle and resources that NotificationHub
     *                needs access to.
     * @param adapter A client that can create/overwrite a reference to this device with a backend.
     */
    public static void initialize(Application application, InstallationAdapter adapter) {
        NotificationHub instance = getInstance();
        instance.setInstanceInstallationAdapter(adapter);
        instance.mApplication = application;

        instance.mPreferences = instance.mApplication.getSharedPreferences(instance.mApplication.getString(R.string.installation_enrichment_file_key), Context.MODE_PRIVATE);

        instance.mIdAssignmentVisitor = new IdAssignmentVisitor(instance.mApplication);
        instance.useInstanceVisitor(instance.mIdAssignmentVisitor);

        instance.mTagVisitor = new TagVisitor(instance.mApplication);
        instance.useInstanceVisitor(instance.mTagVisitor);

        instance.mPushChannelVisitor = new PushChannelVisitor(instance.mApplication);
        instance.useInstanceVisitor(instance.mPushChannelVisitor);

        Intent i =  new Intent(application, FirebaseReceiver.class);
        application.startService(i);

        // Why is this done here instead of being in the manifest like everything else?
        // BroadcastReceivers are special, and starting in Android 8.0 the ability to start them
        // from the manifest was removed. See documentation from Google here:
        // https://developer.android.com/guide/components/broadcasts#android_80
        IntentFilter connectivityFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        connectivityFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        application.registerReceiver(new NetworkStatusReceiver(), connectivityFilter);
    }

    /**
     * Changes the callback that will be invoked when a notification is received.
     * @param listener A callback that will be invoked whenever your application is given access to
     *                 a notification.
     */
    public static void setListener(NotificationListener listener) {
        getInstance().setInstanceListener(listener);
    }

    /**
     * Captures notification activity that happened while your application was in the background.
     * @param activity TODO
     * @param intent TODO
     */
    public static void checkLaunchedFromNotification(Activity activity, Intent intent) {
        // TODO: Cache the activity and intent extras that were passed to us.
    }

    /**
     * Changes the callback that will be invoked when a notification is received.
     * @param listener A callback that will be invoked whenever your application is given access to
     *                 a notification.
     */
    public void setInstanceListener(NotificationListener listener) {
        mListener = listener;
    }

    /**
     * Registers {@link InstallationVisitor} for use when a new {@link Installation} is to be
     * created and registered.
     *
     * Visitors are applied in the order that they are added via calls to this method.
     *
     * @param visitor A {@link InstallationVisitor} to invoke when creating a new
     *                   {@link Installation}.
     */
    static void useVisitor(InstallationVisitor visitor) {
        getInstance().useInstanceVisitor(visitor);
    }

    /**
     * Registers an {@link InstallationVisitor} for use when a new {@link Installation} is to be
     * created and registered.
     *
     * Visitors are applied in the order that they are added via calls to this method.
     *
     * @param visitor A {@link InstallationVisitor} to invoke when creating a new
     *                   {@link Installation}.
     */
    void useInstanceVisitor(InstallationVisitor visitor) {
        mVisitors.add(visitor);
    }

    /**
     * Updates the mechanism that will be used to inform a backend service of the new installation.
     * @param adapter An instance of the {@link InstallationAdapter} that should be used.
     */
    public static void setInstallationAdapter(InstallationAdapter adapter) {
        getInstance().setInstanceInstallationAdapter(adapter);
    }

    /**
     * Updates the mechanism that will be used to inform a backend service of the new installation.
     * @param adapter An instance of the {@link InstallationAdapter} that should be used.
     */
    public void setInstanceInstallationAdapter(InstallationAdapter adapter) {
        this.mManager = adapter;
    }

    /**
     * Creates a new {@link Installation} and registers it with a backend that tracks devices.
     */
    public static void reinstall() {
        getInstance().reinstallInstance();
    }

    /**
     * Creates a new {@link Installation} and registers it with a backend that tracks devices.
     */
    public void reinstallInstance() {
        if (!isInstanceEnabled()) {
            return;
        }

        Installation installation = new Installation();
        for (InstallationVisitor visitor: mVisitors) {
            visitor.visitInstallation(installation);
        }

        if (mManager != null) {
            mManager.saveInstallation(mApplication, installation);
        }
    }

    static void setPushChannel(String token) {
        getInstance().setInstancePushChannel(token);
    }

    /**
     * Fetches the current Push Channel.
     * @return The current string that identifies this device as Push notification receiver. Null if
     *         it hasn't been initialized yet.
     */
    public static String getPushChannel() {
        return getInstance().getInstancePushChannel();
    }

    void setInstancePushChannel(String token) {
        mPushChannelVisitor.setPushChannel(token);
        reinstallInstance();
    }

    /**
     * Fetches the current Push Channel.
     * @return The current string that identifies this device as Push notification receiver. Null if
     *         it hasn't been initialized yet.
     */
    public String getInstancePushChannel() {
        return mPushChannelVisitor.getPushChannel();
    }

    /**
     * Fetches the InstallationId that will be assigned to future Installations that are created.
     * @return The unique ID associated with the record of this device.
     */
    public static String getInstallationId() {
        return getInstance().getInstanceInstallationId();
    }

    /**
     * Fetches the InstallationId that will be assigned to future Installations that are created.
     * @return The unique ID associated with the record of this device.
     */
    public String getInstanceInstallationId() {
        return mIdAssignmentVisitor.getInstallationId();
    }

    /**
     * Updates the unique identifier that will be associated with the record of this device.
     * @param id The value to treat as the unique identifier of the record of this device.
     */
    public static void setInstallationId(String id) {
        getInstance().setInstanceInstallationId(id);
    }

    /**
     * Updateds the unique identifier that will be associated with the record of this device.
     * @param id The value to treat as the unique identifier of the record of this device.
     */
    public void setInstanceInstallationId(String id) {
        mIdAssignmentVisitor.setInstallationId(id);
        reinstallInstance();
    }

    static void relayMessage(NotificationMessage message) {
        getInstance().relayInstanceMessage(message);
    }

    void relayInstanceMessage(NotificationMessage message) {
        mListener.onPushNotificationReceived(mApplication, message);
    }

    /**
     * Adds a single tag to this collection.
     *
     * @param tag The tag to include with this collection.
     * @return True if the provided tag was not previously associated with this collection.
     */
    public static boolean addTag(String tag) {
        return getInstance().addInstanceTag(tag);
    }

    /**
     * Adds a single tag to this collection.
     *
     * @param tag The tag to include with this collection.
     * @return True if the provided tag was not previously associated with this collection.
     */
    public boolean addInstanceTag(String tag) {
        if(mTagVisitor.addTag(tag)){
            reinstallInstance();
            return true;
        }
        return false;
    }

    /**
     * Adds several tags to the collection.
     *
     * @param tags The tags to include with this collection.
     * @return True if any of the provided tags had not previously been associated with this
     * Installation.
     */
    public static boolean addTags(Collection<? extends String> tags) {
       return getInstance().addInstanceTags(tags);
    }

    /**
     * Adds several tags to the collection.
     *
     * @param tags The tags to include with this collection.
     * @return True if any of the provided tags had not previously been associated with this
     * Installation.
     */
    public boolean addInstanceTags(Collection<? extends String> tags) {
        if(mTagVisitor.addTags(tags)) {
            reinstallInstance();
            return true;
        }
        return false;
    }

    /**
     * Deletes one tag from this collection.
     *
     * @param tag The tag that should no longer be in the collection.
     * @return True if the tag had previously been associated with this collection.
     */
    public static boolean removeTag(String tag) {
        return getInstance().removeInstanceTag(tag);
    }

    /**
     * Deletes one tag from this collection.
     *
     * @param tag The tag that should no longer be in the collection.
     * @return True if the tag had previously been associated with this collection.
     */
    public boolean removeInstanceTag(String tag) {
        if(mTagVisitor.removeTag(tag)) {
            reinstallInstance();
            return true;
        }
        return false;
    }

    /**
     * Deletes several tags from this collection.
     *
     * @param tags The tags that should no longer be in the collection.
     * @return True if any of the tags had previously been associated with this collection.
     */
    public static boolean removeTags(Collection<? extends String> tags) {
        return getInstance().removeInstanceTags(tags);
    }

    /**
     * Deletes several tags from this collection.
     *
     * @param tags The tags that should no longer be in the collection.
     * @return True if any of the tags had previously been associated with this collection.
     */
    public boolean removeInstanceTags(Collection<? extends String> tags) {
        if(mTagVisitor.removeTags(tags)) {
            reinstallInstance();
            return true;
        }
        return false;
    }

    /**
     * Fetches the tags associated with this collection.
     *
     * @return A set of tags.
     */
    public static Iterable<String> getTags() {
        return getInstance().getInstanceTags();
    }

    /**
     * Fetches the tags associated with this collection.
     *
     * @return A set of tags.
     */
    public Iterable<String> getInstanceTags() {
        return mTagVisitor.getTags();
    }

    /**
     * Empties the collection of tags.
     */
    public static void clearTags() {
       getInstance().clearInstanceTags();
    }

    /**
     * Empties the collection of tags.
     */
    public void clearInstanceTags() {
        if (mTagVisitor.getTags().iterator().hasNext()) {
            mTagVisitor.clearTags();
            this.reinstallInstance();
        }
    }

    /**
     * Controls whether or not this application should be listening for Notifications.
     * @param enable true if the application should be listening for notifications, false if not.
     */
    public static void setEnabled(boolean enable) {
        getInstance().setInstanceEnabled(enable);
    }

    /**
     * Controls whether or not this application should be listening for Notifications.
     * @param enable true if the application should be listening for notifications, false if not.
     */
    public void setInstanceEnabled(boolean enable) {
        mPreferences.edit().putBoolean(IS_ENABLED_PREFERENCE_KEY, enable).apply();
        if (enable) {
            reinstallInstance();
        }
    }

    public static boolean isEnabled() {
        return getInstance().isInstanceEnabled();
    }

    public boolean isInstanceEnabled() {
        return mPreferences.getBoolean(IS_ENABLED_PREFERENCE_KEY, true);
    }
}
