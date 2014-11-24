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
import java.util.List;

import de.k3b.android.AndroidCompressJob;
import de.k3b.android.MediaUtil;
import de.k3b.android.widgets.Clipboard;

/**
 * This pseudo activity has no gui. It starts add2zip from intent-data
 * or starts the settings-activity if the zip-output-dir is write-protected
 */
public class Add2ZipActivity extends Activity {

    private AndroidCompressJob job = null;
    /**
     * caption for logging
     */
    private static final String TAG = "Add2ZipActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean canWrite = SettingsImpl.init(this);

        this.job = new AndroidCompressJob(this, getCurrentZipFile(), Global.debugEnabled);
        File[] filesToBeAdded = getFilesToBeAdded();
        String textToBeAdded = getTextToBeAdded();

        // on error show settings
        if (!canWrite) {
            SettingsActivity.show(this, filesToBeAdded, textToBeAdded);
            finish();
            return;
        }

        // no error yet
        if ((textToBeAdded == null) && (filesToBeAdded == null)) {
            Toast.makeText(this, getString(R.string.WARN_ADD_NO_FILES), Toast.LENGTH_LONG).show();
        } else {
            this.job.addToZip(textToBeAdded, filesToBeAdded);
        }
        this.finish();
    }

    private File getCurrentZipFile() {
        return new File(SettingsImpl.getZipfile());
    }

    private String getTextToBeAdded() {
        Intent intent = getIntent();
        Bundle extras = (intent != null) ? intent.getExtras() : null;
        Object extra = (extras != null) ? extras.get(Intent.EXTRA_TEXT) : null;
        if (extra != null) {
            if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction())) {
                List<String> strings = extras.getStringArrayList(Intent.EXTRA_TEXT);
                if (strings != null) {
                    StringBuilder result = new StringBuilder();
                    for (String item : strings) {
                        result.append(item).append("\n\n");
                    }
                    if (result.length() > 0) return result.toString();
                }
            } else {
                String s = extras.getString(Intent.EXTRA_TEXT);
                if (s != null)
                    return s;
            }

            // fallback for unknown extra-extra type
            if (extra != null)
                return extra.getClass().getCanonicalName() + ":" + extra.toString();
        }
        return null;
    }

    private File[] getFilesToBeAdded() {
        StringBuilder errorMessage = new StringBuilder();
        ArrayList<File> result = new ArrayList<File>();
        Object extra = null;
        try {
            Intent intent = getIntent();
            Bundle extras = (intent != null) ? intent.getExtras() : null;
            extra = (extras != null) ? extras.get(Intent.EXTRA_STREAM) : null;
            if (extra != null) {
                if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction())) {
                    ArrayList<Uri> uris = extras.getParcelableArrayList(Intent.EXTRA_STREAM);
                    if (uris != null) {
                        for (Uri item : uris) {
                            addResult(result, item);
                        }
                    } else {
                        errorMessage.append("unknown format for Intent.EXTRA_STREAM : "
                                + getLogMessageString(extra) + "\n");
                    }
                } else {
                    addResult(result, (Uri) extras.getParcelable(Intent.EXTRA_STREAM));
                }
                extra = null;
            }

            addResult(result, intent.getData());
        } catch (Exception ex) {
            errorMessage.append("error : " + ex.getMessage() +"\nlast extra = " + getLogMessageString(extra) + "\n");
        }
        int len = result.size();
        if (Global.debugEnabled) {
            if (errorMessage.length() > 0) {

                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();

                if (Global.debugEnabled) {
                    Clipboard.addToClipboard(this, errorMessage);
                }





                errorMessage.insert(0,"\n=");
            }
            Log.d(TAG, "getFilesToBeAdded " + len + ":" + result + errorMessage);
        }

        if (len == 0) return null;
        return result.toArray(new File[len]);
    }

    private String getLogMessageString(Object extra) {
        if (extra != null) {
            return extra.getClass().getCanonicalName()
                    + ": " + extra;
        }
        return "";
    }

    private void addResult(ArrayList<File> result, Uri uri) {
        File file = getLocalFile(uri);
        if (file != null) {
            result.add(file);
        }
    }

    private File getLocalFile(Uri uri) {
        if (uri != null) {
            String scheme = uri.getScheme();

            if ("file".equalsIgnoreCase(scheme)) {
                return new File(uri.getPath());
            } else if ("content".equalsIgnoreCase(scheme)) {
                String path = MediaUtil.convertMediaUriToPath(this, uri);
                if (path != null) {
                    return new File(path);
                }
            }
        }
        return null;
    }

}
