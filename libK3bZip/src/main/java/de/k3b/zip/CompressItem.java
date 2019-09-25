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

import java.io.IOException;
import java.io.InputStream;

/**
 * Something that can be added to a zip file via a {@link CompressJob}.
 *
 * Created by k3b on 16.02.2015.
 */
abstract public class CompressItem {
    protected static final String FIELD_DELIMITER = ";";
    protected boolean processed;

    /** the antry may contain path within zip file */
    private String zipEntryFileName;

    /** if not null this will become the zip entry comment */
    private String zipEntryComment;

    /**
     * false means store without compression, which is faster
     */
    private boolean doCompress = true;

    /** stream where the source file can be read from. */
    abstract public InputStream getFileInputStream() throws IOException ;

    /** when source file was last modified. */
    abstract public long getLastModified();

    /** return true if both refer to the same entry in zip file */
    public boolean isSame(CompressItem other) {
        if (other == null) return false;
        return this.zipEntryFileName.equals(other.zipEntryFileName);
    }

    /** the entry may contain path within zip file */
    public String getZipEntryFileName() {
        return zipEntryFileName;
    }

    /** the entry may contain path within zip file */
    public CompressItem setZipEntryFileName(String zipEntryFileName) {
        this.zipEntryFileName = zipEntryFileName;
        return this;
    }

    /**
     * false means store without compression, which is faster
     */
    public boolean isDoCompress() {
        return doCompress;
    }

    public void setDoCompress(boolean doCompress) {
        this.doCompress = doCompress;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    /** if not null this will become the zip entry comment */
    public String getZipEntryComment() {
        return zipEntryComment;
    }

    public void setZipEntryComment(String zipEntryComment) {
        this.zipEntryComment = zipEntryComment;
    }

    public StringBuilder getLogEntry(StringBuilder _result) {
        StringBuilder result = (_result == null) ? new StringBuilder() : _result;
        result.append(getZipEntryFileName());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = getLogEntry(null);
        result.insert(0, FIELD_DELIMITER);
        result.insert(0,this.getClass().getSimpleName());
        result.insert(0, processed ? "[v] " : "[ ] ");
        return result.toString();
    }
}
