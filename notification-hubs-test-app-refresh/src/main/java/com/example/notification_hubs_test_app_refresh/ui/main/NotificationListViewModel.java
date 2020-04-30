package com.example.notification_hubs_test_app_refresh.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.microsoft.windowsazure.messaging.notificationhubs.NotificationMessage;
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationHub;
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationListener;

import java.util.ArrayList;
import java.util.List;

public class NotificationListViewModel extends ViewModel {
    private final MutableLiveData<List<NotificationMessage>> mNotifications;
    private final NotificationListener mListener;

    public NotificationListViewModel() {
        mNotifications = new MutableLiveData<List<NotificationMessage>>();
        mListener = (context, message) -> {
            addNotification(message);
        };

        NotificationHub.setListener(mListener);
    }

    public void addNotification(NotificationMessage notification) {
        List<NotificationMessage> toUpdate = mNotifications.getValue();
        if(toUpdate == null) {
            toUpdate = new ArrayList<NotificationMessage>();
        }
        toUpdate.add(0, notification);
        mNotifications.postValue(toUpdate);
    }

    public void clearNotifications() {
        List<NotificationMessage> toUpdate = mNotifications.getValue();
        if (toUpdate == null) {
            toUpdate = new ArrayList<NotificationMessage>();
        } else {
            toUpdate.clear();
        }
        mNotifications.postValue(toUpdate);
    }

    public LiveData<List<NotificationMessage>> getNotificationList() {
        return mNotifications;
    }
}
