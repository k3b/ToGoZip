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

import android.content.Context;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;

import de.k3b.android.MediaUtil;
import de.k3b.zip.CompressItem;

/**
 * One android-context-uri dependant dto-item that should be compressed.<br/>
 * Created by k3b on 16.02.2015.
 */
public class AndroidUriCompressItem extends CompressItem {
    private final Uri uri;
    private final Context context;

    public AndroidUriCompressItem(Context context, Uri uri, String mimeType, String zipEntryComment) {
        this.context = context;
        this.uri = uri;
        String name = MediaUtil.getFileName(context, uri, mimeType);
        setZipEntryFileName(name);
        setZipEntryComment(zipEntryComment);

    }

    @Override
    public InputStream getFileInputStream() throws IOException {
        return this.context.getContentResolver().openInputStream(this.uri);
    }

    @Override
    public long getLastModified() {
        return MediaUtil.getDateModified(this.context, this.uri);
    }

    @Override
    public StringBuilder getLogEntry(StringBuilder _result) {
        StringBuilder result = super.getLogEntry(_result);
        result.append(FIELD_DELIMITER);
        if (uri != null) result.append(uri);
        return result;
    }
}
