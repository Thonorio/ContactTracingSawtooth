package com.example.covid_track_2021.ui.submission;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.covid_track_2021.Endpoints;
import com.example.covid_track_2021.R;
import com.example.covid_track_2021.blockchainAPI.BlockchainController;
import com.example.covid_track_2021.blockchainAPI.BlockchainHandler;
import com.example.covid_track_2021.database.LocationsVisitedTracking.LocationsTrackingService;
import com.example.covid_track_2021.database.ContactedUsers.ContactedUsersService;
import com.example.covid_track_2021.database.UserInformation.UserService;
import com.example.covid_track_2021.database.UserPreferences.UserPreferencesService;
import com.example.covid_track_2021.database.UserUUIDTraking.UserTrackingService;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import co.nstant.in.cbor.CborBuilder;
import co.nstant.in.cbor.CborEncoder;
import co.nstant.in.cbor.CborException;

public class SubmissionFragment extends Fragment{

    //  Database
    private ContactedUsersService contactedUsersService;
    private UserService userService;
    private UserTrackingService userTrackingService;
    private LocationsTrackingService locationsVisitService;
    private UserPreferencesService userPreferencesService;
    private SubmissionViewModel mViewModel;

    public static SubmissionFragment newInstance() {
        return new SubmissionFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.submission_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SubmissionViewModel.class);
        // TODO: Use the ViewModel
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        contactedUsersService = new ContactedUsersService(this.getContext());
        userService = new UserService(this.getContext());
        userTrackingService = new UserTrackingService(this.getContext());
        locationsVisitService = new LocationsTrackingService(this.getContext());
        userPreferencesService = new UserPreferencesService(this.getContext());

        int numberOfDays = 14;

        view.findViewById(R.id.sendUserInformation2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<ByteArrayOutputStream> listOfPayloads = new ArrayList<>();
                BlockchainController requestHandler = new BlockchainController(BlockchainHandler.getPrivateKeyFromString(userService.getKeyForAddress()));
                ByteArrayOutputStream payload = new ByteArrayOutputStream();

                if(userPreferencesService.isSendUUIDsChecked()){
                    // Fetch User UUIDs
                    String publicIdJson = new Gson().toJson(userTrackingService.getAllIdentifiersGenerated(numberOfDays));

                    try {
                        new CborEncoder(payload).encode(new CborBuilder()
                                .addMap()
                                .put("ACTION", "publish_positive")
                                .put("KEY", publicIdJson)
                                .end()
                                .build());
                    } catch (CborException e) {
                        e.printStackTrace();
                    }
                    listOfPayloads.add(payload);

                    requestHandler.wrapAndSend(Endpoints.URL.value, listOfPayloads);
                }

                if(userPreferencesService.isSendLocationsChecked()) {
                    // Fetch locations
                    String locationsJson = new Gson().toJson(locationsVisitService.getAllIdentifiersGenerated(numberOfDays));

                    try {
                        new CborEncoder(payload).encode(new CborBuilder()
                                .addMap()
                                .put("ACTION", "publish_location")
                                .put("KEY", locationsJson)
                                .end()
                                .build());
                    } catch (CborException e) {
                        e.printStackTrace();
                    }
                    listOfPayloads.add(payload);

                    requestHandler.wrapAndSend(Endpoints.URL.value, listOfPayloads);
                }
            }
        });
    }
}