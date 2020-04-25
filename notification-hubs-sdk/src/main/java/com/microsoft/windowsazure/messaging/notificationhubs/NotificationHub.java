package com.microsoft.windowsazure.messaging.notificationhubs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.iid.FirebaseInstanceId;

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

    private InstallationManager mManager;
    private Context mContext;

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
     * @param context The application that will own the lifecycle and resources that NotificationHub
     *                needs access to.
     * @param hubName The name of the Notification Hub that will broadcast notifications to this
     *                device.
     * @param connectionString The Listen-only AccessPolicy that grants this device the ability to
     *                         receive notifications.
     */
    public static void initialize(Context context, String hubName, String connectionString) {
        initialize(context, new DebounceInstallationManager(new NotificationHubInstallationManager(
                hubName,
                connectionString)));
    }

    /**
     * Initialize the single global instance of {@link NotificationHub} and configure to associate
     * this device with a custom backend that will store device references for future broadcasts.
     *
     * This is useful when your backend will exclusively use Notification Hub's direct send
     * functionality.
     * @param context The application that will own the lifecycle and resources that NotificationHub
     *                needs access to.
     * @param manager A client that can create/overwrite a reference to this device with a backend.
     */
    public static void initialize(Context context, InstallationManager manager) {
        NotificationHub instance = getInstance();
        instance.setInstanceInstallationManager(manager);
        instance.mContext = context.getApplicationContext();

        instance.mIdAssignmentVisitor = new IdAssignmentVisitor(instance.mContext);
        instance.useInstanceVisitor(instance.mIdAssignmentVisitor);

        instance.mTagVisitor = new TagVisitor(instance.mContext);
        instance.useInstanceVisitor(instance.mTagVisitor);

        instance.mPushChannelVisitor = new PushChannelVisitor(instance.mContext);
        instance.useInstanceVisitor(instance.mPushChannelVisitor);

        instance.useInstanceVisitor(instance.mPushChannelVisitor);

        Intent i =  new Intent(context, FirebaseReceiver.class);
        context.startService(i);
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
     * @param visitor A {@link InstallationVisitor} to invoke when creating a new
     *                   {@link Installation}.
     */
    public static void useVisitor(InstallationVisitor visitor) {
        getInstance().useInstanceVisitor(visitor);
    }

    /**
     * Registers an {@link InstallationVisitor} for use when a new {@link Installation} is to be
     * created and registered.
     * @param visitor A {@link InstallationVisitor} to invoke when creating a new
     *                   {@link Installation}.
     */
    public void useInstanceVisitor(InstallationVisitor visitor) {
        mVisitors.add(visitor);
    }

    /**
     * Updates the mechanism that will be used to inform a backend service of the new installation.
     * @param manager An instance of the {@link InstallationManager} that should be used.
     */
    public static void setInstallationManger(InstallationManager manager) {
        getInstance().setInstanceInstallationManager(manager);
    }

    /**
     * Updates the mechanism that will be used to inform a backend service of the new installation.
     * @param manager An instance of the {@link InstallationManager} that should be used.
     */
    public void setInstanceInstallationManager(InstallationManager manager) {
        this.mManager = manager;
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
        Installation installation = new Installation();
        for (InstallationVisitor visitor: mVisitors) {
            visitor.visitInstallation(installation);
        }

        if (mManager != null) {
            mManager.saveInstallation(mContext, installation);
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
        mListener.onPushNotificationReceived(mContext, message);
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
}
