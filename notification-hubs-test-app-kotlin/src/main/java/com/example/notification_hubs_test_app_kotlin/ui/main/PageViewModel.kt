package com.example.notification_hubs_test_app_kotlin.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel


class PageViewModel : ViewModel() {
    private val mIndex: MutableLiveData<Int> = MutableLiveData()
    private val mText: LiveData<String?> = Transformations.map(mIndex) { input -> "Hello world from section: $input" }

    fun setIndex(index: Int) {
        mIndex.value = index
    }

    fun getText(): LiveData<String?>? {
        return mText
    }
}