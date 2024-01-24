package com.microsoft.windowsazure.messaging.notificationhubs;

import android.os.Build;
import android.util.Base64;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.microsoft.windowsazure.messaging.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
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
 * Custom Android Volley Request to UPSERT an Installation records with the Azure Notification Hub backend.
 *
 * It was built specifically to work with API Version 2020-06.
 */
class InstallationPutRequest extends JsonObjectRequest {
    private final static String API_VERSION = "2020-06";
    private final static long TOKEN_EXPIRE_SECONDS = 5 * 60;
    private final static DateFormat sIso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ", Locale.ENGLISH);

    private final ConnectionString mConnectionString;

    public InstallationPutRequest(ConnectionString connectionString, String hubName, Installation installation, Response.Listener<JSONObject> onSuccess, Response.ErrorListener onFailure){
        super(
                Method.PUT,
                getInstallationUrl(connectionString.getEndpoint(), hubName, installation.getInstallationId()),
                getBody(installation),
                onSuccess,
                onFailure);
        mConnectionString = connectionString;
    }

    @Override
    public Map<String, String> getHeaders() {
        try {
            Map<String,String> params = new HashMap<String, String>(){{
                put("Content-Type", "application/json");
                put("x-ms-version", API_VERSION);
                put("Authorization", generateAuthToken(
                        InstallationPutRequest.super.getUrl(),
                        mConnectionString.getSharedAccessKeyName(),
                        mConnectionString.getSharedAccessKey()));
                put("User-Agent", getUserAgent());
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

    public static JSONObject getBody(final Installation installation) {
        final JSONArray tagList = new JSONArray();
        for (String tag: installation.getTags()) {
            tagList.put(tag);
        }

        try {
            final JSONObject serializedTemplates = new JSONObject();
            for (Map.Entry<String, InstallationTemplate> template : installation.getTemplates().entrySet()) {
                String templateName = template.getKey();
                serializedTemplates.put(templateName, InstallationTemplate.serialize(templateName, template.getValue()));
            }

            JSONObject jsonBody = new JSONObject() {{
                put("installationId", installation.getInstallationId());
                put("platform", installation.getPlatform());
                put("pushChannel", installation.getPushChannel());
                put("tags", tagList);
                put("templates", serializedTemplates);
                put("userId", installation.getUserId());
            }};

            Date expiration = installation.getExpiration();
            if (expiration != null) {
                String formattedExpiration = sIso8601Format.format(expiration);
                jsonBody.put("expirationTime", formattedExpiration);
            }
            return jsonBody;
        } catch (JSONException e) {
            // Converting to a RuntimeException, avoiding specific checking which would break
            throw new UnsupportedOperationException("", e);
        }
    }

    public static <T> Response.Listener<T> wrapListener(final InstallationAdapter.Listener subject, final Installation installation) {
        return new Response.Listener<T>() {
            @Override
            public void onResponse(T response) {
                subject.onInstallationSaved(installation);
            }
        };
    }

    public static Response.ErrorListener wrapErrorListener(final InstallationAdapter.ErrorListener errorListener) {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                errorListener.onInstallationSaveError(error);
            }
        };
    }

    /**
     * Generates the User-Agent
     */
    private static String getUserAgent() {
        String apiOrigin = "AndroidSdkV1FcmV" + BuildConfig.VERSION_NAME;
        String userAgent = String.format("NOTIFICATIONHUBS/%s (api-origin=%s; os=%s; os_version=%s;)",
                API_VERSION, apiOrigin, "Android", Build.VERSION.RELEASE);

        return userAgent;
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
}
