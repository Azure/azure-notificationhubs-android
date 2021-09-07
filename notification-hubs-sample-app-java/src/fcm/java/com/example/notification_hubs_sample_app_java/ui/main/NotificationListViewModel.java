package com.example.notification_hubs_sample_app_java.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.messaging.RemoteMessage;
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationHub;
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationListener;

import java.util.ArrayList;
import java.util.List;

public class NotificationListViewModel extends ViewModel {
    private final MutableLiveData<List<RemoteMessage>> mNotifications;
    private final NotificationListener mListener;

    public NotificationListViewModel() {
        mNotifications = new MutableLiveData<>();
        mListener = (context, message) -> addNotification(message);

        NotificationHub.setListener(mListener);
    }

    public void addNotification(RemoteMessage notification) {
        List<RemoteMessage> toUpdate = mNotifications.getValue();
        if(toUpdate == null) {
            toUpdate = new ArrayList<>();
        }
        toUpdate.add(0, notification);
        mNotifications.postValue(toUpdate);
    }

    public void clearNotifications() {
        List<RemoteMessage> toUpdate = mNotifications.getValue();
        if (toUpdate == null) {
            toUpdate = new ArrayList<>();
        } else {
            toUpdate.clear();
        }
        mNotifications.postValue(toUpdate);
    }

    public LiveData<List<RemoteMessage>> getNotificationList() {
        return mNotifications;
    }
}
