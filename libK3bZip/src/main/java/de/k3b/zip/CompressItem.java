/*
 * Copyright (C) 2014 k3b
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

/**
 * One dto-item that should be compressed.<br/>
 * <p/>
 * Author k3b
 */
public class CompressItem {
    private File file;
    private String zipFileName;

    public File getFile() {
        return file;
    }

    public CompressItem setFile(File file) {
        this.file = file;
        return this;
    }

    public String getZipFileName() {
        return zipFileName;
    }

    public CompressItem setZipFileName(String zipFileName) {
        this.zipFileName = zipFileName;
        return this;
    }

    public InputStream getFileInputStream() throws IOException {
        return new FileInputStream(file);
    }
}
