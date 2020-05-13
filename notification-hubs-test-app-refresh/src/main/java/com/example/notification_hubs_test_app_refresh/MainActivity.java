package com.example.notification_hubs_test_app_refresh;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.notification_hubs_test_app_refresh.ui.main.SectionsPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.microsoft.windowsazure.messaging.notificationhubs.InstallationTemplate;
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationHub;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        NotificationHub.initialize(this.getApplication(), BuildConfig.hubName, BuildConfig.hubListenConnectionString);
        NotificationHub.addTag("userAgent:com.example.notification_hubs_test_app_refresh:0.1.0");
        InstallationTemplate testTemplate = new InstallationTemplate();
        testTemplate.setBody("{\"data\":{\"message\":\"Notification Hub test notification: $myTextProp\"}}");
        NotificationHub.addTemplate("testTemplate", testTemplate);
    }
}