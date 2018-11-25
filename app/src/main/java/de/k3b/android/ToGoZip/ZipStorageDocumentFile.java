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
 * Encapsulates all storage related operations that relate to a Zipfile.
 *
 * This is a android.support.v4.provider.DocumentFile based implementation for android-5.0ff.
 *
 * The method-names are nearly the same as for java.io.File except
 * that there is an additional parameter {@link ZipInstance}
 * that tells which current zip file to be used.
 *
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

    /**
     *  {@inheritDoc}
     */
    @Override
    public boolean exists() {

        return null != directory.findFile(filename);
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public boolean delete(ZipInstance zipInstance) {
        DocumentFile zipFile = directory.findFile(getZipFileNameWithoutPath(zipInstance));
        return (zipFile != null) && zipFile.delete();
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public OutputStream createOutputStream(ZipInstance zipInstance) throws FileNotFoundException {
        // find existing
        DocumentFile zipFile = getDocumentFile(zipInstance);

        // if not found create it.
        if (zipFile == null) zipFile = directory.createFile(MIMETYPE_ZIP, getZipFileNameWithoutPath(zipInstance));

        if (zipFile != null) return context.getContentResolver().openOutputStream(zipFile.getUri(), "w");

        return null;
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public String getFullZipUriOrNull() {
        DocumentFile zipFile = getDocumentFile(ZipStorage.ZipInstance.current);
        if (zipFile != null) return zipFile.getUri().toString();
        return null;
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public String getFullZipDirUriOrNull() {
        return directory.getUri().toString();
    }

    private DocumentFile getDocumentFile(ZipInstance zipInstance) {
        return directory.findFile(getZipFileNameWithoutPath(zipInstance));
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public InputStream createInputStream() throws FileNotFoundException {
        DocumentFile zipFile = directory.findFile(filename);
        if (zipFile != null) return context.getContentResolver().openInputStream(zipFile.getUri());
        return null;
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public String getAbsolutePath() {
        return directory.getUri().getPath() + "/" + filename;
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public boolean rename(ZipInstance zipInstanceFrom, ZipInstance zipInstanceTo) {
        DocumentFile zipFile = directory.findFile(getZipFileNameWithoutPath(zipInstanceFrom));
        if (zipFile != null) return zipFile.renameTo(getZipFileNameWithoutPath(zipInstanceTo));
        return false;
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public String getZipFileNameWithoutPath(ZipInstance zipInstance) {
        return filename + zipInstance.getZipFileSuffix();
    }
}
