package com.example.covid_track_2021.database.ContactedUsers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.example.covid_track_2021.database.DatabaseHelper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class ContactedUsersService {
    private DatabaseHelper databaseHelperInstance;

    public ContactedUsersService(Context context) {
        this.databaseHelperInstance = DatabaseHelper.getInstance(context);
    }

    public void contactFound(String userPublicKey){
        Cursor tableCursor = this.databaseHelperInstance.getTableCursor(ContactedUsers.getTableName());

        while(tableCursor.moveToNext()) {
            String publicKey = tableCursor.getString(tableCursor.getColumnIndexOrThrow(ContactedUsers.getKeyPublicKey()));
            // If KEYS already exists
            if(publicKey.equals(userPublicKey)){
                LocalDate timestamp = LocalDate.parse(tableCursor.getString(tableCursor.getColumnIndexOrThrow(ContactedUsers.getKeyTimestamp())));

                // Measure if difference in days is bigger that 1
                if(ChronoUnit.DAYS.between(LocalDate.now(), timestamp) == 0 ){
                    System.out.println("Contact already exists");
                    Log.i("LOG:","Contact already exists");
                    return;
                }
                Log.i("LOG:","Contact needs date updated");

                ContentValues cValues = new ContentValues();
                cValues.put(ContactedUsers.getKeyPublicKey(), userPublicKey);
                cValues.put(ContactedUsers.getKeyTimestamp(), String.valueOf(LocalDate.now()));
                String[] whereArgs = {userPublicKey};
                this.databaseHelperInstance.getWritableDatabase().update(ContactedUsers.getTableName(), cValues, ContactedUsers.getKeyPublicKey()  + " = ?" ,  whereArgs);

                return;
            }
        }

        tableCursor.close();

        Log.i("LOG -> NEW CONTACT UUID: ", userPublicKey);

        ContentValues cValues = new ContentValues();
        cValues.put(ContactedUsers.getKeyPublicKey(), userPublicKey);
        cValues.put(ContactedUsers.getKeyTimestamp(), String.valueOf(LocalDate.now()));

        this.databaseHelperInstance.getWritableDatabase().insert(ContactedUsers.getTableName(),null, cValues);
    }

    public ArrayList<String> getAllContactsFound() {

        ArrayList<String> contactIds = new ArrayList<>();

        String query = "SELECT " + ContactedUsers.getKeyPublicKey() + " FROM " + ContactedUsers.getTableName();

        Cursor cursor = this.databaseHelperInstance.getWritableDatabase().rawQuery(query, null);

        while (cursor.moveToNext()) {
            contactIds.add(cursor.getString(cursor.getColumnIndex(ContactedUsers.getKeyPublicKey())));
        }

        cursor.close();
        return contactIds;
    }

    public boolean wasThereAContactWithinTheLast14Days(String UUIDs){

        String query = "SELECT " + ContactedUsers.getKeyPublicKey() + " FROM " + ContactedUsers.getTableName() +
                        " WHERE " + ContactedUsers.getKeyPublicKey() + " in " + UUIDs +
                        "AND (JULIANDAY(" + LocalDate.now() +") - JULIANDAY(" + ContactedUsers.getKeyTimestamp() +")) < 14" ;
        Cursor cursor = this.databaseHelperInstance.getWritableDatabase().rawQuery(query, null);

        while (cursor.moveToNext()) {
            cursor.close();
            return true;
        }

        cursor.close();
        return false;
    }
}
