package com.example.covid_track_2021.database.UserUUIDTraking;

import android.database.sqlite.SQLiteDatabase;

public class UserTracking {
    private static final String TABLE_NAME = "USER_TRACKING";
    private static final String KEY_ID = "id";
    private static final String KEY_RANDOM_UUID = "uuid";
    private static final String KEY_TIMESTAMP = "timestamp";

    public static void onCreate(SQLiteDatabase db){
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
                + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_RANDOM_UUID + " BLOB,"
                + KEY_TIMESTAMP + " TEXT"
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

    public static String getKeyUuid() {
        return KEY_RANDOM_UUID;
    }

    public static String getKeyRandomUuid() {
        return KEY_RANDOM_UUID;
    }

    public static String getKeyTimestamp() {
        return KEY_TIMESTAMP;
    }
}