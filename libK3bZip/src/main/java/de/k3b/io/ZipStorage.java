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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Encapsulates all storage related operations that relate to a java.io.File.
 * The method-name are nearly the same as for java.io.File except
 * that String parameters named suffixXXXX are variations of the original name.
 *
 * Created by k3b on 12.12.2017.
 */
public interface ZipStorage {
    boolean exists();

    /**
     *  i.e. new ZipStorage("/path/to/file.zip").delete(".tmp") will
     *  delete "/path/to/file.zip.tmp"
     */
    boolean delete(Instance suffix);

    /**
     *  i.e. new ZipStorage("/path/to/file.zip").createOutputStream(".tmp") will
     *  delete exisit "/path/to/file.zip.tmp", create dirs "/path/to" and
     *  create outputstream for "/path/to/file.zip.tmp"
     */
    OutputStream createOutputStream(Instance suffix) throws FileNotFoundException;

    /**
     *  i.e. new ZipStorage("/path/to/file.zip").createInputStream(".tmp") will
     *  create intputstream for "/path/to/file.zip.tmp"
     */
    InputStream createInputStream() throws FileNotFoundException;

    /**
     *  i.e. new ZipStorage("/path/to/file.zip").getName() will
     *  return "file.zip"
     * @param suffix
     */
    String getName(Instance suffix);

    /**
     *  i.e. new ZipStorage("/path/to/file.zip").getAbsolutePath() will
     *  return "/path/to/file.zip"
     */
    String getAbsolutePath();

    /**
     *  i.e. new ZipStorage("/path/to/file.zip").rename(".tmp","") will
     *  rename from "/path/to/file.zip.tmp" to from "/path/to/file.zip"
     */
    boolean rename(Instance suffixFrom, Instance suffixTo);

    public enum Instance {
        /** path of the zip when CompressJob has finished i.e. /path/to/file.zip */
        current,

        /** path of the original unmodified zip while CompressJob is active /path/to/file.zip.bak */
        old,
        /** path of the new updated zip while CompressJob is active /path/to/file.zip.tmp */
        new_}

    static final String SUFFIX_NEW_ZIP = ".tmp.zip";
    static final String SUFFIX_OLD_ZIP = ".bak.zip";
    static final String SUFFIX_CURRENT_ZIP = "";


}
