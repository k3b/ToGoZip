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
package de.k3b.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Encapsulates all storage related operations that relate to Zipfile.
 * 
 * This is a java.io.File based implementation for j2se junittests and for android-4.4 and below.
 * 
 * The method-names are nearly the same as for java.io.File except
 * that there is an additional parameter {@link ZipInstance}
 * that tells which current zip file to be used.
 *
 * Created by k3b on 12.12.2017.
 */

public class ZipStorageFile implements de.k3b.zip.ZipStorage {
    private final File fileCur;
    private final File fileNew;
    private final File fileOld;

    /**
     * Constructs a new zip-file-storage using the specified path.
     *
     * @param path the path to be used for the zip-file.
     * @throws NullPointerException if {@code path} is {@code null}.
     */
    public ZipStorageFile(String path) {
        this.fileCur = new File(path + SUFFIX_CURRENT_ZIP);
        this.fileNew = new File(path + SUFFIX_NEW_ZIP);
        this.fileOld = new File(path + SUFFIX_OLD_ZIP);
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public boolean exists() {
        return fileCur.exists();
    }

    /**
     * return true if zip file directory exists
     */
    @Override
    public boolean writableZipFileParentDirectoryExists() {
        File directory = (fileCur != null) ? fileCur.getParentFile() : null;
        return (directory != null) && (directory.exists()) && (directory.isDirectory()) && (directory.canWrite());
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public boolean delete(ZipInstance zipInstance) {
        return file(zipInstance).delete();
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public OutputStream createOutputStream(ZipInstance zipInstance) throws FileNotFoundException {
        // create parent dirs if not exist
        fileCur.getParentFile().mkdirs();

        // i.e. /path/to/somefile.tmp.zip
        File newZip = file(zipInstance);

        // replace existing
        newZip.delete();
        return new FileOutputStream(newZip);
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public InputStream createInputStream() throws FileNotFoundException {
        // return this;
        return new FileInputStream(fileCur);
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public String getZipFileNameWithoutPath(ZipInstance zipInstance) {
        return file(zipInstance).getName();
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public String getAbsolutePath() {
        return fileCur.getAbsolutePath();
    }

    /**
     * get absolute path zipFile as Uri-string or null if zip does not exist
     */
    @Override
    public String getFullZipUriOrNull() {
        return "file://" + getAbsolutePath();
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public boolean rename(ZipInstance zipInstanceFrom, ZipInstance zipInstanceTo) {
        return file(zipInstanceFrom).renameTo(file(zipInstanceTo));
    }


    private File file(ZipInstance zipInstance) {
        switch (zipInstance) {
            case new_: return fileNew;
            case old: return fileOld;
            default: return fileCur;
        }
    }
}