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

/**
 * Android-Media related helper functions
 *
 * Created by k3b on 23.11.2014.
 */
public class MediaUtil {
    /*
    Input: URI -- something like content://com.example.app.provider/table2/dataset1
    Output: PATH -- something like /sdcard/DCIM/123242-image.jpg
    */
    public static String convertMediaUriToPath(Context context, Uri uri) {
        Cursor cursor = null;
        try {
            String[] fields = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(uri, fields, null, null, null);
            int column_index = cursor.getColumnIndex(fields[0]);
            if (column_index >= 0) {
                cursor.moveToFirst();
                return cursor.getString(column_index);
            }
        } catch (Exception ignore) {
        } finally {
            cursor.close();
        }
        return null;
    }
}
