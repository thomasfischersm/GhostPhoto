package com.playposse.ghostphoto.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * A helper class that accesses the SQLLite instance.
 */
public class GhostPhotoDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "ghostPhotoDb";
    private static final int DB_VERSION = 1;

    public GhostPhotoDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(GhostPhotoContract.PhotoShootTable.SQL_CREATE_TABLE);
        db.execSQL(GhostPhotoContract.PhotoTable.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
