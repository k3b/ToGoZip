package de.k3b.android;

import android.content.Context;
import android.widget.Toast;

import java.io.File;

import de.k3b.android.toGoZip.Global;
import de.k3b.android.toGoZip.R;
import de.k3b.android.widgets.Clipboard;
import de.k3b.zip.CompressJob;

/**
 * Created by EVE on 17.11.2014.
 */
public class AndroidCompressJob {
    //############ processing ########

    public static void addToZip(Context context, File currentZipFile, File[] fileToBeAdded) {
        if (fileToBeAdded != null) {
            currentZipFile.getParentFile().mkdirs();
            CompressJob job = new CompressJob(currentZipFile, Global.debugEnabled);
            job.add("", fileToBeAdded);
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
