package com.example.covid_track_2021.database.UserPreferences;

import android.database.sqlite.SQLiteDatabase;

public class UserPreferences {
    private static final String TABLE_NAME = "USER_PREFERENCES";
    private static final String KEY_ID = "id";
    private static final String KEY_SEND_UUIDS = "send_uuids";
    private static final String KEY_SEND_LOCATIONS = "send_locations";

    public static void onCreate(SQLiteDatabase db){
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
                + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_SEND_UUIDS + " INTEGER,"
                + KEY_SEND_LOCATIONS + " INTEGER"
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    public static void onUpgrade(SQLiteDatabase db){
        // Drop older table if exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        // Create tables again
        onCreate(db);
    }

    public static String getTableName() {
        return TABLE_NAME;
    }

    public static String getKeyId() {
        return KEY_ID;
    }

    public static String getKeySendUuids() {
        return KEY_SEND_UUIDS;
    }

    public static String getKeySendLocations() {
        return KEY_SEND_LOCATIONS;
    }
}