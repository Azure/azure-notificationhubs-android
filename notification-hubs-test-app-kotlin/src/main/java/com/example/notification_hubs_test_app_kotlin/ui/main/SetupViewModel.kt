package com.example.notification_hubs_test_app_kotlin.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationHub
import java.util.regex.Pattern

class SetupViewModel : ViewModel() {
    private var mUnknownText: String? = null
    private val mDeviceToken: MutableLiveData<String> = MutableLiveData()
    private val mInstallationId: MutableLiveData<String> = MutableLiveData()
    private val mTags: MutableLiveData<List<String>> = MutableLiveData()
    private val mIsEnabled: MutableLiveData<Boolean> = MutableLiveData()

    fun getDeviceToken(): LiveData<String?>? {
        return mDeviceToken
    }

    fun setDeviceToken(deviceToken: String?) {
        mDeviceToken.value = deviceToken
    }

    fun getInstallationId(): LiveData<String?>? {
        return mInstallationId
    }

    fun setInstallationId(installationId: String?) {
        NotificationHub.setInstallationId(installationId)
        mInstallationId.value = installationId
    }

    fun getIsEnabled(): LiveData<Boolean?>? {
        return mIsEnabled
    }

    fun setIsEnabled(b: Boolean) {
        NotificationHub.setEnabled(b)
        mIsEnabled.value = NotificationHub.isEnabled()
    }

    fun getTags(): LiveData<List<String>> {
        return mTags
    }

    fun addTag(tag: String?) {
        if (!isAllowableTag(tag)) {
            throw java.lang.IllegalArgumentException()
        }
        if (NotificationHub.addTag(tag)) {
            mTags.postValue(iterableToList<String>(NotificationHub.getTags()))
        }
    }

    fun removeTag(tag: String?) {
        if (NotificationHub.removeTag(tag)) {
            mTags.postValue(iterableToList<String>(NotificationHub.getTags()))
        }
    }

    fun getUnknownText(): String? {
        return if (mUnknownText == null) {
            ""
        } else mUnknownText
    }

    fun setUnknownText(unknownText: String) {
        mUnknownText = unknownText
    }

    companion object {
        private val sTagPattern: Pattern = Pattern.compile("^[a-zA-Z0-9_@#\\.:\\-]{1,120}$")
        fun <T> iterableToList(iterable: Iterable<T>): List<T> {
            val retval: ArrayList<T> = ArrayList()
            for (entry in iterable) {
                retval.add(entry)
            }
            return retval
        }

        fun isAllowableTag(tag: String?): Boolean {
            return sTagPattern.matcher(tag).matches()
        }
    }

    init {
        mTags.value = iterableToList<String>(NotificationHub.getTags())
        mIsEnabled.value = NotificationHub.isEnabled()
        val pushChannel: String = NotificationHub.getPushChannel()
        mDeviceToken.value = pushChannel
        val installationId: String = NotificationHub.getInstallationId()
        mInstallationId.value = installationId
    }
}