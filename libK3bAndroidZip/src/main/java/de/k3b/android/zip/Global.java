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
package de.k3b.android.zip;

import android.os.Build;

import java.util.Locale;

/**
 * Global settings
 */
public class Global {
    /** document tree supported since andrid-5.0. For older devices use folder picker */
    public static final boolean USE_DOCUMENT_PROVIDER = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);

    /** #6: local settings: which language should the gui use */
    public static final String PREF_KEY_USER_LOCALE = "user_locale";

    public static final String LOG_CONTEXT = "toGoZip";
    /**
     * true: addToCompressQue several Log.d(...) to show what is going on.
     * debugEnabled is updated by the SettingsActivity
     */
    public static boolean debugEnabled = false;

    /** Remember initial language settings. This allows setting "switch back to device language" after changing app locale */
    public static Locale systemLocale = Locale.getDefault();

    /** if not null added files will be logged in this zip-entry-text-file */
    public static boolean isWriteLogFile2Zip = false;
}
