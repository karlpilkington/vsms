package com.kylejw.vsms.vsms.Database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by Kyle on 1/25/2015.
 */
public class SmsMessageTable {
    public static final String TABLE_MESSAGES = "smsMessages";
    public static final String COLUMN_INTERNAL_ID = "_id";
    public static final String COLUMN_VOIPMS_ID = "voipms_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_DID = "did";
    public static final String COLUMN_CONTACT = "contact";
    public static final String COLUMN_MESSAGE = "message";

    private static final String DATABASE_CREATE  = "CREATE TABLE " + TABLE_MESSAGES + "("
            + COLUMN_INTERNAL_ID + " INTEGER PRIMARY KEY,"
            + COLUMN_VOIPMS_ID + " INTEGER UNIQUE,"
            + COLUMN_DATE + " INTEGER,"
            + COLUMN_TYPE + " STRING,"
            + COLUMN_DID + " TEXT,"
            + COLUMN_CONTACT + " TEXT,"
            + COLUMN_MESSAGE + " TEXT"
            + ")";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        Log.w(SmsMessageTable.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(database);
    }
}
