package com.example.notification_hubs_test_app_kotlin

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager

import com.example.notification_hubs_test_app_kotlin.ui.main.SectionsPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.microsoft.windowsazure.messaging.notificationhubs.InstallationTemplate
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationHub

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

        NotificationHub.start(this.application, BuildConfig.hubName, BuildConfig.hubListenConnectionString)
        NotificationHub.addTag("userAgent:com.example.notification_hubs_test_app_kotlin:0.1.0")
        val testTemplate = InstallationTemplate()
        testTemplate.body = "{\"data\":{\"message\":\"Notification Hub test notification: \$myTextProp\"}}"
        NotificationHub.setTemplate("testTemplate", testTemplate)
    }
}