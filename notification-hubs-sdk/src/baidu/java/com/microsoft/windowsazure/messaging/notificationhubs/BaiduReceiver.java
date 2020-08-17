package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.android.pushservice.PushMessageReceiver;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class BaiduReceiver extends PushMessageReceiver {
    public static final String TAG = BaiduReceiver.class
            .getSimpleName();

    private final NotificationHub mHub;

    /**
     * Creates a new instance that will inform the static-global-instance of {@link com.microsoft.windowsazure.messaging.notificationhubs.NotificationHub}
     * when a new message is received.
     */
    public BaiduReceiver() {
        this(NotificationHub.getInstance());
    }

    /**
     * Creates a new instance that will inform the given {@link com.microsoft.windowsazure.messaging.notificationhubs.NotificationHub} instance when a
     * message is received.
     * @param hub The hub that should be informed when a new notification arrives.
     */
    public BaiduReceiver(NotificationHub hub) {
        mHub = hub;
    }

    @Override
    public void onBind(Context context, int errorCode, String appid,
                       String userId, String channelId, String requestId) {
        String responseString = "onBind errorCode=" + errorCode + " appid="
                + appid + " userId=" + userId + " channelId=" + channelId
                + " requestId=" + requestId;
        Log.d(TAG, responseString);

        if (errorCode == 0) {
            // Binding successful
            Log.d(TAG, " Binding successful");
        }

        mHub.setPushChannel(userId + "-" + channelId);
    }

    @Override
    public void onMessage(Context context, String message,
                          String customContentString) {
        String messageString = " onMessage=\"" + message
                + "\" customContentString=" + customContentString;
        Log.d(TAG, messageString);
        if (!TextUtils.isEmpty(customContentString)) {
            JSONObject customJson = null;
            try {
                customJson = new JSONObject(customContentString);
                String myvalue = null;
                if (!customJson.isNull("mykey")) {
                    myvalue = customJson.getString("mykey");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onNotificationArrived(Context context, String title, String description, String customContentString) {
        String notifyString = " Notice Arrives onNotificationArrived  title=\"" + title
                + "\" description=\"" + description + "\" customContent="
                + customContentString;
        Log.d(TAG, notifyString);

        mHub.relayMessage(getNotificationMessage(title, description, customContentString));
    }

    /**
     * Converts from a customContentString to a {@link BasicNotificationMessage}.
     * @param title The message title intended for this device, as delivered by Baidu.
     * @param description The message description intended for this device, as delivered by Baidu.
     * @param customContentString The message data intended for this device, as delivered by Baidu.
     * @return A fully instantiated {@link BasicNotificationMessage}.
     */
    static BasicNotificationMessage getNotificationMessage(String title, String description, String customContentString) {
        Map<String, String> data = null;
        if (!TextUtils.isEmpty(customContentString)) {
            JSONObject customJson = null;
            try {
                customJson = new JSONObject(customContentString);
                if (!customJson.isNull("mykey")) {
                    String value = customJson.getString("mykey");
                    data.put("mykey", value);
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return new BasicNotificationMessage(
                title,
                description,
                data);
    }

    @Override
    public void onNotificationClicked(Context context, String title, String description, String customContentString) {}

    @Override
    public void onSetTags(Context context, int errorCode,
                          List<String> successTags, List<String> failTags, String requestId) {}

    @Override
    public void onDelTags(Context context, int errorCode,
                          List<String> successTags, List<String> failTags, String requestId) {}

    @Override
    public void onListTags(Context context, int errorCode, List<String> tags,
                           String requestId) {}

    @Override
    public void onUnbind(Context context, int errorCode, String requestId) {
        String responseString = "onUnbind errorCode=" + errorCode
                + " requestId = " + requestId;
        Log.d(TAG, responseString);

        if (errorCode == 0) {
            // Unbinding is successful
            Log.d(TAG, " Unbinding is successful ");
        }
    }
}
