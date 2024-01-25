package com.example.notification_hubs_sample_app_java.legacy;

import static org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.notification_hubs_sample_app_java.BuildConfig;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.microsoft.windowsazure.messaging.NotificationHub;
import com.microsoft.windowsazure.messaging.NotificationHubLegacyExtension;
import com.microsoft.windowsazure.messaging.PnsSpecificRegistrationFactory;
import com.microsoft.windowsazure.messaging.Registration;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

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
        if (!NotificationHubLegacyExtension.isMigratedToFcmV1(context)) {
            NotificationHubLegacyExtension.migrateToFcmV1(context, hub);
        } else {
            fetchTokenAndRegister();
        }
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
                    hub.register(token);
                } catch (Exception e) {
                    Log.e("ANH", "Error registering the device", e);
                }
            }
        });
    }
}
