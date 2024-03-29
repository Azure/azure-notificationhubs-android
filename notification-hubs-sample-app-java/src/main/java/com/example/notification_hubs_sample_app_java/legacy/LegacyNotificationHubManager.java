package com.example.notification_hubs_sample_app_java.legacy;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.notification_hubs_sample_app_java.BuildConfig;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.microsoft.windowsazure.messaging.NotificationHub;

public final class LegacyNotificationHubManager {

    private static LegacyNotificationHubManager sInstance;

    NotificationHub hub;

    public static LegacyNotificationHubManager getInstance() {
        if (sInstance == null) {
            sInstance = new LegacyNotificationHubManager();
        }
        return sInstance;
    }

    public void initialize(Context context) {
        hub = new NotificationHub(BuildConfig.hubName, BuildConfig.hubListenConnectionString, context);
        fetchTokenAndRegister();
    }

    public boolean isInitialized() {
        return hub != null;
    }

    private void fetchTokenAndRegister() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.e("ANH", "unable to fetch FirebaseInstanceId");
                    return;
                }

                register(task.getResult());
            }
        });
    }

    public void register(String token) {
        if (hub == null) {
            return;
        }

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    hub.register(token, "anh-sample-app-reg");
                } catch (Exception e) {
                    Log.e("ANH", "Error registering the device", e);
                }
            }
        });
    }
}
