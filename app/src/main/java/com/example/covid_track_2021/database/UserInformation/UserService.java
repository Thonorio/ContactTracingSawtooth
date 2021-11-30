package com.example.covid_track_2021.database.UserInformation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.example.covid_track_2021.database.ContactedUsers.ContactedUsers;
import com.example.covid_track_2021.database.DatabaseHelper;

import java.util.ArrayList;

import sawtooth.sdk.signing.PrivateKey;

public class UserService {
    private DatabaseHelper databaseHelperInstance;


    public UserService(Context context) {
        this.databaseHelperInstance = DatabaseHelper.getInstance(context);
    }

    public void storePrivateKey(PrivateKey privateKey){

        ContentValues cValues = new ContentValues();
        cValues.put(User.getKeyPrivateKey(), privateKey.getBytes());

        this.databaseHelperInstance.getWritableDatabase().insert(User.getTableName(),null, cValues);
    }

    public boolean doesKeyExists(){
        String query = "SELECT * FROM " + User.getTableName();

        Cursor rowCursor = this.databaseHelperInstance.getWritableDatabase().rawQuery(query, new String[]{});

        if(rowCursor.getCount() == 0){
            rowCursor.close();
            return false;
        }

        rowCursor.close();
        return true;
    }

    public byte[] getKeyForAddress(){
        byte[] blob = new byte[0];

        Cursor rowCursor = this.databaseHelperInstance.getWritableDatabase().rawQuery("SELECT " + User.getKeyPrivateKey() + " FROM " + User.getTableName(), null);

        while (rowCursor.moveToNext()) {
            System.out.println("RowCursor " + rowCursor.getBlob(0).toString());
            blob = rowCursor.getBlob(0);
        }

        rowCursor.close();
        return blob;
    }
}
