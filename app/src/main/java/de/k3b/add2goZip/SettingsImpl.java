package de.k3b.add2GoZip;

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
            final File sdcard = Environment.getExternalStorageDirectory();
            SettingsImpl.zipfile = sdcard.getAbsolutePath() + "/" + context.getString(R.string.default_zip_path);
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
