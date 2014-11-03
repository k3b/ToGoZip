package de.k3b.zip;

import java.io.File;

/**
 *
 * @author k3b
 *
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

}
