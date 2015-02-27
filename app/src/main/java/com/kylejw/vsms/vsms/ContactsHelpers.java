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
//        Uri lkup = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, Uri.encode(contactNumber));
        Uri lkup = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(contactNumber));
        Cursor idCursor = context.getContentResolver().query(lkup, projection, null, null, null);

        final int index = 0;//idCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

        try {
            while (idCursor.moveToNext()) {
                return idCursor.getString(index);
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
