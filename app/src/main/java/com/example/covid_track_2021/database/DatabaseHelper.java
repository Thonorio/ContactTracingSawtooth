package com.example.covid_track_2021.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.covid_track_2021.database.LocationsVisitedTracking.LocationsTracking;
import com.example.covid_track_2021.database.ContactedUsers.ContactedUsers;
import com.example.covid_track_2021.database.UserInformation.User;
import com.example.covid_track_2021.database.UserUUIDTraking.UserTracking;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "covid_tracker";
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public static synchronized DatabaseHelper getInstance() {
        return instance;
    }

    public DatabaseHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        ContactedUsers.onCreate(db);
        User.onCreate(db);
        UserTracking.onCreate(db);
        LocationsTracking.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        ContactedUsers.onUpgrade(db);
        User.onUpgrade(db);
        UserTracking.onUpgrade(db);
        LocationsTracking.onUpgrade(db);
    }

    public Cursor getTableCursor(String tableName){
        Cursor cursor = this.getWritableDatabase().query(
                tableName,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );
        return cursor;
    }
}