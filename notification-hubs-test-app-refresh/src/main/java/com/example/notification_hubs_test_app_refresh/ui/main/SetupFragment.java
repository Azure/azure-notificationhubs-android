package com.example.notification_hubs_test_app_refresh.ui.main;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.notification_hubs_test_app_refresh.R;

public class SetupFragment extends Fragment {

    private SetupViewModel mViewModel;

    public static SetupFragment newInstance() {
        return new SetupFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SetupViewModel.class);
        final String unknownText = getResources().getString(R.string.unknown_information);
        mViewModel.setUnknownText(unknownText);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.setup_fragment, container, false);

        final TextView deviceTokenValue = root.findViewById(R.id.device_token_value);
        final Observer<String> deviceTokenObserver = s -> deviceTokenValue.setText(s);
        mViewModel.getDeviceToken().observe(getViewLifecycleOwner(), deviceTokenObserver);

        final TextView installationIdValue = root.findViewById(R.id.installation_id_value);
        final Observer<String> installationIdObserver = s -> installationIdValue.setText(s);
        mViewModel.getInstallationId().observe(getViewLifecycleOwner(), installationIdObserver);

        return root;
    }
}
