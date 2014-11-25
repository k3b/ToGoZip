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
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.k3b.android.MediaUtil;
import de.k3b.zip.ZipLog;

/**
 * Created by k3b on 25.11.2014.
 */
public class IntentParser {
    private final Intent intent;
    private final ZipLog zipLog;
    private final Context context;

    public IntentParser(Context context, Intent intent, ZipLog zipLog) {
        this.context = context;
        this.intent = intent;
        this.zipLog = zipLog;
    }

    private void getTextToBeAdded(StringBuffer result) {
        int oldItems = result.length();
        Bundle extras = (intent != null) ? intent.getExtras() : null;
        Object extra = (extras != null) ? extras.get(Intent.EXTRA_TEXT) : null;
        if (extra != null) {
            if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction())) {
                List<String> strings = extras.getStringArrayList(Intent.EXTRA_TEXT);
                if (strings != null) {
                    for (String item : strings) {
                        zipLog.traceMessage("Extras[TEXT][] strings: adding {0}", item);
                        result.append(item).append("\n\n");
                    }
                    extra = null;
                }
            } else {
                String s = extras.getString(Intent.EXTRA_TEXT);
                if (s != null) {
                    zipLog.traceMessage("Extras[TEXT] string: adding {0}", s);
                    result.append(s).append("\n\n");
                    extra = null;
                }
            }

            // fallback for unknown extra-extra type
            if (extra != null) {
                zipLog.traceMessage("Extras[TEXT] {1}: adding {0}", extra, extra.getClass().getCanonicalName());
                result.append(extra.getClass().getCanonicalName()).append(":").append(extra.toString()).append("\n\n");
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void addClipUris(ArrayList<File> result, Intent intent, StringBuffer texts) {
        ClipData clipData = intent.getClipData();
        int count = (clipData != null) ? clipData.getItemCount() : 0;
        for (int i = 0; i < count; i++) {
            ClipData.Item clipItem = clipData.getItemAt(i);

            Uri uri = (clipData != null) ? clipItem.getUri() : null;
            addResult("clipData[i] uri", result, uri, null, null);
            if (!addClipData(texts, clipItem.getHtmlText(), "html"))
                addClipData(texts, clipItem.getText(), "text");
        }
    }


    public File[] getFilesToBeAdded(StringBuffer texts) {
        StringBuffer errorMessage = new StringBuffer();
        ArrayList<File> result = new ArrayList<File>();
        Object extra = null;
        try {
            Bundle extras = (intent != null) ? intent.getExtras() : null;
            extra = (extras != null) ? extras.get(Intent.EXTRA_STREAM) : null;
            if (extra != null) {
                if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction())) {
                    ArrayList<Uri> uris = extras.getParcelableArrayList(Intent.EXTRA_STREAM);
                    if (uris != null) {
                        for (Uri item : uris) {
                            addResult("Extras[Stream][] uris", result, item, extra, texts);
                        }
                    } // else unknown format.
                } else {
                    addResult("Extras[Stream] uri", result, (Uri) extras.getParcelable(Intent.EXTRA_STREAM), extra, texts);
                }
                extra = null;
            }

            addResult("getData uri ", result, intent.getData(), null, texts);
            getTextToBeAdded(texts);

            if ((android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) && (result.size() == 0) && (texts.length() == 0)) {
                addClipUris(result, intent, texts);
            }


        } catch (Exception ex) {
            zipLog.addError("error : " + ex.getMessage() + "\nlast extra = " + getLogMessageString(extra));
        }

        int len = result.size();
        if (len == 0) return null;
        return result.toArray(new File[len]);
    }

    private boolean addClipData(StringBuffer result, Object item, String type) {
        if (item != null) {
            zipLog.traceMessage("ClipData[] {1}: adding {0}", item, type);
            result.append(item).append("\n\n");

            return true;
        }
        return false;
    }

    private void addResult(String context, ArrayList<File> result, Uri uri, Object nonUriValue, StringBuffer texts) {
        if (uri != null) {
            zipLog.traceMessage("{0}: adding file {1}", context, uri);

            File file = getLocalFile(uri);
            if (file != null) {
                result.add(file);
                return;
            }
        }
        if (nonUriValue != null) {
            zipLog.traceMessage("{1} : adding text {0}", nonUriValue);
            texts.append(nonUriValue).append("\n\n");
        }
    }

    private File getLocalFile(Uri uri) {
        if (uri != null) {
            String scheme = uri.getScheme();

            if ("file".equalsIgnoreCase(scheme)) {
                File file = new File(uri.getPath());
                if (file.exists()) {
                    return file;
                }
            } else if ("content".equalsIgnoreCase(scheme)) {
                String path = MediaUtil.convertMediaUriToPath(context, uri);
                if (path != null) {
                    return new File(path);
                }
            }
        }
        return null;
    }

    private String getLogMessageString(Object extra) {
        if (extra != null) {
            return extra.getClass().getCanonicalName()
                    + ": " + extra;
        }
        return "";
    }
}
