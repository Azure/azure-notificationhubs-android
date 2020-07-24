package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;
import android.os.Build;
import android.util.Base64;

import androidx.annotation.Nullable;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Responsible for informing Azure Notification Hubs of changes to when this device should receive
 * notifications.
 */
public class NotificationHubInstallationAdapter implements InstallationAdapter {
    private static final long DEFAULT_INSTALLATION_EXPIRATION_MILLIS = 1000L * 60L * 60L * 24L * 90L;

    private final String mHubName;
    private final ConnectionString mConnectionString;
    private final RequestQueue mRequestQueue;
    private final long mInstallationExpirationWindow;


    public NotificationHubInstallationAdapter(Context context, String hubName, String connectionString) {
        this(context, hubName, connectionString, DEFAULT_INSTALLATION_EXPIRATION_MILLIS);
    }

    NotificationHubInstallationAdapter(Context context, String hubName, String connectionString, long installationExpirationWindow) {
        mHubName = hubName;
        mConnectionString = ConnectionString.parse(connectionString);
        mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
        mInstallationExpirationWindow = installationExpirationWindow;
    }

    /**
     * Updates a backend with the updated Installation information for this device.
     *
     * @param installation The record to update.
     */
    @Override
    public void saveInstallation(final Installation installation, final Listener onInstallationSaved, final ErrorListener onInstallationSaveError) {
        addExpiration(installation);

        Request<?> request;

        try {
            request = new InstallationPutRequest(mConnectionString, mHubName, installation, onInstallationSaved, onInstallationSaveError);
        } catch (JSONException e) {
            onInstallationSaveError.onInstallationSaveError(new IllegalArgumentException("unable to serialize installation to JSON", e));
            return;
        }

        mRequestQueue.add(request);
    }

    /**
     * Ensures that the provided {@link Installation} has an expiration.
     * @param target The instance of {@link Installation} which may not have an expiration.
     */
    void addExpiration(Installation target) {
        if (target.getExpiration() != null) {
            return;
        }

        Date expiration = new Date();
        expiration = new Date(expiration.getTime() + mInstallationExpirationWindow);

        target.setExpiration(expiration);
    }
}


