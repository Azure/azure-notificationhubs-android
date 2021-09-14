package com.example.notification_hubs_sample_app_java;

import android.util.Base64;

import androidx.annotation.Nullable;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.microsoft.windowsazure.messaging.notificationhubs.Installation;
import com.microsoft.windowsazure.messaging.notificationhubs.InstallationAdapter;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class CustomInstallationGetRequest extends JsonObjectRequest {
    private final static String API_VERSION = "2020-06";
    private final static long TOKEN_EXPIRE_SECONDS = 5 * 60;
    private final static DateFormat sIso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ", Locale.ENGLISH);
    private final CustomConnectionString connectionString;

    /**
     * Creates a new request.
     *
     * @param connectionString The Notification Hubs Connection string
     * @param hubName          The Notification Hubs hub name.
     * @param jsonRequest      A {@link JSONObject} to post with the request. Null indicates no
     *                         parameters will be posted along with request.
     * @param listener         Listener to receive the JSON response
     * @param errorListener    Error listener, or null to ignore errors.
     */
    public CustomInstallationGetRequest(CustomConnectionString connectionString, String hubName, Installation installation, @Nullable JSONObject jsonRequest, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener) {
        super(Method.GET, getInstallationUrl(connectionString.getEndpoint(), hubName, installation.getInstallationId()), jsonRequest, listener, errorListener);
        this.connectionString = connectionString;
    }

    @Override
    public Map<String, String> getHeaders() {
        try {
            Map<String,String> params = new HashMap<>() {{
                put("Content-Type", "application/json");
                put("x-ms-version", API_VERSION);
                put("Authorization", generateAuthToken(
                        CustomInstallationGetRequest.super.getUrl(),
                        connectionString.getSharedAccessKeyName(),
                        connectionString.getSharedAccessKey()));
                put("User-Agent", "Custom-User-Agent");
            }};
            return params;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {

        if(response.statusCode == HttpURLConnection.HTTP_OK) {
            return Response.success(new JSONObject(), HttpHeaderParser.parseCacheHeaders(response));
        }

        return super.parseNetworkResponse(response);
    }

    static String getInstallationUrl(String endpoint, String hubName, String installationId) {
        final String serviceBusProtocolIdentifier = "sb://";
        StringBuilder url = new StringBuilder();
        if (endpoint.startsWith(serviceBusProtocolIdentifier)) {
            endpoint = endpoint.substring(serviceBusProtocolIdentifier.length());
        }
        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() -1);
        }
        url.append("https://").append(endpoint).append("/").append(hubName).append("/installations/")
                .append(installationId).append("?api-version=").append(API_VERSION);
        return url.toString();
    }

    private static String generateAuthToken(String url, String sharedAccessKeyName, String sharedAccessKey) throws InvalidKeyException {
        try {
            url = URLEncoder.encode(url, "UTF-8").toLowerCase(Locale.ENGLISH);
        } catch (UnsupportedEncodingException e) {
            // this shouldn't happen because of the fixed encoding
        }

        // Set expiration in seconds
        long expires = (System.currentTimeMillis() / 1000) + TOKEN_EXPIRE_SECONDS;

        String toSign = url + '\n' + expires;

        // sign
        byte[] bytesToSign = toSign.getBytes();
        Mac mac = null;
        try {
            mac = Mac.getInstance("HmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            // This shouldn't happen because of the fixed algorithm
        }

        SecretKeySpec secret = new SecretKeySpec(sharedAccessKey.getBytes(), mac.getAlgorithm());
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
        return "SharedAccessSignature sr=" + url + "&sig=" + base64Signature + "&se=" + expires + "&skn=" + sharedAccessKeyName;
    }

    public static <T> Response.Listener<T> wrapListener(final InstallationAdapter.Listener subject, final Installation installation) {
        return response -> subject.onInstallationSaved(installation);
    }

    public static Response.ErrorListener wrapErrorListener(final InstallationAdapter.ErrorListener errorListener) {
        return error -> errorListener.onInstallationSaveError(error);
    }
}
