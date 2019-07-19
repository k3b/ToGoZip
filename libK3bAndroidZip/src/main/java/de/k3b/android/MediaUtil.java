/*
 * Copyright (C) 2014 k3b
 *
 * This file is part of de.k3b.android.toGoZip (https://github.com/k3b/ToGoZip/) .
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package de.k3b.android;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import de.k3b.io.FileNameUtil;

/**
 * Android-Media related helper functions
 *
 * Created by k3b on 23.11.2014.
 */
public class MediaUtil {
    private static String getString(Context context, Uri uri, String columnName) {
        Cursor cursor = null;
        try {
            String[] fields = {columnName};
            cursor = context.getContentResolver().query(uri, fields, null, null, null);
            if (cursor != null) {
                int column_index = cursor.getColumnIndex(fields[0]);
                if (column_index >= 0) {
                    cursor.moveToFirst();
                    return cursor.getString(column_index);
                }
            }
        } catch (Exception ignore) {
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    private static long getLong(Context context, Uri uri, String columnName) {
        Cursor cursor = null;
        try {
            String[] fields = {columnName};
            cursor = context.getContentResolver().query(uri, fields, null, null, null);
            if (cursor != null) {
                int column_index = cursor.getColumnIndex(fields[0]);
                if (column_index >= 0) {
                    cursor.moveToFirst();
                    return cursor.getLong(column_index);
                }
            }
        } catch (Exception ignore) {
        } finally {
            if (cursor != null) cursor.close();
        }
        return 0;
    }

    /*
    Input: URI -- something like content://com.example.app.provider/table2/dataset1
    Output: PATH -- something like /sdcard/DCIM/123242-image.jpg
    */
    public static String convertMediaUriToPath(Context context, Uri uri) {
        return getString(context, uri, MediaStore.Images.Media.DATA);
    }

    /*
    Input: URI -- something like content://com.example.app.provider/table2/dataset1
    Output: 0 or date
    */
    public static long getDateModified(Context context, Uri uri) {
        return getLong(context, uri, MediaStore.Images.Media.DATE_MODIFIED);
    }

    /*
    Input: URI -- something like content://com.example.app.provider/table2/dataset1
    Output: null or filename (with or without path)
    */
    public static String getFileName(Context context, Uri uri, String mimeType) {
        String baseName = getString(context, uri, MediaStore.Images.Media.DISPLAY_NAME);
        if ((baseName == null) || (baseName.length() == 0)) {
            baseName = uri.getLastPathSegment();

            // "content://com.mediatek.calendarimporter/1282" becomes "calendarimporter_1282"
        }
        String defaultFileExtension = (mimeType != null) ? MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) : null;

        return FileNameUtil.createFileName(baseName, defaultFileExtension);
    }
}
