package com.example.notification_hubs_test_app_refresh.ui.main;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.notification_hubs_test_app_refresh.NotificationDetailActivity;
import com.example.notification_hubs_test_app_refresh.R;
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationMessage;

import java.util.HashMap;
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

        mViewModel.addNotification(new NotificationMessage("Puppies for Adoption!", "6 puppies born Sunday, each could be yours for just $100,000.00.", new HashMap<String,String>()));

        Map<String, String> catData = new HashMap<>();
        catData.put("feline", "yes");
        catData.put("fun", "no");
        mViewModel.addNotification(new NotificationMessage("Kittens for pickup!", "Please take them, we can't handle it!", catData));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.notification_list_fragment, container, false);

        final NotificationDisplayAdapter notificationDisplayAdapter = new NotificationDisplayAdapter();
        final Observer<List<NotificationMessage>> notificationsObserver = notificationMessages -> {
            notificationDisplayAdapter.setNotifications(notificationMessages);
            Toast.makeText(this.getContext(), R.string.notification_received_message, Toast.LENGTH_SHORT).show();
        };
        mViewModel.getNotificationList().observe(getViewLifecycleOwner(), notificationsObserver);
        final RecyclerView notificationList = root.findViewById(R.id.notificationList);
        notificationList.setLayoutManager(new LinearLayoutManager(getActivity()));
        notificationList.setAdapter(notificationDisplayAdapter);

        notificationDisplayAdapter.setClickListener(message -> {
            Intent i  = new Intent(this.getActivity(), NotificationDetailActivity.class);
            i.putExtra(NotificationDetailActivity.INTENT_TITLE_KEY, message.getTitle());
            i.putExtra(NotificationDetailActivity.INTENT_BODY_KEY, message.getMessage());
            for (Map.Entry<String, String> row : message.getCustomData().entrySet()) {
                i.putExtra(row.getKey(), row.getValue());
            }
            startActivity(i);
        });

        return root;
    }
}
