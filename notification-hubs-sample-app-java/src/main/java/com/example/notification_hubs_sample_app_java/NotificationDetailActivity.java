package com.example.notification_hubs_sample_app_java;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotificationDetailActivity extends AppCompatActivity {
    private static final String INTENT_KEY_PREFIX = "com.example.notification_hubs_sample_app_java.detail:";
    public static final String INTENT_TITLE_KEY = INTENT_KEY_PREFIX + "title";
    public static final String INTENT_BODY_KEY = INTENT_KEY_PREFIX + "body";

    private static final Set<String> RESERVED_KEYS;

    static {
        RESERVED_KEYS = new HashSet<>();
        RESERVED_KEYS.add(INTENT_TITLE_KEY);
        RESERVED_KEYS.add(INTENT_BODY_KEY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent info = getIntent();

        setContentView(R.layout.activity_notification_detail);

        final TextView titleValue = findViewById(R.id.detailTitleValue);
        if(info.hasExtra(INTENT_TITLE_KEY)) {
            titleValue.setText(info.getStringExtra(INTENT_TITLE_KEY));
        } else {
            titleValue.setText(this.getString(R.string.notification_untitled));
        }

        final TextView bodyValue = findViewById(R.id.detail_body_view);
        if(info.hasExtra(INTENT_BODY_KEY)) {
            bodyValue.setText(info.getStringExtra(INTENT_BODY_KEY));
        } else {
            bodyValue.setText(this.getString(R.string.notification_no_body));
        }

        final LinearLayout detailContent = findViewById(R.id.detail_content);
        for (String key : getDataKeys(info)) {
            final View dataRow = getLayoutInflater().inflate(R.layout.data_item, findViewById(android.R.id.content), false);
            final TextView dataKey = dataRow.findViewById(R.id.data_key);
            dataKey.setText(key);

            final TextView dataValue = dataRow.findViewById(R.id.data_value);
            dataValue.setText(info.getStringExtra(key));

            detailContent.addView(dataRow);
        }
    }

    private static Iterable<String> getDataKeys(Intent i) {
        List<String> returnValue = new ArrayList<>();
        for (String key: i.getExtras().keySet()) {
            if(!RESERVED_KEYS.contains(key)) {
                returnValue.add(key);
            }
        }

        Collections.sort(returnValue);
        return returnValue;
    }
}
