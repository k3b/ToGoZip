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

    private ArrayList<File> resultFiles = null;
    private StringBuffer resultText = null;
    public IntentParser(Context context, Intent intent, ZipLog zipLog) {
        this.context = context;
        this.intent = intent;
        this.zipLog = zipLog;
    }

    private void getTextToBeAdded(StringBuffer resultText) {
        int oldItems = resultText.length();
        Bundle extras = (intent != null) ? intent.getExtras() : null;
        Object extra = (extras != null) ? extras.get(Intent.EXTRA_TEXT) : null;
        if (extra != null) {
            if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction())) {
                List<String> strings = extras.getStringArrayList(Intent.EXTRA_TEXT);
                if (strings != null) {
                    for (String item : strings) {
                        zipLog.traceMessage("Extras[TEXT][] strings: adding {0}", item);
                        resultText.append(item).append("\n\n");
                    }
                    extra = null;
                }
            } else {
                String s = extras.getString(Intent.EXTRA_TEXT);
                if (s != null) {
                    zipLog.traceMessage("Extras[TEXT] string: adding {0}", s);
                    resultText.append(s).append("\n\n");
                    extra = null;
                }
            }

            // fallback for unknown extra-extra type
            if (extra != null) {
                zipLog.traceMessage("Extras[TEXT] {1}: adding {0}", extra, extra.getClass().getCanonicalName());
                resultText.append(extra.getClass().getCanonicalName()).append(":").append(extra.toString()).append("\n\n");
            }
        }
    }

    public File[] getFilesToBeAdded() {
        parse();
        int len = resultFiles.size();
        if (len == 0) return null;
        return resultFiles.toArray(new File[len]);
    }

    public String getTextToBeAdded() {
        parse();
        if (resultText.length() == 0) return null;
        return resultText.toString();
    }

    private void parse() {
        if ((resultFiles != null) && (resultText != null)) {
            return;
        }
        resultFiles = new ArrayList<File>();
        resultText = new StringBuffer();

        Object extra = null;
        try {
            Bundle extras = (intent != null) ? intent.getExtras() : null;
            extra = (extras != null) ? extras.get(Intent.EXTRA_STREAM) : null;
            if (extra != null) {
                if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction())) {
                    ArrayList<Uri> uris = extras.getParcelableArrayList(Intent.EXTRA_STREAM);
                    if (uris != null) {
                        for (Uri item : uris) {
                            addResult("Extras[Stream][] uris", item, extra);
                        }
                    } // else unknown format.
                } else {
                    addResult("Extras[Stream] uri", (Uri) extras.getParcelable(Intent.EXTRA_STREAM), extra);
                }
                extra = null;
            }

            addResult("getData uri ", intent.getData(), null);
            getTextToBeAdded(resultText);

            if ((android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) && (resultFiles.size() == 0) && (resultText.length() == 0)) {
                addClipUris();
            }


        } catch (Exception ex) {
            zipLog.addError("error : " + ex.getMessage() + "\nlast extra = " + getLogMessageString(extra));
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void addClipUris() {
        ClipData clipData = intent.getClipData();
        int count = (clipData != null) ? clipData.getItemCount() : 0;
        for (int i = 0; i < count; i++) {
            ClipData.Item clipItem = clipData.getItemAt(i);

            Uri uri = (clipData != null) ? clipItem.getUri() : null;
            addResult("clipData[i] uri", uri, null);
            if (!addClipData(clipItem.getHtmlText(), "html"))
                addClipData(clipItem.getText(), "text");
        }
    }

    private boolean addClipData(Object item, String type) {
        if (item != null) {
            zipLog.traceMessage("ClipData[] {1}: adding {0}", item, type);
            resultText.append(item).append("\n\n");

            return true;
        }
        return false;
    }

    private void addResult(String context, Uri uri, Object nonUriValue) {
        if (uri != null) {
            zipLog.traceMessage("{0}: adding file {1}", context, uri);

            File file = getLocalFile(uri);
            if (file != null) {
                resultFiles.add(file);
                return;
            }
            zipLog.traceMessage("{1} : adding uri {0}", uri);
            resultText.append(uri).append("\n\n");
            nonUriValue = null;
        }
        if (nonUriValue != null) {
            zipLog.traceMessage("{1} : adding text {0}", nonUriValue);
            resultText.append(nonUriValue).append("\n\n");
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
