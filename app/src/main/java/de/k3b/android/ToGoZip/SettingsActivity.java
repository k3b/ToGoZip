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

import de.k3b.android.widgets.Clipboard;
import de.k3b.android.widgets.EditTextPreferenceWithSummary;

public class SettingsActivity extends PreferenceActivity {

    private static File[] fileToBeAdded;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean canWriteCurrent = SettingsImpl.init(this);

        this.addPreferencesFromResource(R.xml.preferences);

        if (!canWriteCurrent) {
            String defaultZipPath = SettingsImpl.getDefaultZipPath(this);
            String currentZipPath = SettingsImpl.getZipfile();
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
                    finish();
                }
            });

            if (fileToBeAdded != null) {
                builder.setPositiveButton(R.string.cmd_run, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        run();
                    }
                });
            } else {
                builder.setPositiveButton(R.string.cmd_edit, null);
            }

            builder.show();
        }

    }

    /** executes add2zip */
    private void run() {
    }

    /** resets zip to default */
    private void setDefault() {
        String defaultZipPath = SettingsImpl.getDefaultZipPath(this);
        SettingsImpl.setZipfile(this, defaultZipPath);
        finish();
        show(this,fileToBeAdded);
/*
        EditTextPreferenceWithSummary preference = (EditTextPreferenceWithSummary)getPreferenceScreen().findPreference(
                SettingsImpl.KEY_ZIPFILE);

        if (preference != null) {
            preference.setSummary(defaultZipPath);
        }
        */
    }

    /*
    @Override
    public void onResume() {
        super.onResume();
        boolean canWrite = SettingsImpl.init(this);

        if (!canWrite) {
            String msg = String.format(
                    getString(R.string.ERR_NO_WRITE_PERMISSIONS),
                    SettingsImpl.getZipfile(),
                    SettingsImpl.getDefaultZipPath(this));
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            Clipboard.addToClipboard(this, msg);

        }

    }
    */

    public static void show(Context context, File[] fileToBeAdded) {
        final Intent i = new Intent(context,SettingsActivity.class);

        if (Global.debugEnabled) {
            Log.i(Global.LOG_CONTEXT, "start(startActivity='" + i
                    + "')");
        }

        SettingsActivity.fileToBeAdded = fileToBeAdded;
        context.startActivity(i);

    }
}
