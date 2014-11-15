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

import de.k3b.zip.CompressJob;

public class Add2ZipActivity extends Activity {

    private static final String TAG = "Add2ZipActivity";

    //############## state ############

    private File[] fileToBeAdded = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingsImpl.init(this);

        this.fileToBeAdded = getFileToBeAdded();

        if (this.fileToBeAdded == null) {
            Toast.makeText(this, getString(R.string.WARN_ADD_NO_FILES), Toast.LENGTH_LONG).show();
        } else {
            addToZip();
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

    //############ processing ########

    private void addToZip() {
        File currentZipFile = getCurrentZipFile();

        if (this.fileToBeAdded != null) {
            currentZipFile.getParentFile().mkdirs();
            CompressJob job = new CompressJob(currentZipFile, Global.debugEnabled);
            job.add("", this.fileToBeAdded);
            int result = job.compress();

            String currentZipFileAbsolutePath = currentZipFile.getAbsolutePath();
            final String text = getResultMessage(result, currentZipFileAbsolutePath, job);
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();

            if (Global.debugEnabled) {
                addToClipboard(text+ "\n\n" + job.getLastError(true));
            }
        }
    }

    private String getResultMessage(int convertResult, String currentZipFileAbsolutePath, CompressJob job) {
        if (convertResult == CompressJob.RESULT_ERROR_ABOART) {
            return String.format(getString(R.string.ERR_ADD),
                    currentZipFileAbsolutePath, job.getLastError(false));
        } else if (convertResult == CompressJob.RESULT_NO_CHANGES) {
            return String.format(getString(R.string.WARN_ADD_NO_CHANGES), currentZipFileAbsolutePath);
        } else {
            return String.format(getString(R.string.SUCCESS_ADD), currentZipFileAbsolutePath, job.getAddCount());
        }
    }

    private void addToClipboard(String text) {
        // for compatibility reaons using depricated clipboard api. the non depricateded clipboard was not available before api 11.
        try {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addResult(ArrayList<File> result, Uri data) {
        if ((data != null) && ("file".equalsIgnoreCase(data.getScheme()))) {
            result.add(new File(data.getPath()));
        }
    }
}
