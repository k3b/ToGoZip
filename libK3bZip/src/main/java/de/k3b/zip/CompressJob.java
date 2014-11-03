package de.k3b.zip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class CompressJob {
    private static final Logger logger = LoggerFactory.getLogger(CompressJob.class);

    // global settings
    /** remove obsoled bak file when done */
    private boolean optDeleteBakFileWhenFinished = true;

    /**
     * if filename already existed in old zip rename it. Else Do not copy old
     * zipItem into new.
     */
    private boolean optRenameExistingOldEntry = true;

    private List<CompressItem> items = new ArrayList<CompressItem>();
    private File destZip;

    public CompressJob(File destZip) {
        this.destZip = destZip;
    }

    public void add(String destZipPath, String... srcFiles) {
        if (srcFiles != null) {
            for (String srcFile : srcFiles) {
                addItem(destZipPath, new File(srcFile));
            }
        }

    }

    private void addItem(String destZipPath, File srcFile) {
        CompressItem item = new CompressItem().setFile(srcFile).setZipFileName(
                destZipPath + srcFile.getName());
        items.add(item);
    }

    public List<CompressItem> handleDuplicates() {
        if ((destZip != null) && (destZip.exists())) {

            ZipFile zipFile = null;
            try {
                zipFile = new ZipFile(destZip);
                return handleDuplicates(zipFile);
            } catch (ZipException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (zipFile != null)
                    try {
                        zipFile.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
            }
        }
        return null;
    }

    /** a unittest friendly version of handleDuplicates */
    List<CompressItem> handleDuplicates(ZipFile zipFile) {
        List<CompressItem> result = new ArrayList<CompressItem>();
        for(CompressItem item : this.items) {
            String zipFileName = (item != null) ? item.getZipFileName() : null;
            ZipEntry zipEntry = (zipFileName != null) ? zipFile.getEntry(zipFileName) : null;

            if (zipEntry != null) {
                item.setZipFileName(getFixedZipFileName(zipFile, zipEntry,
                        item.getFile().lastModified()));
                result.add(item);
            }
        }

        if (result.size() > 0) {
            return result;
        }
        return null;
    }

    /** package to allow unittesting */
    String getFixedZipFileName(ZipFile zipFile, ZipEntry zipEntry,
                                       long lastModified) {
        String zipFileName = zipEntry.getName();
        if (!optRenameExistingOldEntry) {
            logger.debug("do not include: optRenameExistingOldEntry disabled {}", zipFileName);
            return null;
        }

        if (lastModified == zipEntry.getTime()) {
            logger.debug("do not include: duplicate with same datetime found {}", zipFileName);
            return null;
        }

        String extension = ")";
        int extensionPosition = zipFileName.lastIndexOf(".");
        if (extensionPosition >= 0) {
            extension = ")" + zipFileName.substring(extensionPosition);
            zipFileName = zipFileName.substring(0, extensionPosition) + "(";
        }
        int id = 1;
        while (true) {
            String newZifFileName = zipFileName + id + extension;
            ZipEntry newZipEntry = zipFile.getEntry(newZifFileName);
            if (newZipEntry == null) {
                logger.debug("renamed zipentry from '{}' to '{}'", zipFileName, newZifFileName);
                return newZifFileName;
            }
            if (lastModified == newZipEntry.getTime()) {
                logger.debug("do not include: duplicate with same datetime found '{}' for '{}'",
                        newZifFileName, zipFileName);
                return null;
            }

            id++;
        }
    }

}
