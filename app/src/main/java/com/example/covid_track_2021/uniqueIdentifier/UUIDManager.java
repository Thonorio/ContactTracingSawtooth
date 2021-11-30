package com.example.covid_track_2021.uniqueIdentifier;

import android.content.Context;

import com.example.covid_track_2021.database.UserUUIDTraking.UserTrackingService;

import java.util.UUID;

public class UUIDManager {
    private UUID uuid = UUID.randomUUID();
    private static UUIDManager single_instance = null;
    private UserTrackingService userTrackingService;

    public static UUIDManager getInstance() {
        if (single_instance == null) {
            single_instance = new UUIDManager();
        }

        return single_instance;
    }

    public UUIDManager() {
    }

    public UUID getUuid() {
        return uuid;
    }

    public void updateUuid(Context context) {
        this.uuid = UUID.randomUUID();
        userTrackingService = new UserTrackingService(context);
        userTrackingService.storeRandomUUID(this.uuid);
    }
}