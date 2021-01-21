package com.microsoft.windowsazure.messaging.notificationhubs;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Allows platform-specific functionality to be made available to the NotificationHub global single
 * instance.
 */
class NotificationHubExtension {
    /**
     * Queries Firebase for the most recent token asynchronously.
     *
     * @param hub The instance of {@link NotificationHub} that should be made aware of the token.
     */
    public static void fetchPushChannel(final NotificationHub hub) {
        /*
         * Keeping this out of the NotificationHub class allows us to keep platform-specific knowledge
         * from entering the shared-portion of the code-base.
         *
         * Keeping this out of the FirebaseReceiver class allows us to not rely on Service start-time
         * behavior, which caused an issue for people adopting our platform:
         * https://github.com/Azure/azure-notificationhubs-xamarin/issues/21
         */
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.e("ANH", "unable to fetch FirebaseInstanceId");
                    return;
                }
                hub.setInstancePushChannel(task.getResult());
            }
        });
    }
}
