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

/**
 * implements SettingsData from android preferences
 */
public class SettingsImpl {
    static final String KEY_ZIPFILE = "zipfile";
    private static String zipfile = null;

    private SettingsImpl() {
    }

    /**
     * Load values from prefs. return true, if zip output dir is writable
     */
    public static boolean init(final Context context) {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        Global.debugEnabled = SettingsImpl.getPrefValue(prefs,
                "isDebugEnabled", Global.debugEnabled);

        // in case first start(no where no zip is defined yet),
        if (SettingsImpl.zipfile == null) {
            SettingsImpl.zipfile = getDefaultZipPath(context);
        }

        SettingsImpl.zipfile = SettingsImpl
                .getPrefValue(prefs, KEY_ZIPFILE,
                        SettingsImpl.zipfile);

        return canWrite(SettingsImpl.zipfile);
    }

    /**
     * calculates the dafault-path value for 2go.zip
     */
    public static String getDefaultZipPath(Context context) {
        Boolean isSDPresent = true; // Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);

        // since android 4.4 Environment.getDataDirectory() and .getDownloadCacheDirectory ()
        // is protected by android-os :-(
        // app will not work on devices with no external storage (sdcard)
        final File rootDir = ((isSDPresent)) ? Environment.getExternalStorageDirectory() : Environment.getRootDirectory();
        final String zipfile = rootDir.getAbsolutePath() + "/" + context.getString(R.string.default_zip_path);
        return zipfile;
    }

    /**
     * return true if outputdirectory of zipfile is writable
     */
    public static boolean canWrite(String zipfile) {
        if ((zipfile == null) || (zipfile.trim().length() == 0)) {
            return false; // empty is no valid path
        }

        File parentDir = new File(zipfile).getParentFile();
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            return false; // parentdir does not exist and cannot be created
        }

        return (parentDir.canWrite());
    }

    /**
     * updates zipFile property in preferences
     */
    public static void setZipfile(final Context context, String zipFile) {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        setValue(prefs, KEY_ZIPFILE, zipFile);
    }

    public static String getZipfile() {
        return SettingsImpl.zipfile;
    }

    /**
     * sets preference value
     */
    private static void setValue(SharedPreferences prefs, String key, String value) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(key, value);
        edit.commit();
        SettingsImpl.zipfile = value;
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

        if ((result == null) || (result.trim().length() == 0)) {
            result = notFoundValue;
            setValue(prefs, key, notFoundValue);
        }
        return result;
    }
}
