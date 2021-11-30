package com.example.covid_track_2021.database.LocationsVisitedTracking;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.example.covid_track_2021.database.ContactedLocations.ContactedLocations;
import com.example.covid_track_2021.database.ContactedUsers.ContactedUsers;
import com.example.covid_track_2021.database.DatabaseHelper;

import java.time.LocalDate;
import java.util.ArrayList;

public class LocationsTrackingService {
    private DatabaseHelper databaseHelperInstance;

    public LocationsTrackingService(Context context) {
        this.databaseHelperInstance = DatabaseHelper.getInstance(context);
    }

    public void locationFound(String qrCodeHash){
        ContentValues cValues = new ContentValues();
        cValues.put(LocationsTracking.getKeyQrCodeHash(), qrCodeHash);
        cValues.put(ContactedUsers.getKeyTimestamp(), String.valueOf(LocalDate.now()));

        this.databaseHelperInstance.getWritableDatabase().insert(LocationsTracking.getTableName(),null, cValues);
    }

    public ArrayList<String> getAllIdentifiersGenerated(int numberOfDays) {

        ArrayList<String> contactIds = new ArrayList<>();

        String query = "SELECT " + LocationsTracking.getKeyQrCodeHash() + " FROM " + LocationsTracking.getTableName() + " WHERE " + LocationsTracking.getKeyTimestamp() + " BETWEEN date('now','localtime', '" + numberOfDays + "' ) AND date('now', 'localtime')";

        Cursor cursor = this.databaseHelperInstance.getWritableDatabase().rawQuery(query, null);

        while (cursor.moveToNext()) {
            contactIds.add(cursor.getString(cursor.getColumnIndex(LocationsTracking.getKeyQrCodeHash())));
        }

        cursor.close();
        return contactIds;
    }


}
