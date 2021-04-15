/*
 * Copyright (C) 2017-2019 k3b
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

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import android.widget.Toast;

import static androidx.core.content.ContextCompat.checkSelfPermission;

/**
 * Support for android-6.0 (M) ff runtime permissons.
 * Created by k3b on 23.12.2017.
 *
 * implements ActivityCompat.OnRequestPermissionsResultCallback
 */

public class PermissionHelper {
    /**
     * Id to identify a Storage permission request.
     */
    private static final int REQUEST_CODE_STORAGE = 215;

    /**
     * Permissions required to read and write Storage.
     */
    private static final String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private PermissionHelper() {/*hide public constructor*/};
    /**
     * called before permission is required.
     *
     * @return true if has-permissions. else false and request permissions
     * */
    public static boolean hasPermissionOrRequest(Activity context) {
        if (!hasPermission(context)) {
            // Storage permission has not been requeste yet. Request for first time.
            ActivityCompat.requestPermissions(context, PERMISSIONS_STORAGE, REQUEST_CODE_STORAGE);
            // no permission yet
            return false;
        } // if android-m

        // already has permission.
        return true;
    }

    /**
     * @return true if has-(runtime-)permissions.
     * */
    public static boolean hasPermission(Activity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean needsRead = checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED;

            boolean needsWrite = checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED;

            if (needsRead || needsWrite) {
                // no permission yet
                return false;
            }
        } // if android-m

        // already has permission.
        return true;
    }

    /**
     * called in onRequestPermissionsResult().
     *
     * @return true if just received permissions. else false and calling finish
     */
    public static boolean receivedPermissionsOrFinish(Activity activity, int requestCode,
                                  @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_STORAGE) {
            if ( (grantResults.length == 2)
                  && (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                  && (grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                return true;
            } else {
                showNowPermissionMessage(activity);
                activity.finish();
            }
        }
        return false;
    }

    public static void showNowPermissionMessage(Activity activity) {
        String format = activity.getString(R.string.ERR_NO_WRITE_PERMISSIONS);

        String msg = String.format(
                format,
                "",
                "");

        Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
    }
}
