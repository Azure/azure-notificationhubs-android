package com.microsoft.windowsazure.messaging.notificationhubs;

import android.app.Activity;
import android.content.Intent;
import com.google.firebase.messaging.RemoteMessage;
import com.microsoft.windowsazure.messaging.notificationhubs.async.NotificationHubFuture;

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
    private InstallationManager mManager;

    private Activity mActivity;

    NotificationHub() {
        mMiddleware = new ArrayList<InstallationMiddleware>();

        mPushChannelEnricher = new PushChannelEnricher();
        mTagEnricher = new TagEnricher();

        BagMiddleware defaultEnrichment = new BagMiddleware();
        defaultEnrichment.addEnricher(mPushChannelEnricher);
        defaultEnrichment.addEnricher(mTagEnricher);

        useInstanceMiddleware(defaultEnrichment);

        mManager = new NoopInstallationManager();
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
     * Creates a new {@link Installation} and registers it with a backend that tracks devices.
     */
    public static void reinstall() {
        getInstance().reinstallInstance();
    }

    /**
     * Creates a new {@link Installation} and registers it with a backend that tracks devices.
     */
    public NotificationHubFuture<String> reinstallInstance() {
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

        return mManager.saveInstallation(installation);
    }

    static void setPushChannel(String token) {
        getInstance().setInstancePushChannel(token);
    }

    void setInstancePushChannel(String token) {
        mPushChannelEnricher.setPushChannel(token);
    }

    static void relayMessage(RemoteMessage message) {
        getInstance().relayInstanceMessage(message);
    }

    void relayInstanceMessage(RemoteMessage message) {
        mListener.onPushNotificationReceived(mActivity, new NotificationMessage(message));
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
