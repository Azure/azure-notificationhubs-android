package com.example.notification_hubs_test_app_kotlin.ui.main

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationHub
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationListener
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationMessage

class NotificationListViewModel : ViewModel() {
    private val mNotifications: MutableLiveData<ArrayList<NotificationMessage>> = MutableLiveData()
    private val mListener: NotificationListener
    fun addNotification(notification: NotificationMessage?) {
        var toUpdate: ArrayList<NotificationMessage>? = mNotifications.value
        if (toUpdate == null) {
            toUpdate = ArrayList()
        }
        if (notification != null) {
            toUpdate.add(0, notification)
        }
        mNotifications.postValue(toUpdate)
    }

    fun clearNotifications() {
        var toUpdate: ArrayList<NotificationMessage>? = mNotifications.value
        if (toUpdate == null) {
            toUpdate = ArrayList()
        } else {
            toUpdate.clear()
        }
        mNotifications.postValue(toUpdate)
    }

    fun getNotificationList(): MutableLiveData<ArrayList<NotificationMessage>> {
        return mNotifications;
    }

    init {
        mListener = NotificationListener { context: Context?, message: NotificationMessage? -> addNotification(message) }
        NotificationHub.setListener(mListener)
    }
}