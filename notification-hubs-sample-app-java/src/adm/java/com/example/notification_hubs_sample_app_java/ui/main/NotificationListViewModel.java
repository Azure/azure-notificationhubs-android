package com.example.notification_hubs_sample_app_java.ui.main;

import android.app.Notification;
import android.content.Intent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.microsoft.windowsazure.messaging.notificationhubs.NotificationHub;
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationListener;

import java.util.ArrayList;
import java.util.List;

public class NotificationListViewModel extends ViewModel {
    private final MutableLiveData<List<Intent>> mNotifications;
    private final NotificationListener mListener;

    public NotificationListViewModel() {
        mNotifications = new MutableLiveData<List<Intent>>();
        mListener = (context, message) -> {
            addNotification(message);
        };

        NotificationHub.setListener(mListener);
    }

    public void addNotification(Intent notification) {
        List<Intent> toUpdate = mNotifications.getValue();
        if(toUpdate == null) {
            toUpdate = new ArrayList<Intent>();
        }
        toUpdate.add(0, notification);
        mNotifications.postValue(toUpdate);
    }

    public void clearNotifications() {
        List<Intent> toUpdate = mNotifications.getValue();
        if (toUpdate == null) {
            toUpdate = new ArrayList<Intent>();
        } else {
            toUpdate.clear();
        }
        mNotifications.postValue(toUpdate);
    }

    public LiveData<List<Intent>> getNotificationList() {
        return mNotifications;
    }
}
