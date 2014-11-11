package de.k3b.android;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

public class GuiUtil {
    public static String getAppVersionName(final Context context) {
        try {

            final String versionName = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
            return versionName;
        } catch (final NameNotFoundException e) {
        }
        return null;
    }

}
