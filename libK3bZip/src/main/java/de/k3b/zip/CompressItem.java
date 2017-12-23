package de.k3b.zip;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by k3b on 16.02.2015.
 */
abstract public class CompressItem {
    protected boolean processed;
    private String zipEntryFileName;

    abstract public InputStream getFileInputStream() throws IOException ;

    abstract public long getLastModified();

    public boolean isSame(CompressItem other) {
        if (other == null) return false;
        return this.getClass().equals(other.getClass());
    }

    public String getZipEntryFileName() {
        return zipEntryFileName;
    }

    public CompressItem setZipEntryFileName(String zipEntryFileName) {
        this.zipEntryFileName = zipEntryFileName;
        return this;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }
}
