package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;
import android.os.Build;
import android.util.Base64;

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
    private static final long EXPIRE_SECONDS = 5 * 60;

    private final String mHubName;
    private final ConnectionString mConnectionString;
    private HttpClient mHttpClient;
    private final ExpirationVisitor mExpirationVisitor;

    private final static DateFormat sIso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ", Locale.ENGLISH);

    public NotificationHubInstallationAdapter(Context context, String hubName, String connectionString) {
        this(context, hubName, connectionString, (ExpirationVisitor) null);
    }

    NotificationHubInstallationAdapter(Context context, String hubName, String connectionString, ExpirationVisitor expirationVisitor) {
        mHubName = hubName;
        mConnectionString = ConnectionString.parse(connectionString);
        mHttpClient = HttpUtils.createHttpClient(context.getApplicationContext());
        mExpirationVisitor = expirationVisitor;
    }

    /**
     * Updates a backend with the updated Installation information for this device.
     *
     * @param installation The record to update.\
     */
    @Override
    public void saveInstallation(final Installation installation, final Listener onInstallationSaved, final ErrorListener onInstallationSaveError) {
        String formatEndpoint = NotificationHubInstallationHelper.parseSbEndpoint(mConnectionString.getEndpoint());
        final String url = NotificationHubInstallationHelper.getInstallationUrl(formatEndpoint, mHubName, installation.getInstallationId());

        mHttpClient.callAsync(url, "PUT", getHeaders(url), buildCallTemplate(installation), buildServiceCallback(installation, onInstallationSaved, onInstallationSaveError));
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
        long expires = (System.currentTimeMillis() / 1000) + EXPIRE_SECONDS;

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
        return "SharedAccessSignature sr=" + url + "&sig=" + base64Signature + "&se=" + expires + "&skn=" + keyName;
    }

    private Map<String, String> getHeaders(final String url) {
        try {
            Map<String,String> params = new HashMap<String, String>(){{
                put("Content-Type", "application/json");
                put("x-ms-version", "2015-01");
                put("Authorization", generateAuthToken(url));
                put("User-Agent", getUserAgent());
            }};
            return params;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
    }

    private HttpClient.CallTemplate buildCallTemplate(final Installation installation) {
        return new HttpClient.CallTemplate() {
            @Override
            public String buildRequestBody() throws JSONException {
                try {
                    final JSONArray tagList = new JSONArray();
                    for (String tag: installation.getTags()) {
                        tagList.put(tag);
                    }

                    final JSONObject serializedTemplates = new JSONObject();
                    for (Map.Entry<String, InstallationTemplate> template: installation.getTemplates().entrySet()) {
                        String templateName = template.getKey();
                        serializedTemplates.put(templateName, InstallationTemplate.serialize(templateName, template.getValue()));
                    }

                    JSONObject jsonBody = new JSONObject(){{
                        put("installationId", installation.getInstallationId());
                        put("platform", "GCM");
                        put("pushChannel", installation.getPushChannel());
                        put("tags", tagList);
                        put("templates", serializedTemplates);
                    }};

                    Date expiration = installation.getExpiration();
                    if(expiration != null) {
                        String formattedExpiration = sIso8601Format.format(expiration);
                        jsonBody.put("expirationTime", formattedExpiration);
                    }

                    return jsonBody.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void onBeforeCalling(URL url, Map<String, String> headers) {
            }
        };
    }

    private ServiceCallback buildServiceCallback(final Installation installation, final Listener onSuccess, final ErrorListener onFailure) {
        return new ServiceCallback() {
            @Override
            public void onCallSucceeded(HttpResponse httpResponse) {
                onSuccess.onInstallationSaved(installation);
            }

            @Override
            public void onCallFailed(Exception e) {
                onFailure.onInstallationSaveError(e);
            }
        };
    }


    /**
     * Generates the User-Agent
     */
    private String getUserAgent() {
        String userAgent = String.format("NOTIFICATIONHUBS/%s (api-origin=%s; os=%s; os_version=%s;)",
                "2015-01", "AndroidSdkV1FcmV1.0.0-preview2", "Android", Build.VERSION.RELEASE);

        return userAgent;
    }
}
