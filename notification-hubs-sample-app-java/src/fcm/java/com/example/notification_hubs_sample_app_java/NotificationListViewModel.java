package com.example.notification_hubs_sample_app_java;

import android.app.Notification;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.notification_hubs_sample_app_java.cookbook.NotificationDisplayer;
import com.google.firebase.messaging.RemoteMessage;
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationHub;
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationListener;

import java.util.ArrayList;
import java.util.List;

public class NotificationListViewModel extends ViewModel {
    private final MutableLiveData<List<RemoteMessage>> mNotifications;
    private final NotificationListener mListener;

    public NotificationListViewModel() {
        mNotifications = new MutableLiveData<List<RemoteMessage>>();
        mListener = (context, message) -> {
            addNotification(message);
        };

//        NotificationHub.setListener(mListener);
        NotificationHub.setListener(new NotificationDisplayer());
    }

    public void addNotification(RemoteMessage notification) {
        List<RemoteMessage> toUpdate = mNotifications.getValue();
        if(toUpdate == null) {
            toUpdate = new ArrayList<RemoteMessage>();
        }
        toUpdate.add(0, notification);
        mNotifications.postValue(toUpdate);
    }

    public void clearNotifications() {
        List<RemoteMessage> toUpdate = mNotifications.getValue();
        if (toUpdate == null) {
            toUpdate = new ArrayList<RemoteMessage>();
        } else {
            toUpdate.clear();
        }
        mNotifications.postValue(toUpdate);
    }

    public LiveData<List<RemoteMessage>> getNotificationList() {
        return mNotifications;
    }
}
