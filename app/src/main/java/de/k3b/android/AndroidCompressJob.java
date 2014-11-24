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
package de.k3b.android;

import android.content.Context;
import android.widget.Toast;

import java.io.File;

import de.k3b.android.toGoZip.Global;
import de.k3b.android.toGoZip.R;
import de.k3b.android.toGoZip.SettingsImpl;
import de.k3b.android.widgets.Clipboard;
import de.k3b.zip.CompressJob;

/**
 * Android specific version of zip-CompressJob.<br/>
 * <p/>
 * Created by k3b on 17.11.2014.
 */
public class AndroidCompressJob {
    //############ processing ########

    public static void addToZip(Context context, File currentZipFile, String textToBeAdded, File[] fileToBeAdded) {
        if ((textToBeAdded != null) || (fileToBeAdded != null)) {
            currentZipFile.getParentFile().mkdirs();
            CompressJob job = new CompressJob(currentZipFile, Global.debugEnabled);
            job.addToCompressQue("", fileToBeAdded);
            if (textToBeAdded != null) {
                job.addTextToCompressQue(SettingsImpl.getTextfile(), textToBeAdded);
            }
            int result = job.compress();

            String currentZipFileAbsolutePath = currentZipFile.getAbsolutePath();
            final String text = getResultMessage(context, result, currentZipFileAbsolutePath, job);
            Toast.makeText(context, text, Toast.LENGTH_LONG).show();

            if (Global.debugEnabled) {
                Clipboard.addToClipboard(context, text + "\n\n" + job.getLastError(true));
            }
        }
    }

    private static String getResultMessage(Context context, int convertResult, String currentZipFileAbsolutePath, CompressJob job) {
        if (convertResult == CompressJob.RESULT_ERROR_ABOART) {
            return String.format(context.getString(R.string.ERR_ADD),
                    currentZipFileAbsolutePath, job.getLastError(false));
        } else if (convertResult == CompressJob.RESULT_NO_CHANGES) {
            return String.format(context.getString(R.string.WARN_ADD_NO_CHANGES), currentZipFileAbsolutePath);
        } else {
            return String.format(context.getString(R.string.SUCCESS_ADD), currentZipFileAbsolutePath, job.getAddCount());
        }
    }


}
