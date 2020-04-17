package com.microsoft.windowsazure.messaging.notificationhubs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * A Singleton controller that wraps all interactions with Firebase Cloud Messaging and Azure
 * Notification Hubs.
 */
public final class NotificationHub {
    private static NotificationHub sInstance;

    private NotificationListener mListener;
    private final List<InstallationMiddleware> mMiddleware;
    private final PushChannelEnricher mPushChannelEnricher;
    private final TagEnricher mTagEnricher;
    private final IdAssignmentEnricher mIdAssignmentEnricher;

    private InstallationManager mManager;
    private Context mContext;

    NotificationHub() {
        mMiddleware = new ArrayList<>();

        mPushChannelEnricher = new PushChannelEnricher();
        mTagEnricher = new TagEnricher();
        mIdAssignmentEnricher = new IdAssignmentEnricher();

        BagMiddleware defaultEnrichment = new BagMiddleware();
        defaultEnrichment.addEnricher(mPushChannelEnricher);
        defaultEnrichment.addEnricher(mTagEnricher);
        defaultEnrichment.addEnricher(mIdAssignmentEnricher);

        useInstanceMiddleware(defaultEnrichment);
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> this.setInstancePushChannel(task.getResult().getToken()));
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

    public static void initialize(Context context, String hubName, String connectionString) {
        initialize(context, new NotificationHubInstallationManager(
                hubName,
                connectionString));
    }

    public static void initialize(Context context, InstallationManager manager) {
        NotificationHub instance = getInstance();
        instance.setInstanceInstallationManager(manager);
        instance.mContext = context.getApplicationContext();
        instance.mTagEnricher.setPreferences(instance.mContext);
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
     * Registers {@link InstallationMiddleware} for use when a new {@link Installation} is to be
     * created and registered.
     * @param middleware A {@link InstallationMiddleware} to invoke when creating a new
     *                   {@link Installation}.
     */
    public static void useMiddleware(InstallationMiddleware middleware) {
        getInstance().useInstanceMiddleware(middleware);
    }

    /**
     * Registers {@link InstallationMiddleware} for use when a new {@link Installation} is to be
     * created and registered.
     * @param middleware A {@link InstallationMiddleware} to invoke when creating a new
     *                   {@link Installation}.
     */
    public void useInstanceMiddleware(InstallationMiddleware middleware) {
        mMiddleware.add(middleware);
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
        ListIterator<InstallationMiddleware> iterator = this.mMiddleware.listIterator(this.mMiddleware.size());

        InstallationEnricher enricher = subject -> {
            // Intentionally Left Blank
        };

        while(iterator.hasPrevious()) {
            InstallationMiddleware current = iterator.previous();
            enricher = current.getInstallationEnricher(enricher);
        }

        Installation installation = new Installation();
        enricher.enrichInstallation(installation);

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
        mPushChannelEnricher.setPushChannel(token);
        reinstallInstance();
    }

    /**
     * Fetches the current Push Channel.
     * @return The current string that identifies this device as Push notification receiver. Null if
     *         it hasn't been initialized yet.
     */
    public String getInstancePushChannel() {
        return mPushChannelEnricher.getPushChannel();
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
        return mIdAssignmentEnricher.getInstallationId();
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
        mIdAssignmentEnricher.setInstallationId(id);
        reinstallInstance();
    }

    static void relayMessage(RemoteMessage message) {
        getInstance().relayInstanceMessage(message);
    }

    void relayInstanceMessage(RemoteMessage message) {
        mListener.onPushNotificationReceived(mContext, new NotificationMessage(message));
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
        if(mTagEnricher.addTag(tag)){
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
        if(mTagEnricher.addTags(tags)) {
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
        if(mTagEnricher.removeTag(tag)) {
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
        if(mTagEnricher.removeTags(tags)) {
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
        return mTagEnricher.getTags();
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
        if (mTagEnricher.getTags().iterator().hasNext()) {
            mTagEnricher.clearTags();
            this.reinstallInstance();
        }
    }
}
