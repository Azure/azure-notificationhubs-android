package com.microsoft.windowsazure.messaging;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class NotificationHubLegacyExtension {
    private static final String MIGRATED_TO_FCM_V1_KEY = "__ANH__MIGRATED_TO_FCM_V1";

    public static Task<Void> migrateToFcmV1(Context context, NotificationHub hub, String ...tags) {
        return FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                    sharedPreferences.edit().putBoolean(MIGRATED_TO_FCM_V1_KEY, true).apply();
                }
                fetchTokenAndRegister(hub, tags);
            }
        });
    }

    public static boolean isMigratedToFcmV1(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(MIGRATED_TO_FCM_V1_KEY, false);
    }

    private static void fetchTokenAndRegister(final NotificationHub hub, String ...tags) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.e("ANH", "unable to fetch FirebaseInstanceId");
                    return;
                }

                register(hub, task.getResult(), tags);
            }
        });
    }

    private static void register(final NotificationHub hub, String token, String ...tags) {
        if (hub == null) {
            return;
        }

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    hub.register(token, tags);
                } catch (Exception e) {
                    Log.e("ANH", "Error registering the device", e);
                }
            }
        });
    }
}
