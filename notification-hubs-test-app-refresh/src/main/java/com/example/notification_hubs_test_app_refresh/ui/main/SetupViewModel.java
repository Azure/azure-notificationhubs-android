package com.example.notification_hubs_test_app_refresh.ui.main;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.microsoft.windowsazure.messaging.notificationhubs.Installation;
import com.microsoft.windowsazure.messaging.notificationhubs.InstallationEnricher;
import com.microsoft.windowsazure.messaging.notificationhubs.InstallationMiddleware;
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationHub;

public class SetupViewModel extends ViewModel {

    private String mUnknownText;

    private final MutableLiveData<String> mDeviceToken = new MutableLiveData<String>();
    private final MutableLiveData<String> mInstallationId = new MutableLiveData<String>();

    public SetupViewModel() {
        NotificationHub.useMiddleware(new InstallationMiddleware() {
            // TODO: Instead of having this be InstallationMiddleware, which will get a partially
            //       hydrated Installation, have this be an InstallationManager that intercepts the
            //       call with the finalized installation.

            @Override
            public InstallationEnricher getInstallationEnricher(InstallationEnricher next) {
                return new InstallationEnricher() {
                    @Override
                    public void enrichInstallation(Installation subject) {
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

                        next.enrichInstallation(subject);
                    }
                };
            }
        });
    }

    public MutableLiveData<String> getDeviceToken() {
        return mDeviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        mDeviceToken.setValue(deviceToken);
    }

    public MutableLiveData<String> getInstallationId() {
        return  mInstallationId;
    }

    public void setInstallationId(String installationId) {
        mInstallationId.setValue(installationId);
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
}
