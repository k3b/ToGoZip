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
package de.k3b.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Encapsulates all storage related operations that relate to a java.io.File.
 * The method-name are nearly the same as for java.io.File except
 * that String parameters named suffixXXXX are variations of the original name.
 *
 * Created by k3b on 12.12.2017.
 */

public class ZipStorageFile implements ZipStorage {
    private final File fileCur;
    private final File fileNew;
    private final File fileOld;

    /**
     * Constructs a new file using the specified path.
     *
     * @param path the path to be used for the file.
     * @throws NullPointerException if {@code path} is {@code null}.
     */
    public ZipStorageFile(String path) {
        this.fileCur = new File(path + SUFFIX_CURRENT_ZIP);
        this.fileNew = new File(path + SUFFIX_NEW_ZIP);
        this.fileOld = new File(path + SUFFIX_OLD_ZIP);
    }

    @Override
    public boolean exists() {
        return fileCur.exists();
    }

    /**
     *  i.e. new ZipStorage("/path/to/file.zip").delete(".tmp") will
     *  delete "/path/to/file.zip.tmp"
     */
    @Override
    public boolean delete(Instance suffix) {
        return file(suffix).delete();
    }

    /**
     *  i.e. new ZipStorage("/path/to/file.zip").createOutputStream(".tmp") will
     *  delete exisit "/path/to/file.zip.tmp", create dirs "/path/to" and
     *  create outputstream for "/path/to/file.zip.tmp"
     */
    @Override
    public OutputStream createOutputStream(Instance suffix) throws FileNotFoundException {
        // create parent dirs if not exist
        fileCur.getParentFile().mkdirs();

        // i.e. /path/to/somefile.zip.tmp
        File newZip1 = file(suffix);

        // replace existing
        newZip1.delete();
        return new FileOutputStream(newZip1);
    }

    /**
     *  i.e. new ZipStorage("/path/to/file.zip").createInputStream(".tmp") will
     *  create intputstream for "/path/to/file.zip.tmp"
     */
    @Override
    public InputStream createInputStream() throws FileNotFoundException {
        // return this;
        return new FileInputStream(fileCur);
    }

    /**
     *  i.e. new ZipStorage("/path/to/file.zip").getName() will
     *  return "file.zip"
     * @param suffix
     */
    @Override
    public String getName(Instance suffix) {
        return file(suffix).getName();
    }

    /**
     *  i.e. new ZipStorage("/path/to/file.zip").getAbsolutePath() will
     *  return "/path/to/file.zip"
     */
    @Override
    public String getAbsolutePath() {
        return fileCur.getAbsolutePath();
    }

    /**
     *  i.e. new ZipStorage("/path/to/file.zip").rename(".tmp","") will
     *  rename from "/path/to/file.zip.tmp" to from "/path/to/file.zip"
     */
    @Override
    public boolean rename(Instance suffixFrom, Instance suffixTo) {
        return file(suffixFrom).renameTo(file(suffixTo));
    }


    private File file(Instance suffix) {
        switch (suffix) {
            case new_: return fileNew;
            case old: return fileOld;
            default: return fileCur;
        }
    }


}