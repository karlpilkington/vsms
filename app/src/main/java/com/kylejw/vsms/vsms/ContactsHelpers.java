package com.kylejw.vsms.vsms;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

/**
 * Created by Kyle on 1/31/2015.
 */
public class ContactsHelpers {
    public static String displayNameFromContactNumber(Context context, String contactNumber)
    {
        if (null == context) return contactNumber;

        if (null == contactNumber || contactNumber.isEmpty()) {
            return "";
        }

        final String[] projection = new String[] { ContactsContract.Contacts.DISPLAY_NAME };

        Uri lkup = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(contactNumber));
        Cursor idCursor = context.getContentResolver().query(lkup, projection, null, null, null);

        try {
            while (idCursor.moveToNext()) {
                return idCursor.getString(idCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            }
        } finally {
            if (null != idCursor)
            {
                idCursor.close();
            }
        }

        return contactNumber;
    }
}
