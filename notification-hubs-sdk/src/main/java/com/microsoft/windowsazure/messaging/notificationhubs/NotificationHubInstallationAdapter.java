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

    public NotificationHubInstallationAdapter(Context context, String hubName, String connectionString) {
        mHubName = hubName;
        mConnectionString = ConnectionString.parse(connectionString);
        mHttpClient = HttpUtils.createHttpClient(context.getApplicationContext());
    }

    /**
     * Updates a backend with the updated Installation information for this device.
     *
     * @param installation The record to update.\
     */
    @Override
    public void saveInstallation(final Installation installation, final SaveListener onInstallationSaved, final ErrorListener onInstallationSaveError) {
        final String url = NotificationHubInstallationHelper.getInstallationUrl(getEndpoint(), mHubName, installation.getInstallationId());
        try {
            mHttpClient.callAsync(url, "PUT", getSaveHeaders(url), buildSaveCallTemplate(installation), buildSaveCallback(installation, onInstallationSaved, onInstallationSaveError));
        } catch (InvalidKeyException e) {
            onInstallationSaveError.onInstallationOperationError(e);
        }
    }

    private String getEndpoint() {
        return NotificationHubInstallationHelper.parseSbEndpoint(mConnectionString.getEndpoint());
    }

    /**
     * Updates a backend to remove the references to the specified {@link Installation}.
     *
     * @param id                        The unique identifier associated with the {@link Installation} to be removed.
     * @param onInstallationDeleted     A callback which will be invoked if the {@link Installation} is
     *                                  successfully deleted.
     * @param onInstallationDeleteError A callback which will be invoked if the {@link Installation}
     */
    @Override
    public void deleteInstallation(String id, DeleteListener onInstallationDeleted, ErrorListener onInstallationDeleteError) {
        final String url = NotificationHubInstallationHelper.getInstallationUrl(getEndpoint(), mHubName, id);
        try {
            mHttpClient.callAsync(url, "DELETE", getDeleteHeaders(url), null, buildDeleteCallback(id, onInstallationDeleted, onInstallationDeleteError));
        } catch (InvalidKeyException e) {
            onInstallationDeleteError.onInstallationOperationError(e);
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

    private Map<String, String> getSaveHeaders(final String url) throws InvalidKeyException {
        return new HashMap<String, String>(){{
            put("Content-Type", "application/json");
            put("x-ms-version", "2015-01");
            put("Authorization", generateAuthToken(url));
            put("User-Agent", getUserAgent());
        }};
    }

    private Map<String, String> getDeleteHeaders(final String url) throws InvalidKeyException {
        return new HashMap<String, String>(){{
            put("x-ms-version", "2015-01");
            put("Authorization", generateAuthToken(url));
            put("User-Agent", getUserAgent());
        }};
    }

    private HttpClient.CallTemplate buildSaveCallTemplate(final Installation installation) {
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

    private ServiceCallback buildSaveCallback(final Installation installation, final SaveListener onInstallationSaved, final ErrorListener onInstallationSaveError) {
        return new ServiceCallback() {
            @Override
            public void onCallSucceeded(HttpResponse httpResponse) {
                onInstallationSaved.onInstallationSaved(installation);
            }

            @Override
            public void onCallFailed(Exception e) {
                onInstallationSaveError.onInstallationOperationError(e);
            }
        };
    }

    private ServiceCallback buildDeleteCallback(final String installationId, final DeleteListener onInstallationDeleted, final ErrorListener onInstallationDeleteError) {
        return new ServiceCallback() {
            @Override
            public void onCallSucceeded(HttpResponse httpResponse) {
                onInstallationDeleted.onInstallationDeleted(installationId);
            }

            @Override
            public void onCallFailed(Exception e) {
                onInstallationDeleteError.onInstallationOperationError(e);
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
