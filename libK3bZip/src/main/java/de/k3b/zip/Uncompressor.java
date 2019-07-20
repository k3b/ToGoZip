package de.k3b.zip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import de.k3b.io.FileUtils;

public class Uncompressor {
    private final ZipStorage zipStorage;

    public Uncompressor(ZipStorage zipStorage) {
        this.zipStorage = zipStorage;
    }
    /**
     * Copy ZipStorage[zipEntryFileName] to zipEntryContentConsumer
     *
     * @param zipEntryFileName file name (with path) inside the zip that should be read
     * @param zipEntryContentConsumer where the found content is copied to.
     * @return false if zipEntryFileName not found
     * @throws IOException if zipStorage not found or corrupt
     */
    public boolean copyContentOfZipEntryTo(String zipEntryFileName, OutputStream zipEntryContentConsumer) throws IOException {
        ZipInputStream zipInputStream = null;

        try {
            zipInputStream = new ZipInputStream(this.zipStorage.createInputStream());
            for (ZipEntry zipOldEntry = zipInputStream.getNextEntry(); zipOldEntry != null; zipOldEntry = zipInputStream
                    .getNextEntry()) {
                if (CompressJob.isSameFile(zipEntryFileName, zipOldEntry)) {
                    FileUtils.copyStream(zipEntryContentConsumer, zipInputStream, new byte[4096]);
                    return true;
                }
            }
        } finally {
            FileUtils.close(zipInputStream, this.zipStorage.getAbsolutePath());
        }
        return false;
    }

    public String getContentOfZipEntryAsText(String zipEntryFileName)  throws IOException {
        String result = null;
        ByteArrayOutputStream byteArrayOutputStream = null;

        try {
            byteArrayOutputStream = new ByteArrayOutputStream();

            if (copyContentOfZipEntryTo(zipEntryFileName, byteArrayOutputStream)) {
                result = new String(byteArrayOutputStream.toByteArray());
                byteArrayOutputStream.flush();
            }
        } finally {
            FileUtils.close(byteArrayOutputStream, zipEntryFileName);
        }
        return result;
    }

}
