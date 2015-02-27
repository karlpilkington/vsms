package com.kylejw.vsms.vsms.Database;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.kylejw.vsms.vsms.VoipMsApi.DataModel.SmsMessage;
import com.kylejw.vsms.vsms.VoipMsApi.Exchanges.GetSms;
import com.kylejw.vsms.vsms.VoipMsApi.VoipMsRequest;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;

/**
 * Created by Kyle on 1/25/2015.
 */
public class SmsMessageContentProvider extends ContentProvider {

    private SmsMessageDatabaseHelper database;

    private static final int MESSAGES = 10;
    private static final int MESSAGE_ID = 20;
    private static final int CONTACT = 30;
    private static final int CONVERSATIONS = 40;

    private static final String AUTHORITY = "com.kylejw.vsms.vsms.Database.contentprovider";

    private static final String BASE_PATH = "SmsMessages";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
    public static final Uri CONTACT_URI = Uri.parse(CONTENT_URI + "/contact");
    public static final Uri CONVERSATIONS_URI = Uri.parse(CONTENT_URI + "/" + CONVERSATIONS);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/SmsMessages";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/SmsMessages";

    private static final String PREFERENCES = "VoipMsPreferences";
    private static final String LAST_VOIP_MS_UPDATE_PREF = "lastVoipMsUpdate";

    private static Calendar getLastVoipMsUpdate(Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);

        if (!prefs.contains(LAST_VOIP_MS_UPDATE_PREF))
            return null;

        long msec = prefs.getLong(LAST_VOIP_MS_UPDATE_PREF, -1);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(msec);

        return cal;
    }

    public static void setLastVoipMsUpdate(Context context, Calendar updateTime)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong(LAST_VOIP_MS_UPDATE_PREF, updateTime.getTimeInMillis());
        editor.commit();
    }

    public static void refresh(final Context context)
    {
        Calendar lastUpdate = getLastVoipMsUpdate(context);
        if (null != lastUpdate) {
            lastUpdate.add(Calendar.DAY_OF_YEAR, -1);
        }

        final Calendar now = Calendar.getInstance();

        GetSms request = new GetSms(lastUpdate, now);
        request.executeAsync(new VoipMsRequest.VoipMsCallback<GetSms.GetSmsResponse>() {
            @Override
            public void onComplete(GetSms.GetSmsResponse getSmsResponse) {

                if (!getSmsResponse.getStatus().equals("success")) return;

                setLastVoipMsUpdate(context, now);

                // SmsMessageDatabaseHandler smdh = new SmsMessageDatabaseHandler(context);
                ContentResolver resolver = context.getContentResolver();
                Uri insertUri = SmsMessageContentProvider.CONTENT_URI;

                for (SmsMessage msg : getSmsResponse.getSms())
                {
                    ContentValues values = new ContentValues();
                    values.put(SmsMessageTable.COLUMN_CONTACT, msg.getContact());
                    values.put(SmsMessageTable.COLUMN_DATE, msg.getDate());
                    values.put(SmsMessageTable.COLUMN_DID, msg.getDid());
                    values.put(SmsMessageTable.COLUMN_MESSAGE, msg.getMessage());
                    values.put(SmsMessageTable.COLUMN_TYPE, msg.getMessageType().toString());
                    values.put(SmsMessageTable.COLUMN_VOIPMS_ID, msg.getId());

                    Uri msgUri = resolver.insert(insertUri, values);
                    //     smdh.addMessage(msg);
                }
            }
        });
    }

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, MESSAGES);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", MESSAGE_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/contact/#", CONTACT);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", CONVERSATIONS);
    }

    @Override
    public boolean onCreate() {
        database = new SmsMessageDatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        //checkColumns(projection);

        queryBuilder.setTables(SmsMessageTable.TABLE_MESSAGES);

        String groupBy = null;

        int uriType = sURIMatcher.match(uri);
        switch(uriType) {
            case MESSAGES:
                break;
            case MESSAGE_ID:
                queryBuilder.appendWhere(SmsMessageTable.COLUMN_INTERNAL_ID + "=" + uri.getLastPathSegment());
                break;
            case CONTACT:
                queryBuilder.appendWhere(SmsMessageTable.COLUMN_CONTACT + "=" + uri.getLastPathSegment());
                break;
            case CONVERSATIONS:
                groupBy = SmsMessageTable.COLUMN_CONTACT;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(
                db,
                projection,
//                new String[] {SmsMessageTable.COLUMN_INTERNAL_ID, SmsMessageTable.COLUMN_CONTACT, SmsMessageTable.COLUMN_MESSAGE, SmsMessageTable.COLUMN_DATE, "COUNT(" + SmsMessageTable.COLUMN_MESSAGE + ") as count"},
                selection,
                selectionArgs,
                groupBy,
                null,
                sortOrder
        );
//        Cursor cursor = queryBuilder.query(
//                db,
//                new String[] {SmsMessageTable.COLUMN_INTERNAL_ID, SmsMessageTable.COLUMN_CONTACT, SmsMessageTable.COLUMN_MESSAGE, "MAX(" + SmsMessageTable.COLUMN_DATE + ") as date"},
//                null,
//                null,
//                SmsMessageTable.COLUMN_CONTACT,
//                null,
//                SmsMessageTable.COLUMN_DATE + " desc"
//        );

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        long id = 0;
        switch(uriType) {
            case MESSAGES:
                id = sqlDB.insertWithOnConflict(SmsMessageTable.TABLE_MESSAGES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType) {
            case MESSAGES:
                rowsDeleted = sqlDB.delete(SmsMessageTable.TABLE_MESSAGES, selection,
                        selectionArgs);
                break;
            case MESSAGE_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(SmsMessageTable.TABLE_MESSAGES,
                            SmsMessageTable.COLUMN_INTERNAL_ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(SmsMessageTable.TABLE_MESSAGES,
                            SmsMessageTable.COLUMN_INTERNAL_ID + "=" + id
                                    + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
            case MESSAGES:
                rowsUpdated = sqlDB.update(SmsMessageTable.TABLE_MESSAGES,
                        values,
                        selection,
                        selectionArgs);
                break;
            case MESSAGE_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(SmsMessageTable.TABLE_MESSAGES,
                            values,
                            SmsMessageTable.COLUMN_INTERNAL_ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(SmsMessageTable.TABLE_MESSAGES,
                            values,
                            SmsMessageTable.COLUMN_INTERNAL_ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        String[] available = { SmsMessageTable.COLUMN_CONTACT,
                SmsMessageTable.COLUMN_DATE, SmsMessageTable.COLUMN_DID,
                SmsMessageTable.COLUMN_INTERNAL_ID, SmsMessageTable.COLUMN_MESSAGE,
                SmsMessageTable.COLUMN_TYPE, SmsMessageTable.COLUMN_VOIPMS_ID
        };
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }
}
