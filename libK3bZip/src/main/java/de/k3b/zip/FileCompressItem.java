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
package de.k3b.zip;

import java.io.*;

import de.k3b.io.FileUtils;
import de.k3b.io.StringUtils;

/**
 * One android independant, java.io.File based  dto-item that should be compressed.<br/>
 * <p/>
 * Author k3b
 */
public class FileCompressItem extends CompressItem {
    /** source file to be compressed */
    private File file;

    /** if not null file adds will be relative to this path if file is below this path */
    private static String zipRelPath = null;

    /**
     *
     * @param destZipPathWithoutFileName directory with trailing "/" (without filename) where the entry goes to. null==root dir.
     * @param srcFile full path to source file
     * @param zipEntryComment
     */
    public FileCompressItem(String destZipPathWithoutFileName, File srcFile, String zipEntryComment) {
        String zipEntryName = calculateZipEntryName(destZipPathWithoutFileName, srcFile, FileCompressItem.zipRelPath);
        setFile(srcFile);
        setZipEntryFileName(zipEntryName);
        setZipEntryComment(zipEntryComment);
    }

    /**
     * Calculates the path within the zip file.
     *
     * Scope: package to allow unittesting.
     *
     * @param destZipPathWithoutFileName
     * @param srcFile
     * @param zipRelPath
     * @return
     */
    static String calculateZipEntryName(String destZipPathWithoutFileName, File srcFile, String zipRelPath) {
        if (!StringUtils.isNullOrEmpty(zipRelPath)) {
            String srcPath = getCanonicalPath(srcFile);
            if (srcPath.startsWith(zipRelPath)) {
                String result = srcPath.substring(zipRelPath.length()+1);
                return result;
            }
        }

        StringBuilder result = new StringBuilder();

        if (destZipPathWithoutFileName != null) result.append(destZipPathWithoutFileName);
        result.append(srcFile.getName());
        return result.toString();
    }

    /** if not null file adds will be relative to this path if file is below this path */
    public static void setZipRelPath(File zipRelPath) {
        FileCompressItem.zipRelPath = getCanonicalPath(zipRelPath);
    }

    /** so that files are comparable */
    static String getCanonicalPath(File zipRelPath) {
        File canonicalFile = FileUtils.tryGetCanonicalFile(zipRelPath);
        if (canonicalFile != null) {
            return FileUtils.fixPath(canonicalFile.getAbsolutePath());
        }
        return null;
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
