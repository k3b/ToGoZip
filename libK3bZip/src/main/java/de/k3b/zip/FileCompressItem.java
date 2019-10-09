/*
 * Copyright (C) 2014-2019 k3b
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.k3b.io.FileNameUtil;

/**
 * One android independant, java.io.File based  dto-item that should be compressed.<br/>
 * <p/>
 * Author k3b
 */
public class FileCompressItem extends CompressItem {
    private static final Logger logger = LoggerFactory.getLogger(LibZipGlobal.LOG_TAG);
    private static final String DBG_CONTEXT = "FileCompressItem:";

    /** source file to be compressed */
    private File file;

    /** if not null file adds will be relative to this path if file is below this path */
    private static String zipRelPath = null;

    /**
     *
     * @param outDirInZipForNoneRelPath directory with trailing "/" (without filename) where
     *                                   the entry goes to if outside FileCompressItem.zipRelPath.
     *                                   null==root dir.
     * @param srcFile full path to source file
     * @param zipEntryComment
     */
    public FileCompressItem(String outDirInZipForNoneRelPath, File srcFile, String zipEntryComment) {
        String zipEntryName = calculateZipEntryName(outDirInZipForNoneRelPath, srcFile, FileCompressItem.zipRelPath);
        setFile(srcFile);
        setZipEntryFileName(zipEntryName);
        setZipEntryComment(zipEntryComment);
    }

    /**
     * Calculates the path within the zip file.
     *
     * Scope: package to allow unittesting.
     *
     * @param outDirInZipForNoneRelPath directory with trailing "/" (without filename) where
     *                                   the entry goes to if outside zipRelPath.
     *                                   null==root dir.
     * @param srcFile full path to source file
     * @param zipRelPath if not empty paths are caclulated relative to this directory.
     *                   Must have trailing "/" and be lower case.
     * @return
     */
    static String calculateZipEntryName(String outDirInZipForNoneRelPath, File srcFile, String zipRelPath) {
        String result = FileNameUtil.makePathRelative(zipRelPath, srcFile);

        if (result == null) {
            StringBuilder resultMessage = new StringBuilder();

            if (outDirInZipForNoneRelPath != null)
                resultMessage.append(outDirInZipForNoneRelPath);
            resultMessage.append(srcFile.getName());
            result = resultMessage.toString();
        }
        if (LibZipGlobal.debugEnabled) {
            logger.info(DBG_CONTEXT + "[match={}]  <== calculateZipEntryName(... , srcFile='{}' , zipRelPath='{}')",
                    result, srcFile, zipRelPath);
        }
        return result;
    }

    /** if not null enables zipRelPath mode. "add"-s will be relative to this path if file is below this path */
    public static void setZipRelPath(File zipRelPath) {
        if (zipRelPath != null) {
            FileCompressItem.zipRelPath = FileNameUtil.getCanonicalPath(zipRelPath).toLowerCase();
        } else {
            FileCompressItem.zipRelPath = null;
        }
        if (LibZipGlobal.debugEnabled) {
            logger.info(DBG_CONTEXT + "setZipRelPath('{}') from '{}'",FileCompressItem.zipRelPath, zipRelPath );
        }
    }


    /** source file to be compressed */
    public FileCompressItem setFile(File file) {
        this.file = file;
        this.processed = false;
        return this;
    }

    /** source file to be compressed */
    public File getFile() {
        return file;
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public InputStream getFileInputStream() throws IOException {
        return new FileInputStream(file);
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public long getLastModified() {
        return this.getFile().lastModified();
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public boolean isSame(CompressItem other) {
        return super.isSame(other) && (this.file.equals(((FileCompressItem) other).file));
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public StringBuilder getLogEntry(StringBuilder _result) {
        StringBuilder result = super.getLogEntry(_result);
        result.append(FIELD_DELIMITER);
        if (getFile() != null) result.append(getFile());
        return result;
    }

}
