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
package de.k3b.zip;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Defines the Zipfile where zip processing is done with.
 *
 * Encapsulates all storage related operations on the Zipfile.
 *
 * Goal  Having two implementations for zipiing:
 * * an android-independant {@link ZipStorageFile} based on java.io.File
 * * an android saf implementation ZipStorageDocumentFile based on android.support.v4.provider.DocumentFile
 *
 * The method-names are nearly the same as for java.io.File except
 * that there is an additional parameter {@link ZipInstance}
 * that tells which current zip file to be used.
 *
 * Created by k3b on 12.12.2017.
 */
public interface ZipStorage {
    /** return true if ZipInstance.current exists */
    boolean exists();

    /**
     * return true if zip file directory exists and is writable
     */
    boolean writableZipFileParentDirectoryExists();

    /**
     * deletes the zip file.
     *  i.e. new ZipStorage("/path/to/file.zip").delete(ZipStorage.ZipInstance.new_) will
     *  delete "/path/to/file.tmp.zip"
     */
    boolean delete(ZipInstance zipInstance);

    /**
     * Creates an output stream that reperesents an empty zip file where files can be added to.
     *  i.e. new ZipStorage("/path/to/file.zip").createOutputStream(ZipStorage.ZipInstance.new_) will
     *  delete exisit "/path/to/file.tmp.zip", create dirs "/path/to" and
     *  create outputstream for "/path/to/file.tmp.zip"
     */
    OutputStream createOutputStream(ZipInstance zipInstance) throws FileNotFoundException;

    /**
     * Creates an input stream that reperesents a zip file where files can be extracted from.
     *  i.e. new ZipStorage("/path/to/file.zip").createInputStream() will
     *  create intputstream for "/path/to/file.zip"
     */
    InputStream createInputStream() throws FileNotFoundException;

    /**
     * get zip filename for zipInstance
     *  i.e. new ZipStorage("/path/to/file.zip").getName(ZipStorage.ZipInstance.current) will
     *  return "file.zip"
     * @param zipInstance
     */
    String getZipFileNameWithoutPath(ZipInstance zipInstance);

    /**
     * get absolute path of zipFile that is compatible with File.
     *  i.e. new ZipStorage("/path/to/file.zip").getAbsolutePath() will
     *  return "/path/to/file.zip"
     */
    String getAbsolutePath();

    /**
     * get absolute path zipFile as Uri-string or null if zip does not exist
     */
    String getFullZipUriOrNull();

    /**
         * rename zipfile.
         *  i.e. new ZipStorage("/path/to/file.zip").rename(ZipStorage.ZipInstance.new_,ZipStorage.ZipInstance.current) will
         *  rename from "/path/to/file.tmp.zip" to from "/path/to/file.zip"
         */
    boolean rename(ZipInstance zipInstanceFrom, ZipInstance zipInstanceTo);

    /** While processing a zip-file there can be 3 different instances of the zip:
     * current, old, new */
    public enum ZipInstance {
        /** path of the zip when CompressJob has finished i.e. /path/to/file.zip */
        current(SUFFIX_CURRENT_ZIP),

        /** path of the original unmodified zip while CompressJob is active /path/to/file.bak.zip */
        old(SUFFIX_OLD_ZIP),
        /** path of the new updated zip while CompressJob is active /path/to/file.tmp.zip */
        new_(SUFFIX_NEW_ZIP),

        /** path of the crash log file if something goes wrong (or save logcat in settings is pressed) /path/to/file.crash.log */
        logfile(SUFFIX_CRASH_LOG);

        private final String zipFileSuffix;

        private ZipInstance(String zipFileSuffix) {
            this.zipFileSuffix = zipFileSuffix;
        }

        public String getZipFileSuffix() {
            return zipFileSuffix;
        }
    }

    static final String SUFFIX_NEW_ZIP = ".tmp.zip";
    static final String SUFFIX_OLD_ZIP = ".bak.zip";
    static final String SUFFIX_CURRENT_ZIP = "";
    static final String SUFFIX_CRASH_LOG = ".crash.log";
}
