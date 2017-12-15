package de.k3b.android.toGoZip;

import android.content.Context;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;

import de.k3b.android.MediaUtil;
import de.k3b.zip.CompressItem;

/**
 * Created by k3b on 16.02.2015.
 */
public class AndroidUriCompressItem extends CompressItem {
    private final Uri uri;
    private final Context context;

    public AndroidUriCompressItem(Context context, Uri uri, String mimeType) {
        this.context = context;
        this.uri = uri;
        String name = MediaUtil.getFileName(context, uri, mimeType);
        setZipEntryFileName(name);

    }

    @Override
    public InputStream getFileInputStream() throws IOException {
        return this.context.getContentResolver().openInputStream(this.uri);
    }

    @Override
    public long getLastModified() {
        return MediaUtil.getDateModified(this.context, this.uri);
    }


    @Override
    public String toString() {
        return (this.uri != null) ? this.uri.toString() : super.toString();
    }
}
