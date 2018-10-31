/*
 * Copyright (C) 2017-2018 k3b
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

import android.content.Context;
import android.support.v4.provider.DocumentFile;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

import de.k3b.zip.ZipStorage;

/**
 * Created by k3b on 22.12.2017.
 */

public class ZipStorageDocumentFile implements ZipStorage {

    private static final String MIMETYPE_ZIP = "application/zip";
    private final Context context;
    private final DocumentFile directory;
    private final String filename;

    public ZipStorageDocumentFile(Context context, DocumentFile directory, String filename) {
        this.context = context;
        this.directory = directory;
        this.filename = filename;
    }

    @Override
    public boolean exists() {

        return null != directory.findFile(filename);
    }

    /**
     * i.e. new ZipStorage("/path/to/file.zip").delete(".tmp") will
     * delete "/path/to/file.zip.tmp"
     *
     * @param suffix
     */
    @Override
    public boolean delete(Instance suffix) {
        DocumentFile doc = directory.findFile(getName(suffix));
        return (doc != null) && doc.delete();
    }

    /**
     * i.e. new ZipStorage("/path/to/file.zip").createOutputStream(".tmp") will
     * delete exisit "/path/to/file.zip.tmp", create dirs "/path/to" and
     * create outputstream for "/path/to/file.zip.tmp"
     *
     * @param suffix
     */
    @Override
    public OutputStream createOutputStream(Instance suffix) throws FileNotFoundException {
        DocumentFile doc = directory.findFile(getName(suffix));
        if (doc == null) doc = directory.createFile(MIMETYPE_ZIP, getName(suffix));
        if (doc != null) return context.getContentResolver().openOutputStream(doc.getUri(), "w");
        return null;
    }

    /**
     * i.e. new ZipStorage("/path/to/file.zip").createInputStream(".tmp") will
     * create intputstream for "/path/to/file.zip.tmp"
     */
    @Override
    public InputStream createInputStream() throws FileNotFoundException {
        DocumentFile doc = directory.findFile(filename);
        if (doc != null) return context.getContentResolver().openInputStream(doc.getUri());
        return null;
    }

    /**
     * i.e. new ZipStorage("/path/to/file.zip").getAbsolutePath() will
     * return "/path/to/file.zip"
     */
    @Override
    public String getAbsolutePath() {
        return directory.getUri().getPath() + "/" + filename;
    }

    /**
     * i.e. new ZipStorage("/path/to/file.zip").rename(".tmp","") will
     * rename from "/path/to/file.zip.tmp" to from "/path/to/file.zip"
     *
     * @param suffixFrom
     * @param suffixTo
     */
    @Override
    public boolean rename(Instance suffixFrom, Instance suffixTo) {
        DocumentFile doc = directory.findFile(getName(suffixFrom));
        if (doc != null) return doc.renameTo(getName(suffixTo));
        return false;
    }

    /**
     * i.e. new ZipStorage("/path/to/file.zip").getName() will
     * return "file.zip"
     *
     * @param suffix
     */
    @Override
    public String getName(Instance suffix) {
        switch (suffix) {
            case new_: return filename + SUFFIX_NEW_ZIP;
            case old: return filename + SUFFIX_OLD_ZIP;
            default: return filename;
        }
    }
}
