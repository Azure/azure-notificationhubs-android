package com.example.notification_hubs_test_app_refresh.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.microsoft.windowsazure.messaging.notificationhubs.NotificationHub;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SetupViewModel extends ViewModel {
    private final static Pattern sTagPattern = Pattern.compile("^[a-zA-Z0-9_@#\\.:\\-]{1,120}$");

    private String mUnknownText;

    private final MutableLiveData<String> mDeviceToken = new MutableLiveData<String>();
    private final MutableLiveData<String> mInstallationId = new MutableLiveData<String>();
    private final MutableLiveData<List<String>> mTags = new MutableLiveData<List<String>>();

    private final MutableLiveData<Boolean> mIsEnabled = new MutableLiveData<Boolean>();

    public SetupViewModel() {
        mTags.setValue(iterableToList(NotificationHub.getTags()));
        mIsEnabled.setValue(NotificationHub.isEnabled());

        String pushChannel = NotificationHub.getPushChannel();
        if (pushChannel != null) {
            mDeviceToken.setValue(pushChannel);
        }

        String installationId = NotificationHub.getInstallationId();
        if (installationId != null) {
            mInstallationId.setValue(installationId);
        }
    }

    public LiveData<String> getDeviceToken() {
        return mDeviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        mDeviceToken.setValue(deviceToken);
    }

    public LiveData<String> getInstallationId() {
        return  mInstallationId;
    }

    public void setInstallationId(String installationId) {
        NotificationHub.setInstallationId(installationId);
        mInstallationId.setValue(installationId);
    }

    public LiveData<Boolean> getIsEnabled() {
        return mIsEnabled;
    }

    public void setIsEnabled(boolean b) {
        NotificationHub.setEnabled(b);
        mIsEnabled.setValue(NotificationHub.isEnabled());
    }

    public LiveData<List<String>> getTags() {
        return mTags;
    }

    public void addTag(String tag) {
        if (!isAllowableTag(tag)) {
            throw new IllegalArgumentException();
        }

        if (NotificationHub.addTag(tag)) {
            mTags.postValue(iterableToList(NotificationHub.getTags()));
        }
    }

    public void removeTag(String tag) {
        if (NotificationHub.removeTag(tag)) {
            mTags.postValue(iterableToList(NotificationHub.getTags()));
        }
    }

    public String getUnknownText() {
        if (mUnknownText == null) {
            return "";
        }
        return mUnknownText;
    }

    public void setUnknownText(String mUnknownText) {
        this.mUnknownText = mUnknownText;
    }

    static <T> List<T> iterableToList(Iterable<T> iterable) {
        List<T> retval = new ArrayList<T>();
        for (T entry: iterable) {
            retval.add(entry);
        }
        return retval;
    }

    static boolean isAllowableTag(String tag) {
        return sTagPattern.matcher(tag).matches();
    }
}
