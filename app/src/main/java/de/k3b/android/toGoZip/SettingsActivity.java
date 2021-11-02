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
import androidx.core.app.ActivityCompat;
import android.util.Log;
import androidx.documentfile.provider.DocumentFile;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;

import de.k3b.android.util.FileManagerUtil;
import de.k3b.android.widget.AboutDialogPreference;
import de.k3b.android.widget.LocalizedActivity;
import de.k3b.android.zip.Global;
import de.k3b.zip.ZipStorage;
import lib.folderpicker.FolderPicker;

/**
 * show settings/config activity. On Start and Exit checks if data is valid.
 */
public class SettingsActivity extends PreferenceActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback  {

    private static final int REQUEST_CODE_GET_ZIP_DIR = 12;
    private static final int FOLDERPICKER_CODE = 1234;
    private static final int ZIP_SUB_FOLDERPICKER_CODE = 1235;

    /** false: old style folder picker for subfolder; true use new android Docprovider-Folder-picker */
    private static boolean allowDocProvider4Subfolder = false;

    private SharedPreferences prefsInstance = null;
    private ListPreference defaultLocalePreference;  // #6: Support to change locale at runtime

	// #8: pick folder
    private Preference folderPickerPreference = null;

    // #13: pick zip rel path folder
    private Preference zipRelPathFolderPickerPreference = null;
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

        zipRelPathFolderPickerPreference = (Preference) findPreference(SettingsImpl.PREF_KEY_ZIP_REL_PATH);
        zipRelPathFolderPickerPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                return onCmdPickRelPathFolder();
            }
        });
        zipRelPathFolderPickerPreference.setSummary(getZipRelPathSummary());

        folderPickerPreference = (Preference) findPreference(SettingsImpl.PREF_KEY_ZIP_DOC_DIR_URI);
        folderPickerPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                return onCmdPickFolder();
            }
        });
        folderPickerPreference.setSummary(SettingsImpl.getZipDocDirUri());

        findPreference("debugClearLog").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                onDebugClearLogCat();
                return false; // donot close
            }
        });
        findPreference("debugSaveLog").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                onDebugSaveLogCat();
                return false; // donot close
            }
        });


        // #6: Support to change locale at runtime
        updateSummary();

        boolean canWrite = PermissionHelper.hasPermission(this) && SettingsImpl.canWrite(this, SettingsImpl.getZipDocDirUri());

        if (canWrite) {
            checkRuntimePermission();
        } else {
            showNeedPermissionDialog();
        }
    }

    private String getZipRelPathSummary() {
        String zipRelPath = SettingsImpl.getZipRelPath();
        return (zipRelPath == null)
                ? getString(R.string.pref_short_text_zip_rel_path_disabled_summayr)
                : getString(R.string.pref_short_text_zip_rel_path_enabled_summayr, zipRelPath)
                ;
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

    private boolean onCmdPickRelPathFolder() {
        try {
            return openPicker(SettingsImpl.getZipRelPath(), ZIP_SUB_FOLDERPICKER_CODE,
                    Global.USE_DOCUMENT_PROVIDER && allowDocProvider4Subfolder);
        } catch (RuntimeException ex) {
            final String msg = "onCmdPickRelPathFolder('" + SettingsImpl.getZipRelPath() +
                    "')";
            Log.e(Global.LOG_CONTEXT, msg, ex);

            // do not use old api again
            allowDocProvider4Subfolder = true;
            throw new RuntimeException(msg, ex);
        }
    }

    private void onZipRelPathFolderPickResult(String folderLocation, boolean ok) {
        if (Global.debugEnabled) {
            Log.d(Global.LOG_CONTEXT, "Picked ZipRelPathFolder " + ok + " " + folderLocation);
        }
        SettingsImpl.setZipRelPath(this, (ok) ? folderLocation : null);
        zipRelPathFolderPickerPreference.setSummary(getZipRelPathSummary());
        setResult(RESULT_OK, null);
    }

    private boolean onCmdPickFolder() {
        return openPicker(folderPickerPreference.getSummary(), FOLDERPICKER_CODE, Global.USE_DOCUMENT_PROVIDER);
    }

    private boolean openPicker(CharSequence oldFolder, int folderpickerCode, boolean useDocumentProvider) {
        if (!useDocumentProvider) {
            Intent intent = new Intent(SettingsActivity.this, FolderPicker.class);
            if ((oldFolder != null) && (oldFolder.length() > 0)) {
                intent.putExtra("location", oldFolder); // initial dir
            }
            startActivityForResult(intent, folderpickerCode);
        } else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                    | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
            startActivityForResult(intent, folderpickerCode);
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

    private void onDebugClearLogCat() {
        ((ToGoZipApp) getApplication()).clear();
        Toast.makeText(this, R.string.settings_debug_clear_title, Toast.LENGTH_SHORT).show();
        Log.e(Global.LOG_CONTEXT, "SettingsActivity-ClearLogCat()");
    }

    private void onDebugSaveLogCat() {
        Log.e(Global.LOG_CONTEXT, "SettingsActivity-SaveLogCat(): ");
        ((ToGoZipApp) getApplication()).saveToFile();
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

    private String getPickerUriAsString(Intent data, boolean useDocumentProvider, boolean convert2File) {
        if (data != null) {
            if (useDocumentProvider) {
                Uri uri = data.getData();
                if (uri != null) {
                    return (convert2File)
                            ? de.k3b.android.zip.ZipStorageDocumentFile.getPath(this, uri)
                            : uri.toString();
                }
            } else {
                // folder picker specific implementation
                final Bundle extras = data.getExtras();
                if (extras != null) return extras.getString("data");
            }
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_GET_ZIP_DIR && resultCode == RESULT_OK && Global.USE_DOCUMENT_PROVIDER) {
            grandPermission5(this, data.getData());
        }

        if (requestCode == FOLDERPICKER_CODE && resultCode == Activity.RESULT_OK) {
            final String folderLocation = getPickerUriAsString(data, Global.USE_DOCUMENT_PROVIDER, false);
            onFolderPickResult(folderLocation);
        }
		
        if (requestCode == ZIP_SUB_FOLDERPICKER_CODE) {
            final String folderLocation = getPickerUriAsString(data,
                    Global.USE_DOCUMENT_PROVIDER && allowDocProvider4Subfolder, true);
			onZipRelPathFolderPickResult(folderLocation, resultCode == Activity.RESULT_OK);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void grandPermission5(Context ctx, Uri data) {
        DocumentFile docPath = DocumentFile.fromTreeUri(ctx, data);
        if (docPath != null) {
            final ContentResolver resolver = ctx.getContentResolver();
            resolver.takePersistableUriPermission(data,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_config, menu);
        return result;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        ZipStorage storage = SettingsImpl.getCurrentZipStorage(this);

        final boolean zipExists = storage.exists();
        updateMenuItem(menu, R.id.cmd_delete, R.string.delete_menu_title, zipExists);
        updateMenuItem(menu, R.id.cmd_view_zip, R.string.view_zip_menu_title, zipExists);
        updateMenuItem(menu, R.id.cmd_send, R.string.send_zip_menu_title, zipExists);

        updateMenuItem(menu, R.id.cmd_filemanager, 0,
                FileManagerUtil.hasShowInFilemanager(this, getZipFolder(storage)));

        return super.onPrepareOptionsMenu(menu);
    }

    private String getZipFolder(ZipStorage storage) {
        String path = storage.getAbsolutePath();
        if (path != null) {
            File filePath = new File(path);
            return filePath.getParent();
        }
        return null;
    }

    private void updateMenuItem(Menu menu, int cmd, int menuTitle, boolean visible) {
        MenuItem item = menu.findItem(cmd);

        if (item != null) {
            if ((visible) && (menuTitle != 0)) {
                item.setTitle(getString(menuTitle, SettingsImpl.getZipFile()));
            }
            item.setVisible(visible);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        // cannot use switch case because resource-ids will not be final in future version
        if (itemId == R.id.cmd_send) {
            return onCmdSend(item.getTitle());
        } else if (itemId == R.id.cmd_delete) {
            return onCmdDeleteQuestion();
        } else if (itemId == R.id.cmd_view_zip) {
            return onShowZip();
        } else if (itemId == R.id.cmd_filemanager) {
            ZipStorage storage = SettingsImpl.getCurrentZipStorage(this);
            return FileManagerUtil.showInFilemanager(this, getZipFolder(storage));
        } else if (itemId == R.id.cmd_about) {
            AboutDialogPreference.createAboutDialog(this).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean onShowZip() {
        ZipStorage storage = SettingsImpl.getCurrentZipStorage(this);
        Intent startIntent = new Intent(Intent.ACTION_VIEW);
        Uri contentUri = Global.USE_DOCUMENT_PROVIDER ? Uri.parse(storage.getFullZipUriOrNull()) : null;
        Uri fileUri = Uri.fromFile(new File(storage.getAbsolutePath()));
        String mime = "application/zip";

        if (fileUri != null) {
            // try different combinations. first matching wins
            boolean success = tryStartActivity(startIntent, fileUri, mime)
                    || tryStartActivity(startIntent, fileUri, null)
                    || tryStartActivity(startIntent, contentUri, mime)
                    || tryStartActivity(startIntent, contentUri, null);
            if (!success) {
                String message = getString(R.string.viewer_not_installed, contentUri.toString());
                Toast
                        .makeText(this, message, Toast.LENGTH_LONG)
                        .show();
            }
        }
        return true;
    }

    private boolean tryStartActivity(Intent startIntent, Uri contentUri, String mime) {
        boolean result = false;
        if (contentUri != null) {
            startIntent.setDataAndType(contentUri, mime);
            try {
                startActivity(startIntent);
                result = true;
            } catch (Exception ignore) {
            }

            if (Global.debugEnabled) {
                Log.d(Global.LOG_CONTEXT, "tryStartActivity() returns " + result +
                        " for "
                        + startIntent.toUri(0));
            }
        }
        return result;
    }

    private boolean onCmdSend(CharSequence title) {
        ZipStorage storage = SettingsImpl.getCurrentZipStorage(this);
        Intent intent = new Intent(Intent.ACTION_SEND);
        Uri contentUri = Uri.parse(storage.getFullZipUriOrNull());
        String mime = "application/zip";

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);


        // EXTRA_CC, EXTRA_BCC
        // intent.putExtra(Intent.EXTRA_EMAIL, new String[]{toAddress});

        intent.putExtra(Intent.EXTRA_SUBJECT, SettingsImpl.getZipFile());

        // intent.putExtra(Intent.EXTRA_TEXT, body);

        intent.putExtra(Intent.EXTRA_STREAM, contentUri);

        intent.setType(mime);

        final String debugMessage = "SettingsActivity.onCmdSend(" +
                title +
                ", startActivity='" + intent.toUri(0)
                + "')";
        try {
            final Intent chooser = Intent.createChooser(intent, title);
            chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
            this.startActivity(chooser);
            if (Global.debugEnabled) {
                Log.d(Global.LOG_CONTEXT, debugMessage);
            }
        }
        catch(Exception ex) {
            Log.w(Global.LOG_CONTEXT, debugMessage, ex);
        }

        return true;
    }

    private boolean onCmdDeleteQuestion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.delete_menu_title, SettingsImpl.getZipFile()));

        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onCmdDeleteAnswer();
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        return true;
    }

    private void onCmdDeleteAnswer() {
        SettingsImpl.getCurrentZipStorage(this).delete(ZipStorage.ZipInstance.current);
    }
}
