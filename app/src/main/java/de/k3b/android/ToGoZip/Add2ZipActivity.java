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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import de.k3b.android.AndroidCompressJob;

/**
 * This pseudo activity has no gui. It starts add2zip from intent-data
 * or starts the settings-activity if the zip-output-dir is write-protected
 */
public class Add2ZipActivity extends Activity {

    /**
     * caption for logging
     */
    private static final String TAG = "Add2ZipActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean canWrite = SettingsImpl.init(this);

        File[] filesToBeAdded = getFilesToBeAdded();

        // on error show settings
        if (!canWrite) {
            SettingsActivity.show(this, filesToBeAdded);
            finish();
            return;
        }

        // no error yet
        if (filesToBeAdded == null) {
            Toast.makeText(this, getString(R.string.WARN_ADD_NO_FILES), Toast.LENGTH_LONG).show();
        } else {
            AndroidCompressJob.addToZip(this, getCurrentZipFile(), filesToBeAdded);
        }
        this.finish();
    }

    private File getCurrentZipFile() {
        return new File(SettingsImpl.getZipfile());
    }

    private File[] getFilesToBeAdded() {
        ArrayList<File> result = new ArrayList<File>();
        Intent intent = getIntent();

        if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction())) {
            ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (uris != null) {
                for (Uri item : uris) {
                    addResult(result, item);
                }
            }
        } else {
            addResult(result, (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM));
        }

        addResult(result, intent.getData());

        int len = result.size();
        if (Global.debugEnabled) {
            Log.d(TAG, "getFilesToBeAdded " + len + ":" + result);
        }

        if (len == 0) return null;
        return result.toArray(new File[len]);
    }

    private void addResult(ArrayList<File> result, Uri data) {
        if ((data != null) && ("file".equalsIgnoreCase(data.getScheme()))) {
            result.add(new File(data.getPath()));
        }
    }
}
