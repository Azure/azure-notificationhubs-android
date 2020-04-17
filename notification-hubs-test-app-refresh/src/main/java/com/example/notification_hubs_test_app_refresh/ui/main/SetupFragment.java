package com.example.notification_hubs_test_app_refresh.ui.main;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notification_hubs_test_app_refresh.R;

import java.util.List;

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

        final EditText tagToAddField = root.findViewById(R.id.add_tag_field);
        final Button tagToAddButton = root.findViewById(R.id.add_tag_button);
        tagToAddButton.setOnClickListener(v -> {
            try {
                mViewModel.addTag(tagToAddField.getText().toString());
            } catch (IllegalArgumentException e) {
                Toast toast = Toast.makeText(getContext(), R.string.invalid_tag_message, Toast.LENGTH_SHORT);
                toast.show();
            }
            tagToAddField.getText().clear();
        });

        final TagDisplayAdapter tagDisplayAdapter = new TagDisplayAdapter(mViewModel);
        final Observer<List<String>> tagsObserver = s -> tagDisplayAdapter.setTags(s);
        mViewModel.getTags().observe(getViewLifecycleOwner(), tagsObserver);
        final RecyclerView tagList = root.findViewById(R.id.tag_list);
        tagList.setLayoutManager(new LinearLayoutManager(getActivity()));
        tagList.setAdapter(tagDisplayAdapter);

        return root;
    }
}
