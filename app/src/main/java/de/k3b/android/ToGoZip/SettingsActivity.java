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
import android.support.v4.app.ActivityCompat;
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
public class SettingsActivity extends PreferenceActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback  {

    private static final int REQUEST_CODE_GET_ZIP_DIR = 12;
    private static final int FOLDERPICKER_CODE = 1234;

    private SharedPreferences prefsInstance = null;
    private ListPreference defaultLocalePreference;  // #6: Support to change locale at runtime

    // #8: pick folder
    private Preference folderPickerPreference = null;
    /**
     * public api to start settings-activity
     */
    public static void startActivityForResult(Activity activity, int requestCode) {
        final Intent i = new Intent(activity, SettingsActivity.class);

        if (Global.debugEnabled) {
            Log.d(Global.LOG_CONTEXT, "SettingsActivity.show(startActivity='" + i
                    + "')");
        }

        if (requestCode == 0) {
            activity.startActivity(i);
        } else {
            activity.startActivityForResult(i, requestCode);
        }

    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        LocalizedActivity.fixLocale(this);	// #6: Support to change locale at runtime
        super.onCreate(savedInstanceState);
        SettingsImpl.init(this);
        setResult(RESULT_CANCELED, null);
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

        boolean canWrite = PermissionHelper.hasPermission(this) && SettingsImpl.canWrite(this, SettingsImpl.getZipDocDirUri());

        if (canWrite) {
            checkRuntimePermission();
        } else {
            showNeedPermissionDialog();
        }
    }

    /**
     * return false if no error. else Show Dialog cancel/setToDefault/Edit
     */
    private void showNeedPermissionDialog() {
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

        builder.setPositiveButton(R.string.cmd_edit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkRuntimePermission();
            }
        });

        builder.show();
    }

    private void checkRuntimePermission() {
        if (PermissionHelper.hasPermissionOrRequest(this)) {
            checkDirPermission();
        } // else requestPermission
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (PermissionHelper.receivedPermissionsOrFinish(this, requestCode, permissions, grantResults)) {
            checkDirPermission();
        } else {
            cancel();
        }
    }

    private void checkDirPermission() {
        if (!SettingsImpl.canWrite(this, SettingsImpl.getZipDocDirUri())) {
            onCmdPickFolder();
        } else {
            setResult(RESULT_OK, null);
        }
    }

    // #6: Support to change locale at runtime
    // This is used to show the status of some preference in the description
    private void updateSummary() {
        final String languageKey = prefsInstance.getString(Global.PREF_KEY_USER_LOCALE, "");
        setLanguage(languageKey);
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
        setResult(RESULT_OK, null);
    }

    /**
     * cancel from Dialog cancels SettingsActivity
     */
    private void cancel() {
        setResult(RESULT_CANCELED);
        PermissionHelper.showNowPermissionMessage(this);
        finish();
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
