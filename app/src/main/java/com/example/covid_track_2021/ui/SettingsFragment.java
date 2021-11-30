package com.example.covid_track_2021.ui;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.example.covid_track_2021.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.settings);
    }
}