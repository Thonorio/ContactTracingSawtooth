package com.example.covid_track_2021.database.UserUUIDTraking;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.example.covid_track_2021.database.ContactedUsers.ContactedUsers;
import com.example.covid_track_2021.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.UUID;

import sawtooth.sdk.signing.PrivateKey;

public class UserTrackingService {
    private DatabaseHelper databaseHelperInstance;


    public UserTrackingService(Context context) {
        this.databaseHelperInstance = DatabaseHelper.getInstance(context);
    }

    public void storeRandomUUID(UUID randomUUID){

        ContentValues cValues = new ContentValues();
        cValues.put(UserTracking.getKeyUuid(), randomUUID.toString());

        this.databaseHelperInstance.getWritableDatabase().insert(UserTracking.getTableName(),null, cValues);
    }


    public ArrayList<String> getAllIdentifiersGenerated(int numberOfDays) {

        ArrayList<String> contactIds = new ArrayList<>();

        String query = "SELECT " + UserTracking.getKeyUuid() + " FROM " + UserTracking.getTableName() + " WHERE " + UserTracking.getKeyTimestamp() + " BETWEEN date('now','localtime', '" + numberOfDays + "' ) AND date('now', 'localtime')";

        Cursor cursor = this.databaseHelperInstance.getWritableDatabase().rawQuery(query, null);

        while (cursor.moveToNext()) {
            contactIds.add(cursor.getString(cursor.getColumnIndex(UserTracking.getKeyRandomUuid())));
        }

        cursor.close();
        return contactIds;
    }
}
