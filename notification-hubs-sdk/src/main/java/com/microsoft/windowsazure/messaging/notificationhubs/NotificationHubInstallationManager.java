package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;
import android.util.Base64;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Responsible for informing Azure Notification Hubs of changes to when this device should receive
 * notifications.
 */
public class NotificationHubInstallationManager implements InstallationManager {

    private final String mHubName;
    private final ConnectionString mConnectionString;

    public NotificationHubInstallationManager(String hubName, String connectionString) {
        mHubName = hubName;
        mConnectionString = ConnectionString.parse(connectionString);
    }

    /**
     * Updates a backend with the updated Installation information for this device.
     *
     * @param installation The record to update.
     * @return A future, with the Installation ID as the value.
     */
    @Override
    public void saveInstallation(Context context, Installation installation) {
        try {
            RequestQueue queue = Volley.newRequestQueue(context.getApplicationContext());
            // TODO: Build a PUT Installation Request, and fire it off to the Notification Hubs Backend.
            String url  = "https://" + mConnectionString.getEndpoint() + "/" + mHubName + "/installations/" + installation.getInstallationId() + "?api-version=2017-04";

            JSONObject jsonBody = new JSONObject();

                jsonBody.put("installationId", installation.getInstallationId());
                jsonBody.put("platform", "FCM");
                jsonBody.put("pushChannel", installation.getPushChannel());

            final String requestBody = jsonBody.toString();


            StringRequest request = new StringRequest(
                    Request.Method.PUT,
                    url,
                    response -> {
                        System.out.println();
                    },
                    error -> {
                    }
            ){
                @Override
                public byte[] getBody() {
                    try {
                        return requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        return null;
                    }
                }

                @Override
                public  Map<String, String> getHeaders(){
                    Map<String,String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/json");
                    try {
                        params.put("Authorization", generateAuthToken(url));
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    }
                    params.put("x-ms-version", "2017-04");
                    return params;
                }
            };

            queue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private String generateAuthToken(String url) throws InvalidKeyException {

        String keyName = mConnectionString.getSharedAccessKeyName();

        String key = mConnectionString.getSharedAccessKey();

        try {
            url = URLEncoder.encode(url, "UTF-8").toLowerCase(Locale.ENGLISH);
        } catch (UnsupportedEncodingException e) {
            // this shouldn't happen because of the fixed encoding
        }

        // Set expiration in seconds
        Calendar expireDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expireDate.add(Calendar.MINUTE, 5);

        long expires = expireDate.getTimeInMillis() / 1000;

        String toSign = url + '\n' + expires;

        // sign

        byte[] bytesToSign = toSign.getBytes();
        Mac mac = null;
        try {
            mac = Mac.getInstance("HmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            // This shouldn't happen because of the fixed algorithm
        }

        SecretKeySpec secret = new SecretKeySpec(key.getBytes(), mac.getAlgorithm());
        mac.init(secret);
        byte[] signedHash = mac.doFinal(bytesToSign);
        String base64Signature = Base64.encodeToString(signedHash, Base64.DEFAULT);
        base64Signature = base64Signature.trim();
        try {
            base64Signature = URLEncoder.encode(base64Signature, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // this shouldn't happen because of the fixed encoding
        }

        // construct authorization string
        String token = "SharedAccessSignature sr=" + url + "&sig=" + base64Signature + "&se=" + expires + "&skn=" + keyName;

        return token;
    }


}
