/*
 * Copyright (C) 2014-2018 k3b
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

import de.k3b.android.toGoZip.SettingsImpl;
import de.k3b.android.zip.Global;
import de.k3b.android.widget.Clipboard;
import de.k3b.zip.CompressItem;
import de.k3b.zip.CompressJob;
import de.k3b.zip.ZipLog;

/**
 * Android specific version of zip-CompressJob.<br/>
 * <p/>
 * Created by k3b on 17.11.2014.
 */
public class AndroidCompressJob extends CompressJob {
    private final Context context;
    private final int resStringBanner;
    private final int resStringSuccessAdd;
    private final int resStringWarnAddNoChanges;
    private final int resStringErrAdd;

    /**
     * Creates a job.
     * @param zipLog if true collect diagnostics/debug messages to debugLogMessages.
     * @param fileLogInZip
     */
    public AndroidCompressJob(
            Context context,
            ZipLog zipLog,
            String fileLogInZip,
            int resStringBanner, int resStringSuccessAdd, int resStringWarnAddNoChanges, int resStringErrAdd) {
        super(zipLog, fileLogInZip);
        this.context = context;
        this.resStringBanner = resStringBanner;
        this.resStringSuccessAdd = resStringSuccessAdd;
        this.resStringWarnAddNoChanges = resStringWarnAddNoChanges;
        this.resStringErrAdd = resStringErrAdd;
    }
    //############ processing ########

    public void executeAddToZip(String textToBeAdded, CompressItem[] filesToBeAdded) {
        if ((textToBeAdded != null) || ((filesToBeAdded != null) && (filesToBeAdded.length > 0))) {
            addToCompressQueue(filesToBeAdded);
            boolean useLongTextFile = false;
            if (textToBeAdded != null) {
                useLongTextFile = SettingsImpl.useLongTextFile(textToBeAdded.length());
                addTextToCompressQue(SettingsImpl.getTextfile(useLongTextFile), textToBeAdded);
            }
            int result = compress(useLongTextFile);

            final String text = getResultMessage(result);
            Toast.makeText(context, text, Toast.LENGTH_LONG).show();

            if (Global.debugEnabled) {
                Clipboard.addToClipboard(context, text + "\n\n" + getLastError(true));
            }
        }
    }

    private String getResultMessage(int convertResult) {
        String currentZipFileAbsolutePath = getAbsolutePath();
        if (convertResult == CompressJob.RESULT_ERROR_ABOART) {
            return String.format(context.getString(resStringErrAdd),
                    currentZipFileAbsolutePath, getLastError(false));
        } else if (convertResult == CompressJob.RESULT_NO_CHANGES) {
            return String.format(context.getString(resStringWarnAddNoChanges), currentZipFileAbsolutePath);
        } else {
            return String.format(context.getString(resStringSuccessAdd), currentZipFileAbsolutePath, getAddCount());
        }
    }

    /** footer added to text collector. null means no text. */
    @Override protected String getTextFooter() {
        String result = this.context.getResources().getString(resStringBanner); // "Collected with ToGoZip version ...";

        final String versionName = GuiUtil.getAppVersionName(context);
        if (versionName != null) {
            result = result.replace("$versionName$", versionName);
        }

        return result;
    }
}
