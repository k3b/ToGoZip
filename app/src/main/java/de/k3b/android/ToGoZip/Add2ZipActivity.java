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

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.k3b.android.AndroidCompressJob;
import de.k3b.android.MediaUtil;

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
                        job.traceMessage("Extras[TEXT][] strings: adding {0}", item);
                        result.append(item).append("\n\n");
                    }
                    if (result.length() > 0) return result.toString();
                }
            } else {
                String s = extras.getString(Intent.EXTRA_TEXT);
                if (s != null) {
                    job.traceMessage("Extras[TEXT] string: adding {0}", s);
                    return s;
                }
            }

            // fallback for unknown extra-extra type
            if (extra != null) {
                job.traceMessage("Extras[TEXT] {1}: adding {0}", extra, extra.getClass().getCanonicalName());
                return extra.getClass().getCanonicalName() + ":" + extra.toString();
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return getClipDataText(intent);
        }

        return null;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private String getClipDataText(Intent intent) {
        ClipData clipData = intent.getClipData();
        if (clipData != null) {
            StringBuilder result = new StringBuilder();

            int count = clipData.getItemCount();
            for(int i=0; i < count; i++) {
                ClipData.Item clipItem = clipData.getItemAt(i);
                if (!addClipData(result, clipItem.getHtmlText(), "html")
                        && !addClipData(result, clipItem.getText(), "text")) {
                    Uri uri = clipItem.getUri();
                    String scheme = (uri != null) ? uri.getScheme() : "";

                    if ((!"file".equalsIgnoreCase(scheme) && (!"content".equalsIgnoreCase(scheme)))) {
                        addClipData(result, uri, "uri");
                    }

                }
            }
            if (result.length() > 0) return result.toString();
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
                            addResult("Extras[Stream][] uris", result, item, true);
                        }
                    } else {
                        job.addError("Unknown format for Intent.Extras[Stream] : "
                                + getLogMessageString(extra));
                    }
                } else {
                    addResult("Extras[Stream] uri", result, (Uri) extras.getParcelable(Intent.EXTRA_STREAM), true);
                }
                extra = null;
            }

            addResult("getData uri ", result, intent.getData(), true);

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                addClipUris(result, intent);
            }


        } catch (Exception ex) {
            job.addError("error : " + ex.getMessage() +"\nlast extra = " + getLogMessageString(extra));
        }
        int len = result.size();
        if (len == 0) return null;
        return result.toArray(new File[len]);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void addClipUris(ArrayList<File> result, Intent intent) {
        ClipData clipData = intent.getClipData();
        int count = (clipData != null) ? clipData.getItemCount() : 0;
        for(int i=0; i < count; i++) {
            ClipData.Item clipItem = clipData.getItemAt(i);

            Uri uri = (clipData != null) ? clipItem.getUri() : null;
            addResult("clipData[i] uri", result, uri, false);
        }
    }

    private boolean addClipData(StringBuilder result, Object item, String type) {
        if (item != null) {
            job.traceMessage("ClipData[] {1}: adding {0}", item, type);
            result.append(item).append("\n\n");

            return true;
        }
        return false;
    }

    private String getLogMessageString(Object extra) {
        if (extra != null) {
            return extra.getClass().getCanonicalName()
                    + ": " + extra;
        }
        return "";
    }

    private void addResult(String context, ArrayList<File> result, Uri uri, boolean addErrorIfUnresolved) {
        if (uri != null) {
            job.traceMessage("{0}: adding file {1}", context, uri);

            File file = getLocalFile(uri, addErrorIfUnresolved);
            if (file != null) {
                result.add(file);
            }
        }
    }

    private File getLocalFile(Uri uri, boolean addErrorIfUnresolved) {
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
            if (addErrorIfUnresolved) job.addError("Not implemented url: " + uri);
        }
        return null;
    }

}
