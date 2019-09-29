/*
 * Copyright (C) 2018-2019 k3b
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

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import de.k3b.LibGlobal;
import de.k3b.android.util.LogCat;
import de.k3b.android.widget.Clipboard;
import de.k3b.android.zip.Global;
import de.k3b.io.FileUtils;
import de.k3b.zip.LibZipGlobal;
import de.k3b.zip.ZipStorage;
import lib.folderpicker.FolderPicker;

public class ToGoZipApp extends Application {
    private static final String FILE_NAME_PREFIX = "toGoZip.logcat-";
    private LogCat mCrashSaveToFile = null;

    @Override public void onCreate() {
        super.onCreate();

        SettingsImpl.init(this);
        //         SettingsActivity.prefs2Global(this);

        mCrashSaveToFile = new LogCat(Global.LOG_CONTEXT,
                LibGlobal.LOG_TAG, LibZipGlobal.LOG_TAG, FolderPicker.LOG_TAG) {
            public void saveToFile() {
                final ToGoZipApp context = ToGoZipApp.this;
                ZipStorage logStorage = SettingsImpl.getCurrentStorage(
                        context, getLocalLogFileName(FILE_NAME_PREFIX));
                String message = null;
                OutputStream outputStream = null;
                if (logStorage != null) {
                    try {
                        outputStream = logStorage.createOutputStream(ZipStorage.ZipInstance.logfile);
                        message = "saving errorlog ('LocCat') to " + logStorage.getAbsolutePath();
                        Log.e(Global.LOG_CONTEXT, message);
                    } catch (IOException e) {
                        Log.e(Global.LOG_CONTEXT, "Error creating crashlogfile " + logStorage.getAbsolutePath(), e);
                        outputStream = null;
                    }
                }

                ByteArrayOutputStream bos = null;
                if (outputStream == null) {
                    bos = new ByteArrayOutputStream();
                    outputStream = bos;
                    message = "saving errorlog ('LocCat') to clipboard";
                    Log.e(Global.LOG_CONTEXT, message);
                }

                Toast.makeText(context, message, Toast.LENGTH_LONG).show();

                saveLogCat(null, outputStream, mTags);
                FileUtils.close(outputStream, "");

                if (bos != null) {
                    Clipboard.addToClipboard(context, new String(bos.toByteArray()));
                }
            }

        };
        Log.i(Global.LOG_CONTEXT, getAppId() + " created");

    }

    public String getAppId() {
        return getString(R.string.app_name);
    }

    @Override
    public void onTerminate() {
        Log.i(Global.LOG_CONTEXT, getAppId() + " terminated");
        if (mCrashSaveToFile != null) {
            mCrashSaveToFile.close();
        }
        mCrashSaveToFile = null;
        super.onTerminate();
    }

    public void saveToFile() {
        if (mCrashSaveToFile != null) {
            mCrashSaveToFile.saveToFile();
        }
    }
    public void clear() {
        if (mCrashSaveToFile != null) {
            mCrashSaveToFile.clear();
        }
    }
}
