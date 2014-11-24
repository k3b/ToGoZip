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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import de.k3b.android.AndroidCompressJob;

/**
 * show settings/config activity. On Start and Exit checks if data is valid.
 */
public class SettingsActivity extends PreferenceActivity {

    /**
     * if not null: try to execute add2zip on finish
     */
    private static File[] filesToBeAdded = null;
    private static String textToBeAdded = null;
    private AndroidCompressJob job = null;

    /**
     * public api to start settings-activity
     */
    public static void show(Context context, File[] filesToBeAdded, String textToBeAdded) {
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
        super.onCreate(savedInstanceState);
        SettingsImpl.init(this);
        this.job = new AndroidCompressJob(this, new File(SettingsImpl.getZipfile()), Global.debugEnabled);

        this.addPreferencesFromResource(R.xml.preferences);

        showAlertOnError();
    }

    /**
     * return false if no error. else Show Dialog cancel/setToDefault/Edit
     */
    private boolean showAlertOnError() {
        boolean canWriteCurrent = SettingsImpl.init(this);

        if (!canWriteCurrent) {
            String currentZipPath = SettingsImpl.getZipfile();
            String defaultZipPath = SettingsImpl.getDefaultZipPath(this);
            boolean canWriteDefault = SettingsImpl.canWrite(defaultZipPath);

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
        String defaultZipPath = SettingsImpl.getDefaultZipPath(this);
        SettingsImpl.setZipfile(this, defaultZipPath);
        File[] fileToBeAdded = SettingsActivity.filesToBeAdded;
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
}
