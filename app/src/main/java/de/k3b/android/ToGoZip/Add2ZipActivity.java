/*
 * Copyright (C) 2014-2019 k3b
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

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import de.k3b.android.widget.LocalizedActivity;
import de.k3b.android.zip.Global;
import de.k3b.android.zip.IntentParser;
import de.k3b.zip.CompressItem;
import de.k3b.zip.FileCompressItem;
import de.k3b.zip.ZipLog;
import de.k3b.zip.ZipLogImpl;

/**
 * This pseudo activity has no gui. It starts add2zip from intent-data
 * or starts the settings-activity if the zip-output-dir is write-protected
 */
public class Add2ZipActivity extends LocalizedActivity {
    /**
     * caption for logging
     */
    private static final String TAG = "Add2ZipActivity";
    private static final int CODE_SETTIMGS = 1235;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        executeZipJob();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_SETTIMGS) { //  && resultCode == RESULT_OK && Global.USE_DOCUMENT_PROVIDER) {
            if (resultCode == RESULT_OK) {
                executeZipJob();
            } else {
                PermissionHelper.showNowPermissionMessage(this);
                finish();
            }
        }
    }

    private void executeZipJob() {
        boolean canWrite = PermissionHelper.hasPermission(this) && SettingsImpl.init(this);
        // on error show settings
        if (!canWrite) {
            SettingsActivity.startActivityForResult(this, CODE_SETTIMGS);
            return;
        }

        FileCompressItem.setZipRelPath(SettingsImpl.getZipRelPathAsFile());
        ZipLog zipLog = new ZipLogImpl(Global.debugEnabled);
        IntentParser intentParser = new IntentParser(this, getIntent(), zipLog);

        ToGoZipCompressJob job = new ToGoZipCompressJob(
                this, zipLog,
                Global.isWriteLogFile2Zip ? (this.getString(R.string.app_name) + ".log") : null
        );
        job.setZipStorage(SettingsImpl.getCurrentZipStorage(this));

        CompressItem[] filesToBeAdded = intentParser.getFilesToBeAdded();
        String textToBeAdded = intentParser.getTextToBeAdded();

        // no error yet
        if ((textToBeAdded == null) && (filesToBeAdded == null)) {
            Toast.makeText(this, getString(R.string.WARN_ADD_NO_FILES), Toast.LENGTH_LONG).show();
        } else {
            job.executeAddToZip(textToBeAdded, filesToBeAdded);
        }
        this.finish();
    }

}
