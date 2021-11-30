package com.example.covid_track_2021.database.ContactedLocations;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.example.covid_track_2021.database.DatabaseHelper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class ContactedLocationsService {
    private DatabaseHelper databaseHelperInstance;

    public ContactedLocationsService(Context context) {
        this.databaseHelperInstance = DatabaseHelper.getInstance(context);
    }

    public void contactFound(String userPublicKey){
        Cursor tableCursor = this.databaseHelperInstance.getTableCursor(ContactedLocations.getTableName());

        while(tableCursor.moveToNext()) {
            String publicKey = tableCursor.getString(tableCursor.getColumnIndexOrThrow(ContactedLocations.getKeyHash()));
            // If KEYS already exists
            if(publicKey.equals(userPublicKey)){
                LocalDate timestamp = LocalDate.parse(tableCursor.getString(tableCursor.getColumnIndexOrThrow(ContactedLocations.getKeyTimestamp())));

                // Measure if difference in days is bigger that 1
                if(ChronoUnit.DAYS.between(LocalDate.now(), timestamp) == 0 ){
                    System.out.println("Contact already exists");
                    Log.i("LOG:","Contact already exists");
                    return;
                }
                Log.i("LOG:","Contact needs date updated");

                ContentValues cValues = new ContentValues();
                cValues.put(ContactedLocations.getKeyHash(), userPublicKey);
                cValues.put(ContactedLocations.getKeyTimestamp(), String.valueOf(LocalDate.now()));
                String[] whereArgs = {userPublicKey};
                this.databaseHelperInstance.getWritableDatabase().update(ContactedLocations.getTableName(), cValues, ContactedLocations.getKeyHash()  + " = ?" ,  whereArgs);

                return;
            }
        }

        tableCursor.close();

        Log.i("LOG -> NEW CONTACT UUID: ", userPublicKey);

        ContentValues cValues = new ContentValues();
        cValues.put(ContactedLocations.getKeyHash(), userPublicKey);
        cValues.put(ContactedLocations.getKeyTimestamp(), String.valueOf(LocalDate.now()));

        this.databaseHelperInstance.getWritableDatabase().insert(ContactedLocations.getTableName(),null, cValues);
    }

    public boolean wasThereAContactWithinTheLast14Days(String UUIDs){

        String query = "SELECT " + ContactedLocations.getKeyHash() + " FROM " + ContactedLocations.getTableName() +
                        " WHERE " + ContactedLocations.getKeyHash() + " in " + UUIDs +
                        "AND (JULIANDAY(" + LocalDate.now() +") - JULIANDAY(" + ContactedLocations.getKeyTimestamp() +")) < 14" ;
        Cursor cursor = this.databaseHelperInstance.getWritableDatabase().rawQuery(query, null);

        while (cursor.moveToNext()) {
            cursor.close();
            return true;
        }

        cursor.close();
        return false;
    }
}
