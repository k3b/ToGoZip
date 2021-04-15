/*
 * Copyright (C) 2014-2019 k3b
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
import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import android.util.Log;

import java.io.File;

import de.k3b.LibGlobal;
import de.k3b.android.zip.Global;
import de.k3b.io.StringUtils;
import de.k3b.zip.LibZipGlobal;
import de.k3b.zip.ZipStorage;
import de.k3b.zip.ZipStorageFile;

/**
 * implements SettingsData from android preferences
 */
public class SettingsImpl {
    /** full path of directory where the zipfile is stored as uri
     * either as file(android-4.4. and older) or
     * as documentfile(android-5 and newer) uri. */
    public static final String PREF_KEY_ZIP_DOC_DIR_URI = "zip.dir";
    private static String zipDocDirUri = null;

    /** file name of the zipfile (without path) where "Add To Zip" goes to. */
    private static String zipfile = "2go.zip";
    static final String KEY_ZIPFILE = "zip.file";

    /** #13: if not empty: create subfolders in zip relative to this path. */
    public static final String PREF_KEY_ZIP_REL_PATH = "zip.rel_path";
    private static String zipRelPath = null;

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
        LibGlobal.debugEnabled = Global.debugEnabled;
        LibZipGlobal.debugEnabled = Global.debugEnabled;

        Global.isWriteLogFile2Zip = SettingsImpl.getPrefValue(prefs,
                "isWriteLogFile2Zip", Global.isWriteLogFile2Zip);

        SettingsImpl.zipfile = SettingsImpl
                .getPrefValue(prefs, KEY_ZIPFILE,
                        SettingsImpl.zipfile);

        SettingsImpl.zipDocDirUri = SettingsImpl
                .getPrefValue(prefs, PREF_KEY_ZIP_DOC_DIR_URI,
                        SettingsImpl.zipDocDirUri);

        SettingsImpl.zipRelPath = SettingsImpl
                .getPrefValue(prefs, PREF_KEY_ZIP_REL_PATH,
                        SettingsImpl.zipRelPath);

        fixPathIfNeccessary(context);

        SettingsImpl.textfile_short = SettingsImpl
                .getPrefValue(prefs, KEY_TEXTFILE_SHORT,
                        SettingsImpl.textfile_short);

        SettingsImpl.textfile_long = SettingsImpl
                .getPrefValue(prefs, KEY_TEXTFILE_LONG,
                        SettingsImpl.textfile_long);

        SettingsImpl.textfile_long_min = SettingsImpl
                .getPrefValue(prefs, KEY_TEXTFILE_LONG_MIN,
                        SettingsImpl.textfile_long_min);

        return canWrite(context, SettingsImpl.getZipDocDirUri());
    }

    private static void fixPathIfNeccessary(Context context) {
        // convert from togozip-ver-1 to togozip-ver-2
        if ((SettingsImpl.zipfile != null) && (zipfile.contains("/"))) {
            // old formt of togozip-ver-1.x with path
            // new forman in togozip-ver-2.x dir and filename are seperate
            File f = new File(SettingsImpl.zipfile).getAbsoluteFile();

            SettingsImpl.setZipfile(context, f.getName());
            String path = f.getParent();
            if (path != null) {
                SettingsImpl.setZipDocDirUri(context, path);
            }
        }

        // there must always be a dir. Set to default if it does not exist.
        if (SettingsImpl.zipDocDirUri == null) {
            SettingsImpl.zipDocDirUri = getDefaultZipDirPath(context);
        }
    }

    /**
     * calculates the dafault-path value for 2go.zip
     */
    public static String getDefaultZipDirPath(Context context) {
        File rootDir = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            // before api-14/android-4.4/KITKAT
            // write support on sdcard, if mounted
            Boolean isSDPresent = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
            rootDir = ((isSDPresent)) ? Environment.getExternalStorageDirectory() : Environment.getRootDirectory();
        } else if (Global.USE_DOCUMENT_PROVIDER && (zipDocDirUri != null)) {

            // DocumentFile docDir = DocumentFile.fromTreeUri(context, Uri.parse(zipDocDirUri));
            DocumentFile docDir = DocumentFile.fromFile(new File(zipDocDirUri));
            if ((rootDir != null) && (docDir != null) && docDir.canWrite()) {
                return rootDir.getAbsolutePath();
            }
        }

        if (rootDir == null) {
            // since android 4.4 Environment.getDataDirectory() and .getDownloadCacheDirectory ()
            // is protected by android-os :-(
            rootDir = getRootDir44();
        }

        final String zipfile = rootDir.getAbsolutePath() + "/copy";
        return zipfile;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static File getRootDir44() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    public static ZipStorage getCurrentZipStorage(Context context) {
        return getCurrentStorage(context, SettingsImpl.zipfile);
    }

    public static ZipStorage getCurrentStorage(Context context, String baseFileName) {
        if (Global.USE_DOCUMENT_PROVIDER) {
            DocumentFile docDir = getDocFile(context, zipDocDirUri);
            return new de.k3b.android.zip.ZipStorageDocumentFile(context, docDir, baseFileName);

        } else {
            File absoluteZipFile = getAbsoluteFile(baseFileName);
            return new ZipStorageFile(absoluteZipFile.getAbsolutePath());
        }
    }


    /**
     * return true if outputdirectory of zipfile is writable
     */
    public static boolean canWrite(Context context, String dir) {
        if ((dir == null) || (dir.trim().length() == 0)) {
            return false; // empty is no valid path
        }

        if (Global.USE_DOCUMENT_PROVIDER) {
            DocumentFile docDir = getDocFile(context, dir);
            return ((docDir != null) && (docDir.exists()) && docDir.canWrite());
        }

        File fileDir = new File(dir);
        if ((fileDir == null) || (!fileDir.exists() && !fileDir.mkdirs())) {
            return false; // parentdir does not exist and cannot be created
        }

        return true; // (parentDir.canWrite());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static DocumentFile getDocFile(Context context, @NonNull String dir ) {
        DocumentFile docDir = null;

        if (dir.contains(":")) {
            Uri uri = Uri.parse(dir);

            if ("file".equals(uri.getScheme())) {
                String path = uri.getPath();
                if (path == null) return null;
                File fileDir = new File(path);
                docDir = DocumentFile.fromFile(fileDir);
            } else {
                docDir = DocumentFile.fromTreeUri(context, uri);
            }
        } else {
            docDir = DocumentFile.fromFile(new File(dir));
        }
        return docDir;

    }

    /**
     * updates zipFile property in preferences
     */
    private static void setZipfile(final Context context, String zipFile) {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        setValue(prefs, KEY_ZIPFILE, zipFile);
        SettingsImpl.zipfile = zipFile;
    }

    /** full path of the zipfile where "Add To Zip" goes to. */
    public static String getZipFile() {
        return zipfile;
    }

    public static File getAbsoluteZipFile() {
        return getAbsoluteFile(SettingsImpl.zipfile);
    }

    /** full path of the zipfile where "Add To Zip" goes to. */
    protected static File getAbsoluteFile(String fileName) {
        return new File(zipDocDirUri, fileName);
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
        } catch (final Exception ex) {
            // ClassCastException or NumberFormatException

            Log.w(Global.LOG_CONTEXT, "getPrefValue-Integer(" + key + ","
                    + notFoundValue + ") failed: " + ex.getMessage());
            return notFoundValue;
        }
    }

    private static boolean getPrefValue(final SharedPreferences prefs,
                                        final String key, final boolean notFoundValue) {
        try {
            return prefs.getBoolean(key, notFoundValue);
        } catch (final Exception ex) {
            // ClassCastException or FormatException
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

    /** full path of the zipfile where "Add To Zip" goes to. */
    public static String getZipRelPath() {
        return zipRelPath;
    }
    public static File getZipRelPathAsFile() {
        String rel = getZipRelPath();
        if (StringUtils.isNullOrEmpty((CharSequence) rel)) return null;
        return new File(rel);
    }

    public static void setZipRelPath(final Context context, String zipRelPath) {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        setValue(prefs, PREF_KEY_ZIP_REL_PATH, zipRelPath);
        SettingsImpl.zipRelPath = zipRelPath;
    }
	
	    /** full path of the zipfile where "Add To Zip" goes to. */
    public static String getZipDocDirUri() {
        return zipDocDirUri;
    }

    public static void setZipDocDirUri(final Context context, String zipDocDirUri) {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        setValue(prefs, PREF_KEY_ZIP_DOC_DIR_URI, zipDocDirUri);
        SettingsImpl.zipDocDirUri = zipDocDirUri;
    }

}
