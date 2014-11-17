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
package de.k3b.android.toGoZip;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;

public class SettingsImpl {
    /**
     * data of the one and only SettingsImpl instance.
     */
    private static SettingsImpl ourInstance = new SettingsImpl();
    private static String zipfile = "";

    private SettingsImpl() {
    }

    public static void init(final Context context) {
        if ((SettingsImpl.zipfile == null) || (SettingsImpl.zipfile.trim().length() == 0)) {
            Boolean isSDPresent = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);

            // since android 4.4 Environment.getDataDirectory() and .getDownloadCacheDirectory () 
			// is protected by android-os :-(
			// app will not work on devices with no external storage (sdcard)
            final File rootDir = ((isSDPresent)) ? Environment.getExternalStorageDirectory() : Environment.getRootDirectory();
            SettingsImpl.zipfile = rootDir.getAbsolutePath() + "/" + context.getString(R.string.default_zip_path);
        }

        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        SettingsImpl.zipfile = SettingsImpl
                .getPrefValue(prefs, "zipfile",
                        SettingsImpl.zipfile);

        Global.debugEnabled = SettingsImpl.getPrefValue(prefs,
                "isDebugEnabled", Global.debugEnabled);
    }

    public static SettingsImpl getInstance() {
        return ourInstance;
    }

    /**
     * Since this value comes from a text-editor it is stored as string.
     * Conversion to int must be done yourself.
     */
    private static int getPrefValue(final SharedPreferences prefs,
                                    final String key, final int notFoundValue) {
        try {
            return Integer.parseInt(prefs.getString(key,
                    Integer.toString(notFoundValue)));
        } catch (final ClassCastException ex) {

            Log.w(Global.LOG_CONTEXT, "getPrefValue-Integer(" + key + ","
                    + notFoundValue + ") failed: " + ex.getMessage());
            return notFoundValue;
        }
    }

    private static boolean getPrefValue(final SharedPreferences prefs,
                                        final String key, final boolean notFoundValue) {
        try {
            return prefs.getBoolean(key, notFoundValue);
        } catch (final ClassCastException ex) {
            Log.w(Global.LOG_CONTEXT, "getPrefValue-Boolean(" + key + ","
                    + notFoundValue + ") failed: " + ex.getMessage());
            return notFoundValue;
        }
    }

    private static String getPrefValue(final SharedPreferences prefs,
                                       final String key, final String notFoundValue) {
        String result = prefs.getString(key, null);

        if (result == null) {
            result = notFoundValue;
            SharedPreferences.Editor prefEditor = prefs.edit();
            prefEditor.putString(key, notFoundValue);
            prefEditor.commit();
        }
        return result;
    }

    public static String getZipfile() {
        return SettingsImpl.zipfile;
    }

    public static void setZipfile(final String value) {
        SettingsImpl.zipfile = value;
    }
}
