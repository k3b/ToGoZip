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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import java.io.File;

import de.k3b.io.ZipStorage;
import de.k3b.io.ZipStorageFile;

/**
 * implements SettingsData from android preferences
 */
public class SettingsImpl {
    /** full path of the zipfile where "Add To Zip" goes to. */
    private static String zipDocDirUri = null;
    static final String KEY_ZIPDIR = "zip.dir";

    /** full path of the zipfile where "Add To Zip" goes to. */
    private static String zipfile = null;
    static final String KEY_ZIPFILE = "zip.file";

    /** short texts like urls are prepended to this zip entry */
    private static String textfile_short = "texts.txt";
    static final String KEY_TEXTFILE_SHORT = "textfile_short";

    /** longe texts like editor-content are added as this new zip entry */
    private static String textfile_long = "text.txt";
    static final String KEY_TEXTFILE_LONG = "textfile_long";

    /** texts longer than this value go into the zipentry for long texts. */
    private static int textfile_long_min = 150;
    static final String KEY_TEXTFILE_LONG_MIN = "textfile_long_min";

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

        SettingsImpl.textfile_short = SettingsImpl
                .getPrefValue(prefs, KEY_TEXTFILE_SHORT,
                        SettingsImpl.textfile_short);

        SettingsImpl.textfile_long = SettingsImpl
                .getPrefValue(prefs, KEY_TEXTFILE_LONG,
                        SettingsImpl.textfile_long);

        SettingsImpl.textfile_long_min = SettingsImpl
                .getPrefValue(prefs, KEY_TEXTFILE_LONG_MIN,
                        SettingsImpl.textfile_long_min);

        SettingsImpl.zipfile = SettingsImpl
                .getPrefValue(prefs, KEY_ZIPFILE,
                        SettingsImpl.zipfile);

        return canWrite(SettingsImpl.zipfile);
    }

    /**
     * calculates the dafault-path value for 2go.zip
     */
    public static String getDefaultZipPath(Context context) {
        File rootDir = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            // before api-14/android-4.4/KITKAT
            // write support on sdcard, if mounted
            Boolean isSDPresent = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
            rootDir = ((isSDPresent)) ? Environment.getExternalStorageDirectory() : Environment.getRootDirectory();
        } else if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) && (zipDocDirUri != null)) {
            DocumentFile docDir = DocumentFile.fromTreeUri(context, Uri.parse(zipDocDirUri));
            if (docDir != null) {
                // DocumentFile.fromFile()
                // docDir.
            }
        }

        if (rootDir == null) {
            // since android 4.4 Environment.getDataDirectory() and .getDownloadCacheDirectory ()
            // is protected by android-os :-(
            rootDir = getRootDir44();
        }

        final String zipfile = rootDir.getAbsolutePath() + "/" + context.getString(R.string.default_zip_path);
        return zipfile;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static File getRootDir44() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }
    /**
     * return true if outputdirectory of zipfile is writable
     */
    public static boolean canWrite(String zipfile) {
        if ((zipfile == null) || (zipfile.trim().length() == 0)) {
            return false; // empty is no valid path
        }

        File parentDir = new File(zipfile).getParentFile();
        if ((parentDir == null) || (!parentDir.exists() && !parentDir.mkdirs())) {
            return false; // parentdir does not exist and cannot be created
        }

        return true; // (parentDir.canWrite());
    }

    /**
     * updates zipFile property in preferences
     */
    public static void setZipfile(final Context context, String zipFile) {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        setValue(prefs, KEY_ZIPFILE, zipFile);
    }

    /** full path of the zipfile where "Add To Zip" goes to. */
    public static String getZipfile() {
        return SettingsImpl.zipfile;
    }

    public static ZipStorage getCurrentZipStorage() {
        return new ZipStorageFile(SettingsImpl.getZipfile());
    }

    public static String getTextfile(boolean useLongTextFile) {
        if (useLongTextFile) return getTextfile_long();
        return getTextfile_short();
    }

    public static boolean useLongTextFile(int length) {
        return length >= getTextfile_long_min();
    }

    /** short texts like urls are prepended to this zip entry */
    public static String getTextfile_short() {
        return SettingsImpl.textfile_short;
    }

    /** longe texts like editor-content are added as this new zip entry */
    public static String getTextfile_long() {
        return SettingsImpl.textfile_long;
    }

    /** texts longer than this value go into the zipentry for long texts. */
    public static int getTextfile_long_min() {
        return SettingsImpl.textfile_long_min;
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
