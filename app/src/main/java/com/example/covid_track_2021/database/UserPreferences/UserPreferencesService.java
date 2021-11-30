package com.example.covid_track_2021.database.UserPreferences;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.example.covid_track_2021.database.DatabaseHelper;

import sawtooth.sdk.signing.PrivateKey;

public class UserPreferencesService {
    private DatabaseHelper databaseHelperInstance;


    public UserPreferencesService(Context context) {
        this.databaseHelperInstance = DatabaseHelper.getInstance(context);
    }

    public void setSendUUIDs(boolean sendUUIDs) {
        ContentValues cValues = new ContentValues();
        cValues.put(UserPreferences.getKeySendLocations(), sendUUIDs ? 1 : 0);

        this.databaseHelperInstance.getWritableDatabase().insert(UserPreferences.getTableName(),null, cValues);
    }

    public void setSendLocations(boolean sendLocations) {
        ContentValues cValues = new ContentValues();
        cValues.put(UserPreferences.getKeySendLocations(), sendLocations ? 1 : 0);

        this.databaseHelperInstance.getWritableDatabase().insert(UserPreferences.getTableName(),null, cValues);
    }

    public boolean isSendUUIDsChecked() {
        String query = "SELECT * FROM " + UserPreferences.getTableName() + " DESC LIMIT 1";
        Cursor cursor = this.databaseHelperInstance.getWritableDatabase().rawQuery(query, null);

        while (cursor.moveToNext()) {
            return  cursor.getInt(cursor.getColumnIndex(UserPreferences.getKeySendUuids())) == 1 ? true : false;
        }

        return false;
    }

    public boolean isSendLocationsChecked() {
        String query = "SELECT * FROM " + UserPreferences.getTableName() + " DESC LIMIT 1";
        Cursor cursor = this.databaseHelperInstance.getWritableDatabase().rawQuery(query, null);

        if(cursor.moveToNext()) {
           return  cursor.getInt(cursor.getColumnIndex(UserPreferences.getKeySendLocations())) == 1 ? true : false;
        }

        return false;
    }
}
