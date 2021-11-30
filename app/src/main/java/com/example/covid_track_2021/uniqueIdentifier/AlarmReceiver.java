package com.example.covid_track_2021.uniqueIdentifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.covid_track_2021.uniqueIdentifier.UUIDManager;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        UUIDManager.getInstance().updateUuid(context);
    }
}