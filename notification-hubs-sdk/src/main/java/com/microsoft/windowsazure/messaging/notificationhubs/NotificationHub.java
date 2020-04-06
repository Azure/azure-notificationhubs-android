package com.microsoft.windowsazure.messaging.notificationhubs;

import android.app.Activity;
import android.content.Intent;
import java.util.ArrayList;
import java.util.List;

/**
 * A Singleton controller that wraps all interactions with Firebase Cloud Messaging and Azure
 * Notification Hubs.
 */
public final class NotificationHub {
    private static NotificationHub sInstance;

    private NotificationListener mListener;
    private final List<InstallationMiddleware> mMiddleware;

    private Activity mActivity;

    NotificationHub() {
        mMiddleware = new ArrayList<InstallationMiddleware>();
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
}
