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
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import de.k3b.android.AndroidCompressJob;
import de.k3b.android.widgets.Clipboard;
import de.k3b.zip.CompressJob;

public class Add2ZipActivity extends Activity {

    private static final String TAG = "Add2ZipActivity";

    //############## state ############

    private File[] fileToBeAdded = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingsImpl.init(this);
        boolean canWrite = SettingsImpl.init(this);

        this.fileToBeAdded = getFileToBeAdded();

        if (!canWrite) {
            SettingsActivity.show(this, this.fileToBeAdded);
            /*
            String msg = String.format(
                    getString(R.string.ERR_NO_WRITE_PERMISSIONS),
                    SettingsImpl.getZipfile(),
                    SettingsImpl.getDefaultZipPath(this));
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            Clipboard.addToClipboard(this, msg);
            */
            finish();
            return;
        }

        if (this.fileToBeAdded == null) {
            Toast.makeText(this, getString(R.string.WARN_ADD_NO_FILES), Toast.LENGTH_LONG).show();
        } else {
            AndroidCompressJob.addToZip(this, getCurrentZipFile(), this.fileToBeAdded);
        }
        this.finish();
    }

    private File getCurrentZipFile() {
        return new File(SettingsImpl.getZipfile());
    }

    private File[] getFileToBeAdded() {
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
            Log.d(TAG, "getFileToBeAdded " + len + ":" + result);
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
