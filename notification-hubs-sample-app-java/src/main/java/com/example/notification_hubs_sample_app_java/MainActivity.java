package com.example.notification_hubs_sample_app_java;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.notification_hubs_sample_app_java.ui.main.SectionsPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.microsoft.windowsazure.messaging.notificationhubs.DebounceInstallationAdapter;
import com.microsoft.windowsazure.messaging.notificationhubs.InstallationAdapter;
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationHub;
import com.microsoft.windowsazure.messaging.notificationhubs.PushChannelValidationAdapter;

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

        // Use the custom installation adapter to save to the backend once received from the backend
        InstallationAdapter client = new CustomInstallationAdapter(this.getApplication(), BuildConfig.hubName, BuildConfig.hubListenConnectionString);
        InstallationAdapter debouncer = new DebounceInstallationAdapter(this.getApplication(), client);
        NotificationHub.start(
            this.getApplication(),
            new PushChannelValidationAdapter(debouncer)
        );
    }
}