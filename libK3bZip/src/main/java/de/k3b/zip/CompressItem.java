package de.k3b.zip;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by k3b on 16.02.2015.
 */
abstract public class CompressItem {
    protected boolean processed;
    private String zipFileName;

    abstract public InputStream getFileInputStream() throws IOException ;

    abstract public long getLastModified();

    public boolean isSame(CompressItem other) {
        if (other == null) return false;
        return this.getClass().equals(other.getClass());
    }

    public String getZipFileName() {
        return zipFileName;
    }

    public CompressItem setZipFileName(String zipFileName) {
        this.zipFileName = zipFileName;
        return this;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }
}
