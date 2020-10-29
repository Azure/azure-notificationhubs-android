package com.example.notification_hubs_sample_app_java;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.google.firebase.messaging.RemoteMessage;

import java.util.List;
import java.util.Map;

public class NotificationListFragment extends Fragment {

    private NotificationListViewModel mViewModel;

    public static NotificationListFragment newInstance() {
        return new NotificationListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(NotificationListViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.notification_list_fragment, container, false);

        final NotificationDisplayAdapter notificationDisplayAdapter = new NotificationDisplayAdapter(
                this.getString(R.string.notification_untitled),
                this.getString(R.string.notification_no_body));
        final Observer<List<RemoteMessage>> notificationsObserver = notificationMessages -> {
            notificationDisplayAdapter.setNotifications(notificationMessages);
            Toast.makeText(this.getContext(), R.string.notification_received_message, Toast.LENGTH_SHORT).show();
        };
        mViewModel.getNotificationList().observe(getViewLifecycleOwner(), notificationsObserver);
        final RecyclerView notificationList = root.findViewById(R.id.notificationList);
        notificationList.setLayoutManager(new LinearLayoutManager(getActivity()));
        notificationList.setAdapter(notificationDisplayAdapter);

        notificationDisplayAdapter.setClickListener(message -> {
            Intent i  = new Intent(this.getActivity(), NotificationDetailActivity.class);
            RemoteMessage.Notification notification = message.getNotification();
            if(notification != null){
                i.putExtra(NotificationDetailActivity.INTENT_TITLE_KEY, notification.getTitle());
                i.putExtra(NotificationDetailActivity.INTENT_BODY_KEY, notification.getBody());
            }
            for (Map.Entry<String, String> row : message.getData().entrySet()) {
                i.putExtra(row.getKey(), row.getValue());
            }
            startActivity(i);
        });

        return root;
    }
}
