package com.example.notification_hubs_test_app_java;

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
    private static final String INTENT_KEY_PREFIX = "com.example.notification_hubs_test_app_java.detail:";
    public static final String INTENT_TITLE_KEY = INTENT_KEY_PREFIX + "title";
    public static final String INTENT_BODY_KEY = INTENT_KEY_PREFIX + "body";

    private static final Set<String> RESERVED_KEYS;

    static {
        RESERVED_KEYS = new HashSet<String>();
        RESERVED_KEYS.add(INTENT_TITLE_KEY);
        RESERVED_KEYS.add(INTENT_BODY_KEY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent info = getIntent();

        setContentView(R.layout.activity_notification_detail);

        final TextView titleValue = findViewById(R.id.detailTitleValue);
        titleValue.setText(info.getStringExtra(INTENT_TITLE_KEY));

        final TextView bodyValue = findViewById(R.id.detail_body_view);
        bodyValue.setText(info.getStringExtra(INTENT_BODY_KEY));

        final LinearLayout detailContent = findViewById(R.id.detail_content);
        for (String key : getDataKeys(info)) {
            final View dataRow = getLayoutInflater().inflate(R.layout.data_item, null);
            final TextView dataKey = dataRow.findViewById(R.id.data_key);
            dataKey.setText(key);

            final TextView dataValue = dataRow.findViewById(R.id.data_value);
            dataValue.setText(info.getStringExtra(key));

            detailContent.addView(dataRow);
        }
    }

    private static Iterable<String> getDataKeys(Intent i) {
        List<String> retval = new ArrayList<String>();
        for (String key: i.getExtras().keySet()) {
            if(!RESERVED_KEYS.contains(key)) {
                retval.add(key);
            }
        }

        Collections.sort(retval);
        return retval;
    }
}
