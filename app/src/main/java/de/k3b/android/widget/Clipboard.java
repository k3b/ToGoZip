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
package de.k3b.android.widget;

import android.content.Context;

/**
 * Helper to set text to android clipboard.<br/>
 * <br/>
 * Created by k3b on 17.11.2014.
 */
public class Clipboard {
    public static void addToClipboard(Context context, CharSequence text) {
        // for compatibility reasons using depricated clipboard api. the non depricateded clipboard was not available before api 11.
        try {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
