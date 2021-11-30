package com.example.covid_track_2021.ui.settings;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.example.covid_track_2021.MainActivity;
import com.example.covid_track_2021.R;
import com.example.covid_track_2021.database.UserPreferences.UserPreferences;
import com.example.covid_track_2021.database.UserPreferences.UserPreferencesService;

public class SettingsFragment extends PreferenceFragmentCompat {

    private UserPreferencesService userPreferencesService = new UserPreferencesService(getContext());

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.settings);

        Preference sendContacts = findPreference("sendContacts");
        sendContacts.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange (Preference preference, Object newValue) {
                userPreferencesService.setSendUUIDs(((SwitchPreference) preference).isChecked());
                return false;
            }
        });

        Preference senLocations = findPreference("senLocations");
        senLocations.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange (Preference preference, Object newValue) {
                // Handle preference click
                userPreferencesService.setSendLocations(((SwitchPreference) preference).isChecked());
                return true;
            }
        });

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

}