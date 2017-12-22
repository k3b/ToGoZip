/*
 * Copyright (C) 2014-2018 k3b
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
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import android.support.v4.provider.DocumentFile;

import java.io.File;

import de.k3b.android.AndroidCompressJob;
import de.k3b.android.widget.LocalizedActivity;
import de.k3b.zip.CompressItem;
import de.k3b.zip.ZipLog;
import de.k3b.zip.ZipLogImpl;
import lib.folderpicker.FolderPicker;

/**
 * show settings/config activity. On Start and Exit checks if data is valid.
 */
public class SettingsActivity extends PreferenceActivity {

    private static final int REQUEST_CODE_GET_ZIP_DIR = 12;
    private static final int FOLDERPICKER_CODE = 1234;
    /**
     * if not null: try to execute add2zip on finish
     */
    private static CompressItem[] filesToBeAdded = null;
    private static String textToBeAdded = null;
    private AndroidCompressJob job = null;

    private SharedPreferences prefsInstance = null;
    private ListPreference defaultLocalePreference;  // #6: Support to change locale at runtime

    // #8: pick folder
    private Preference folderPickerPreference = null;
    /**
     * public api to start settings-activity
     */
    public static void show(Context context, CompressItem[] filesToBeAdded, String textToBeAdded) {
        final Intent i = new Intent(context, SettingsActivity.class);

        if (Global.debugEnabled) {
            Log.d(Global.LOG_CONTEXT, "SettingsActivity.show(startActivity='" + i
                    + "')");
        }

        SettingsActivity.filesToBeAdded = filesToBeAdded;
        SettingsActivity.textToBeAdded = textToBeAdded;
        context.startActivity(i);

    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        LocalizedActivity.fixLocale(this);	// #6: Support to change locale at runtime
        super.onCreate(savedInstanceState);
        SettingsImpl.init(this);
        ZipLog zipLog = new ZipLogImpl(Global.debugEnabled);

        this.job = new AndroidCompressJob(this, zipLog);
        this.job.setDestZipFile(SettingsImpl.getCurrentZipStorage(this));

        this.addPreferencesFromResource(R.xml.preferences);

        prefsInstance = PreferenceManager
                .getDefaultSharedPreferences(this);
        // #6: Support to change locale at runtime
        defaultLocalePreference =
                (ListPreference) findPreference(Global.PREF_KEY_USER_LOCALE);
        defaultLocalePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                setLanguage((String) newValue);
                LocalizedActivity.recreate(SettingsActivity.this);
                return true; // change is allowed
            }
        });

        folderPickerPreference = (Preference) findPreference(SettingsImpl.PREF_KEY_ZIP_DOC_DIR_URI);
        folderPickerPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                return onCmdPickFolder();
            }
        });
        folderPickerPreference.setSummary(SettingsImpl.getZipDocDirUri());
        // #6: Support to change locale at runtime
        updateSummary();

        showAlertOnError();
    }

    private boolean onCmdPickFolder() {
        CharSequence folder = folderPickerPreference.getSummary();

        if (!Global.USE_DOCUMENT_PROVIDER) {
            Intent intent = new Intent(SettingsActivity.this, FolderPicker.class);
            if ((folder != null) && (folder.length() > 0)) {
                intent.putExtra("location", folder); // initial dir
            }
            startActivityForResult(intent, FOLDERPICKER_CODE);
        } else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(intent, FOLDERPICKER_CODE);
        }
        return true;
    }

    private void onFolderPickResult(String folderLocation) {
        if (Global.debugEnabled) {
            Log.d(Global.LOG_CONTEXT, "Picked folder " + folderLocation);
        }
        SettingsImpl.setZipDocDirUri(this, folderLocation);
        folderPickerPreference.setSummary(folderLocation);
    }

    /**
     * return false if no error. else Show Dialog cancel/setToDefault/Edit
     */
    private boolean showAlertOnError() {
        boolean canWriteCurrent = SettingsImpl.init(this);

        if (!canWriteCurrent) {
            String currentZipPath = SettingsImpl.getAbsoluteZipFile().getAbsolutePath();
            String defaultZipPath = SettingsImpl.getDefaultZipDirPath(this);
            boolean canWriteDefault = SettingsImpl.canWrite(this, defaultZipPath);

            String format = (canWriteDefault)
                    ? getString(R.string.ERR_NO_WRITE_PERMISSIONS_CHANGE_TO_DEFAULT)
                    : getString(R.string.ERR_NO_WRITE_PERMISSIONS);

            if (defaultZipPath.compareTo(currentZipPath) == 0) {
                currentZipPath = ""; // display name only once
            }
            String msg = String.format(
                    format,
                    currentZipPath,
                    defaultZipPath);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(msg);
            builder.setTitle(R.string.title_activity_add2zip);
            builder.setIcon(R.drawable.ic_launcher);
            //builder.setPositiveButton(R.string.delete, this);
            //builder.setNegativeButton(R.string.cancel, this);

            if (canWriteDefault) {
                builder.setNeutralButton(R.string.cmd_use_default, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setDefault();
                    }
                });
            }

            builder.setNegativeButton(R.string.cmd_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    cancel();
                }
            });
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    cancel();
                }
            });

            builder.setPositiveButton(R.string.cmd_edit, null);

            builder.show();
        }
        return !canWriteCurrent;
    }

    /**
     * android os function to end this activity. Hooked to verify that data is valid.
     */
    @Override
    public void finish() {
        if (Global.debugEnabled) {
            Log.d(Global.LOG_CONTEXT, "SettingsActivity.finish");
        }

        if (!showAlertOnError()) {
            finishWithoutCheck();
        }
    }

    /**
     * executes finish without checking validity. Executes add2Zip source-files are available.
     */
    private void finishWithoutCheck() {
        if ((SettingsActivity.textToBeAdded != null) || (SettingsActivity.filesToBeAdded != null)) {
            SettingsImpl.init(this);
            job.addToZip(textToBeAdded, SettingsActivity.filesToBeAdded);
            SettingsActivity.filesToBeAdded = null;
        }
        super.finish();
    }

    /**
     * resets zip to default and restart settings activity.
     */
    private void setDefault() {
        String defaultZipPath = SettingsImpl.getDefaultZipDirPath(this);
        SettingsImpl.setZipDocDirUri(this, defaultZipPath);
        CompressItem[] fileToBeAdded = SettingsActivity.filesToBeAdded;
        String textToBeAdded = SettingsActivity.textToBeAdded;
        SettingsActivity.filesToBeAdded = null; // do not start add2zip
        SettingsActivity.textToBeAdded = null;
        finishWithoutCheck();
        // restart with new settings
        show(this, fileToBeAdded, textToBeAdded);
    }

    /**
     * cancel from Dialog cancels SettingsActivity
     */
    private void cancel() {
        if ((SettingsActivity.textToBeAdded != null) || (SettingsActivity.filesToBeAdded != null)) {
            Toast.makeText(this, getString(R.string.WARN_ADD_CANCELED), Toast.LENGTH_LONG).show();
            SettingsActivity.filesToBeAdded = null;
            SettingsActivity.textToBeAdded = null;
        }

        finishWithoutCheck();
    }

    // #6: Support to change locale at runtime
    // This is used to show the status of some preference in the description
    private void updateSummary() {
        final String languageKey = prefsInstance.getString(Global.PREF_KEY_USER_LOCALE, "");
        setLanguage(languageKey);
    }

    // #6: Support to change locale at runtime
    private void setLanguage(String languageKey) {
        setPref(languageKey, defaultLocalePreference, R.array.pref_locale_names);
    }

    private void setPref(String key, ListPreference listPreference, int arrayResourceId) {
        int index = listPreference.findIndexOfValue(key);
        String summary = "";

        if (index >= 0) {
            String[] names = this.getResources().getStringArray(arrayResourceId);
            if (index < names.length) {
                summary = names[index];
            }
        }
        listPreference.setSummary(summary);

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void requestZipDir(Activity ctx, File formerZipDir) {
        final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

        /*
        if (formerZipDir != null) {
            // since API level 26
            DocumentFile docDir = DocumentFile.fromFile(formerZipDir);
            intent.putExtra(Intent.EXTRA_INITIAL_URI, docDir.getUri().toString());
        }
        */

        ctx.startActivityForResult(intent, REQUEST_CODE_GET_ZIP_DIR);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_GET_ZIP_DIR && resultCode == RESULT_OK && Global.USE_DOCUMENT_PROVIDER) {
            grandPermission5(this, data.getData());
        }

        if (requestCode == FOLDERPICKER_CODE && resultCode == Activity.RESULT_OK) {
            if (Global.USE_DOCUMENT_PROVIDER) {
                Uri uri = data.getData();
                onFolderPickResult(uri.toString());
            } else {
                // folder picker specific implementation
                String folderLocation = data.getExtras().getString("data");
                onFolderPickResult(folderLocation);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void grandPermission5(Context ctx, Uri data) {
        DocumentFile docPath = DocumentFile.fromTreeUri(ctx, data);
        if (docPath != null) {
            final ContentResolver resolver = ctx.getContentResolver();
            resolver.takePersistableUriPermission(data,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        }
    }

}
