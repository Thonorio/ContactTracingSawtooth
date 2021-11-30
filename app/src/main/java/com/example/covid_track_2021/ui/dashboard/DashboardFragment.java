package com.example.covid_track_2021.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.covid_track_2021.Endpoints;
import com.example.covid_track_2021.R;
import com.example.covid_track_2021.blockchainAPI.BlockchainController;
import com.example.covid_track_2021.ui.submission.SubmissionFragment;
import com.example.covid_track_2021.blockchainAPI.BlockchainHandler;
import com.example.covid_track_2021.database.ContactedUsers.ContactedUsersService;
import com.example.covid_track_2021.database.UserInformation.UserService;
import com.example.covid_track_2021.databinding.FragmentDashboardBinding;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import co.nstant.in.cbor.CborBuilder;
import co.nstant.in.cbor.CborEncoder;
import co.nstant.in.cbor.CborException;
import sawtooth.sdk.signing.PrivateKey;

public class DashboardFragment extends Fragment {

    //  Database
    private ContactedUsersService contactedUsersService;
    private UserService userService;

    // View
    private DashboardViewModel dashboardViewModel;
    private FragmentDashboardBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.submitCode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, new SubmissionFragment()); // give your fragment container id in first parameter

                transaction.commit();

            }
        });

       /* view.findViewById(R.id.contactFound).setOnClickListener(new View.OnClickListener() { // Testing a new contact without needing to user 2 smartphones
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                contactedUsersService.contactFound(UUIDManager.getInstance().getUuid().toString());
            }
        });*/
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}