package com.example.notification_hubs_test_app_refresh.ui.main;

import android.content.Context;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.notification_hubs_test_app_refresh.R;
import com.microsoft.windowsazure.messaging.notificationhubs.INotification;
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationHub;
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationListener;
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NotificationListViewModel extends ViewModel {
    private final MutableLiveData<List<INotification>> mNotifications;
    private final NotificationListener mListener;

    public NotificationListViewModel() {
        mNotifications = new MutableLiveData<List<INotification>>();
        mListener = (context, message) -> {
            addNotification(message);
        };

        NotificationHub.setListener(mListener);
    }

    public void addNotification(INotification notification) {
        List<INotification> toUpdate = mNotifications.getValue();
        if(toUpdate == null) {
            toUpdate = new ArrayList<INotification>();
        }
        toUpdate.add(0, notification);
        mNotifications.postValue(toUpdate);
    }

    public void clearNotifications() {
        List<INotification> toUpdate = mNotifications.getValue();
        if (toUpdate == null) {
            toUpdate = new ArrayList<INotification>();
        } else {
            toUpdate.clear();
        }
        mNotifications.postValue(toUpdate);
    }

    public LiveData<List<INotification>> getNotificationList() {
        return mNotifications;
    }
}
