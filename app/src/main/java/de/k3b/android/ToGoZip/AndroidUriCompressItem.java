package de.k3b.android.toGoZip;

import java.io.IOException;
import java.io.InputStream;

import de.k3b.zip.CompressItem;

/**
 * Created by k3b on 16.02.2015.
 */
public class AndroidUriCompressItem extends CompressItem {
    @Override
    public InputStream getFileInputStream() throws IOException {
        return null;
    }

    @Override
    public long getLastModified() {
        return 0;
    }
}
