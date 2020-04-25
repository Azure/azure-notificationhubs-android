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

    public SetupViewModel() {
        NotificationHub.useVisitor(
            // TODO: Instead of having this be InstallationMiddleware, which will get a partially
            //       hydrated Installation, have this be an InstallationManager that intercepts the
            //       call with the finalized installation.
            subject -> {
                String pushChannel = subject.getPushChannel();
                if (pushChannel == null) {
                    pushChannel = getUnknownText();
                }
                mDeviceToken.setValue(pushChannel);

                String installationId = subject.getInstallationId();
                if (installationId == null) {
                    installationId = getUnknownText();
                }
                mInstallationId.setValue(installationId);
            });
        mTags.setValue(iterableToList(NotificationHub.getTags()));

        // TODO: This reinstall is forced to take advantage of the hook we setup above into the
        //       Installation creation process. Honestly, this stinks. We shouldn't encourage people
        //       to reinstall the app every time this screen is launched.
        //       Because this is a sample application, we probably need to either decide to remove
        //       the device token/installation id fields on the setup fragment, or add getters for
        //       this information. This decisions will hinge on how common of scenario it is to
        //       fetch your installation id or device token.
        NotificationHub.reinstall();
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
