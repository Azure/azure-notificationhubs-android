package com.microsoft.windowsazure.messaging.notificationhubs;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.util.Log;

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
    private TemplateVisitor mTemplateVisitor;
    private IdAssignmentVisitor mIdAssignmentVisitor;
    private UserIdVisitor mUserIdVisitor;

    private InstallationAdapter mAdapter;
    private Application mApplication;

    private SharedPreferences mPreferences;
    private static final String IS_ENABLED_PREFERENCE_KEY = "isEnabled";

    private InstallationAdapter.Listener mOnSavedInstallation;
    private InstallationAdapter.ErrorListener mOnInstallationFailure;

    NotificationHub() {
        mVisitors = new ArrayList<>();

        mOnInstallationFailure = new InstallationAdapter.ErrorListener() {
            @Override
            public void onInstallationSaveError(Exception e) {
                Log.e("ANH", "unable to save installation: " + e.toString());
            }
        };
        mOnSavedInstallation = new InstallationAdapter.Listener() {
            @Override
            public void onInstallationSaved(Installation i) {
                Log.i("ANH", "updated installation");
            }
        };
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

     synchronized void registerApplication(Application application) {
        if (mApplication == application) {
            return;
        }

        mApplication = application;

        mPreferences = mApplication.getSharedPreferences(mApplication.getString(R.string.installation_enrichment_file_key), Context.MODE_PRIVATE);

        mIdAssignmentVisitor = new IdAssignmentVisitor(mApplication);
        useInstanceVisitor(mIdAssignmentVisitor);

        mTagVisitor = new TagVisitor(mApplication);
        useInstanceVisitor(mTagVisitor);

        mTemplateVisitor = new TemplateVisitor(mApplication);
        useInstanceVisitor(mTemplateVisitor);

        mPushChannelVisitor = new PushChannelVisitor(mApplication);
        useInstanceVisitor(mPushChannelVisitor);

        mUserIdVisitor = new UserIdVisitor(mApplication);
        useInstanceVisitor(mUserIdVisitor);
    }

    /**
     * Initialize the single global instance of {@link NotificationHub} and configure to associate
     * this device with an Azure Notification Hub.
     * @param application The application that will own the lifecycle and resources that NotificationHub
     *                needs access to.
     * @param hubName The name of the Notification Hub that will broadcast notifications to this
     *                device.
     * @param connectionString The Listen-only AccessPolicy that grants this device the ability to
     */
    public static void start(Application application, String hubName, String connectionString) {
        InstallationAdapter client = new NotificationHubInstallationAdapter(
                application,
                hubName,
                connectionString);
        InstallationAdapter debouncer = new DebounceInstallationAdapter(application, client);
        InstallationAdapter pushChannelAdapter = new PushChannelValidationAdapter(debouncer);

        start(application, pushChannelAdapter);
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
    public static void start(Application application, InstallationAdapter adapter) {
        final NotificationHub instance = getInstance();
        instance.mAdapter = adapter;

        instance.registerApplication(application);

        // Why is this done here instead of being in the manifest like everything else?
        // BroadcastReceivers are special, and starting in Android 8.0 the ability to start them
        // from the manifest was removed. See documentation from Google here:
        // https://developer.android.com/guide/components/broadcasts#android_80
        IntentFilter connectivityFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        connectivityFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        NetworkStateHelper.getSharedInstance(application).addListener(new NetworkStateHelper.Listener() {
            @Override
            public void onNetworkStateUpdated(boolean connected) {
                if (connected) {
                    instance.beginInstanceInstallationUpdate();
                }
            }
        });
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
     * Changes the callback that will be invoked when a notification is received.
     * @param listener A callback that will be invoked whenever your application is given access to
     *                 a notification.
     */
    public void setInstanceListener(NotificationListener listener) {
        mListener = listener;
    }

    NotificationListener getInstanceListener() {
        return mListener;
    }

    public static void setInstallationSavedListener(InstallationAdapter.Listener listener) {
        getInstance().setInstanceInstallationSavedListener(listener);
    }

    public void setInstanceInstallationSavedListener(InstallationAdapter.Listener listener) {
        mOnSavedInstallation = listener;
    }

    public static void setInstallationSaveFailureListener(InstallationAdapter.ErrorListener listener) {
        getInstance().setInstanceInstallationSaveFailureListener(listener);
    }

    public void setInstanceInstallationSaveFailureListener(InstallationAdapter.ErrorListener listener) {
        mOnInstallationFailure = listener;
    }

    /**
     * Captures notification activity that happened while your application was in the background.
     * @param activity TODO
     * @param intent TODO
     */
    static void checkLaunchedFromNotification(Activity activity, Intent intent) {
        // TODO: Cache the activity and intent extras that were passed to us.
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
    public static void useVisitor(InstallationVisitor visitor) {
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
    public void useInstanceVisitor(InstallationVisitor visitor) {
        mVisitors.add(visitor);
    }

    /**
     * Creates a new {@link Installation} and registers it with a backend that tracks devices.
     */
    public static void beginInstallationUpdate() {
        getInstance().beginInstanceInstallationUpdate();
    }

    /**
     * Creates a new {@link Installation} and registers it with a backend that tracks devices.
     */
    public void beginInstanceInstallationUpdate() {
        if (!isInstanceEnabled()) {
            return;
        }

        Installation installation = new Installation();
        for (InstallationVisitor visitor: mVisitors) {
            visitor.visitInstallation(installation);
        }

        if (mAdapter != null) {
            mAdapter.saveInstallation(installation, mOnSavedInstallation, mOnInstallationFailure);
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
        if (token.equals(mPushChannelVisitor.getPushChannel())) {
            Log.i("ANH", "requested push channel matches previous record, no operation necessary");
            return;
        }
        mPushChannelVisitor.setPushChannel(token);
        Log.i("ANH", "updated local record of push channel");
        beginInstanceInstallationUpdate();
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
     * Updates the unique identifier that will be associated with the record of this device.
     * @param id The value to treat as the unique identifier of the record of this device.
     */
    public void setInstanceInstallationId(String id) {
        if (id.equals(mIdAssignmentVisitor.getInstallationId())) {
            return;
        }

        mIdAssignmentVisitor.setInstallationId(id);
        beginInstanceInstallationUpdate();
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
            beginInstanceInstallationUpdate();
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
            beginInstanceInstallationUpdate();
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
            beginInstanceInstallationUpdate();
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
            beginInstanceInstallationUpdate();
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
            this.beginInstanceInstallationUpdate();
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
            beginInstanceInstallationUpdate();
        }
    }

    public static boolean isEnabled() {
        return getInstance().isInstanceEnabled();
    }

    public boolean isInstanceEnabled() {
        return mPreferences.getBoolean(IS_ENABLED_PREFERENCE_KEY, true);
    }

    /**
     * Add or update template in the collection
     *
     * @param templateName Name of template
     * @param template Template instance
     */
    public static void setTemplate(String templateName, InstallationTemplate template) {
        getInstance().setInstanceTemplate(templateName, template);
    }

    public void setInstanceTemplate(String templateName, InstallationTemplate template) {
        mTemplateVisitor.setTemplate(templateName, template);
        beginInstanceInstallationUpdate();
    }

    /**
     * Remove template from collection
     *
     * @param templateName The name of template that should no longer be in the collection.
     * @return True if any of the templates had previously been associated with this name.
     */
    public static boolean removeTemplate(String templateName) {
        return getInstance().removeInstanceTemplate(templateName);
    }

    public boolean removeInstanceTemplate(String templateName) {
        if(mTemplateVisitor.removeTemplate(templateName)){
            beginInstanceInstallationUpdate();
            return true;
        }
        return false;
    }

    /**
     *
     * @param templateName Name of template
     * @return Instance of template associated with name
     */
    public static InstallationTemplate getTemplate(String templateName) {
        return getInstance().getInstanceTemplate(templateName);
    }

    public InstallationTemplate getInstanceTemplate(String templateName) {
        return mTemplateVisitor.getTemplate(templateName);
    }

    /**
     * Updates the UserID that will be associated with this device.
     *
     * @param userId The UserID to associate with the device.
     * @return True if user id was updated, False if current value is equal to the new value
     */
    public static boolean setUserId(String userId) {
        return getInstance().setInstanceUserId(userId);
    }

    /**
     * Updates the UserID that will be associated with this device.
     *
     * @param userId The UserID to associate with the device.
     * @return True if user id was updated, False if current value is equal to the new value
     */
    public boolean setInstanceUserId(String userId) {
        if(mUserIdVisitor.setUserId(userId)) {
            beginInstanceInstallationUpdate();
            return true;
        }
        return false;
    }

    /**
     * Fetches the current User Id.
     *
     * @return The user Id currently associated with this device. Null if none is currently set.
     */
    public static String getUserId() {
        return getInstance().getInstanceUserId();
    }

    /**
     * Fetches the current User Id.
     *
     * @return The user Id currently associated with this device. Null if none is currently set.
     */
    public String getInstanceUserId() {
        return mUserIdVisitor.getUserId();
    }
}
