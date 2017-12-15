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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import de.k3b.io.IFile;

/**
 * Android independant enginge that adds files to a zip file via a compressQue.
 */
public class CompressJob implements ZipLog {
    //!!! next handle text append in copy loop
    // private static final boolean useOldImpl_Depricated = false;
    private static final boolean useOldImpl_Depricated = true;

    public static final int RESULT_NO_CHANGES = 0;
    public static final int RESULT_ERROR_ABOART = -1;
    private static final Logger logger = LoggerFactory.getLogger(CompressJob.class);

    // global processing options
    /**
     * true: remove obsolete bak file when done
     */
    private final boolean optDeleteBakFileWhenFinished = true;

    /**
     * true: if filename already existed in old zip rename it. Else Do not copy old
     * zipItem into new.
     */
    private final boolean optRenameExistingOldEntry = true;

    /**
     * logger used for compressing or null
     */
    private final ZipLog zipLog;

    /**
     * items To Be Added to be processed in the job.
     */
    private List<CompressItem> compressQue = new ArrayList<CompressItem>();

    private TextCompressItem compressTextItem = null;

    /**
     * where old entries come from and new entries go to
     */
    protected IFile destZip;

    // used to copy content
    private byte[] buffer = new byte[4096];

    /**
     * Creates a job.
     *
     * @param destZip full path to the zipfile where the new files should be added to
     * @param zipLog  if not null use this for logging.
     */
    public CompressJob(IFile destZip, ZipLog zipLog) {
        this.destZip = destZip;
        this.zipLog = zipLog;
    }

    /**
     * local helper to copy stream-data to zip
     */
    static void copyStream(OutputStream outputStream, InputStream inputStream, byte[] buffer) throws IOException {
        for (int read = inputStream.read(buffer); read > -1; read = inputStream
                .read(buffer)) {
            outputStream.write(buffer, 0, read);
        }
    }

    /**
     * Remember files that should be added to the zip.
     * Used in unittests.
     *
     * @param destZipPath where the files will go to
     * @param srcFiles    path where new-toBeZipped-compressQue come from
     */
    void addToCompressQue(String destZipPath, String... srcFiles) {
        if (srcFiles != null) {
            for (String srcFile : srcFiles) {
                addToCompressQue(destZipPath, new File(srcFile));
            }
        }

    }

    /**
     * Remember files that should be added to the zip.
     */
    public CompressItem addToCompressQue(String destZipPath, File... srcFiles) {
        CompressItem item = null;
        if (srcFiles != null) {
            for (File srcFile : srcFiles) {
                if (srcFile.isDirectory()) {
                    String subDir = (destZipPath.length() > 0) ? destZipPath + "/" + srcFile.getName() + "/" : srcFile.getName() + "/";
                    addToCompressQue(subDir, srcFile.listFiles());
                } else if (srcFile.isFile()) {
                    item = addItemToCompressQue(destZipPath, srcFile);
                }
            }
        }
        return item;
    }

    public boolean addToCompressQueue(CompressItem item) {
        if (findInCompressQue(item) != null) {
            // already inside current collection. Do not add again
            return false;
        }

        compressQue.add(item);
        return true;
    }

    /**
     * adds one file to the CompressQue
     */
    CompressItem addItemToCompressQue(String destZipPath, File srcFile) {
        CompressItem item = new FileCompressItem(destZipPath, srcFile);
        if (addToCompressQueue(item)) {
            return item;
        }
        return null;
    }

    public void addToCompressQueue(CompressItem[] items) {
        for (CompressItem item : items) {
            addToCompressQueue(item);
        }
    }

    public TextCompressItem addTextToCompressQue(String textfile, String textToBeAdded) {
        if ((textToBeAdded!=null) && (textToBeAdded.length() > 0)) {
            if (this.compressTextItem == null) {
                File srcFile = new File("/" + textfile);
                this.compressTextItem = new TextCompressItem(textfile, srcFile);
                this.compressTextItem.setLastModified(new Date().getTime());
                addToCompressQueue(this.compressTextItem);
            }
            this.compressTextItem.addText(textToBeAdded);
        }
        return this.compressTextItem;
    }

    /**
     * return null, if file is not in the CompressQue yet.
     */
    private CompressItem findInCompressQue(CompressItem file) {
        for (CompressItem item : this.compressQue) {
            if (file.isSame(item)) return item;
        }
        return null;
    }

    /**
     * depending on global options: duplicate zip entries are either ignored or renamed
     *
     * @return collection of items that where renamed.
     */
    public List<CompressItem> handleDuplicates_Depricated() {
        if ((this.destZip != null) && (this.destZip.exists())) {
            //!!! TODO change from ZipFile to ZipInputStream
            ZipInputStream zi = null;
            ZipOutputStream zo = null;
            ZipFile zipFile = null;
            try {
                zipFile = new ZipFile(this.destZip);
                return handleDuplicates_Depricated(zipFile);
            } catch (ZipException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (zipFile != null)
                    try {
                        zipFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }
        return null;
    }

    /**
     * a unittest friendly version of handleDuplicates_Depricated:<br/>
     * depending on global options: duplicate zip entries are either ignored or renamed
     *
     * @param existingZipEntries
     * @return collection of items that where renamed or null if no renaming took place.
     */
    List<CompressItem> handleDuplicates(Map<String, Long> existingZipEntries) {
        List<CompressItem> result = new ArrayList<CompressItem>();
        for (CompressItem itemToAdd : this.compressQue) {
            if (!itemToAdd.isProcessed()) {
                String addFileName = (itemToAdd != null) ? itemToAdd.getZipEntryFileName() : null;
                Long zipEntryFileLastModified = (addFileName != null) ? existingZipEntries.get(addFileName) : null;

                if (zipEntryFileLastModified != null) {
                    // same file name found
                    // may zipEntryExisting.setName(null) if not to add
                    itemToAdd.setZipEntryFileName(getRenamedZipEntryFileName(existingZipEntries, itemToAdd,
                            zipEntryFileLastModified));
                    result.add(itemToAdd);
                }
            }
        }
        for (int i = this.compressQue.size() - 1; i >= 0; i--) {
            if (this.compressQue.get(i).getZipEntryFileName() == null) {
                this.compressQue.remove(i);
            }
        }

        if (result.size() > 0) {
            return result;
        }
        return null;
    }

    /**
     * a unittest friendly version of handleDuplicates_Depricated:<br/>
     * depending on global options: duplicate zip entries are either ignored or renamed
     *
     * @param zipFile
     * @return collection of items that where renamed.
     */
    List<CompressItem> handleDuplicates_Depricated(ZipFile zipFile) {
        List<CompressItem> result = new ArrayList<CompressItem>();
        for (CompressItem itemToAdd : this.compressQue) {
            if (!itemToAdd.isProcessed()) {
                String zipFileName = (itemToAdd != null) ? itemToAdd.getZipEntryFileName() : null;
                ZipEntry zipEntryExisting = (zipFileName != null) ? zipFile.getEntry(zipFileName) : null;

                if (zipEntryExisting != null) {
                    itemToAdd.setZipEntryFileName(getRenamedZipEntryFileName_Deprecated(zipFile, zipEntryExisting,
                            itemToAdd.getLastModified()));
                    result.add(itemToAdd);
                }
            }
        }
        for (int i = this.compressQue.size() - 1; i >= 0; i--) {
            if (this.compressQue.get(i).getZipEntryFileName() == null) {
                this.compressQue.remove(i);
            }
        }

        if (result.size() > 0) {
            return result;
        }
        return null;
    }

    /**
     * package to allow unittesting: <br/>
     * gets a fixed (renamed) name for the zip entry or null if file
     * should not be added to zip.
     */
    String getRenamedZipEntryFileName(Map<String, Long> existingZipEntries, CompressItem itemToAdd,
                                                 long zipEntryFileLastModified) {
        String zipEntryFileName = itemToAdd.getZipEntryFileName();
        if (!optRenameExistingOldEntry) {
            logger.debug("do not include: optRenameExistingOldEntry disabled {}", zipEntryFileName);
            return null;
        }

        if (sameDate(zipEntryFileName, itemToAdd.getLastModified(), zipEntryFileLastModified)) {
            logger.debug("do not include: duplicate with same datetime found {}", zipEntryFileName);
            return null;
        }

        String extension = ")";
        int extensionPosition = zipEntryFileName.lastIndexOf(".");
        if (extensionPosition >= 0) {
            extension = ")" + zipEntryFileName.substring(extensionPosition);
            zipEntryFileName = zipEntryFileName.substring(0, extensionPosition) + "(";
        }
        int id = 1;
        while (true) {
            String newZifFileName = zipEntryFileName + id + extension;
            Long fileLastModified = existingZipEntries.get(newZifFileName);
            if (fileLastModified == null) {
                logger.debug("renamed zipentry from '{}' to '{}'", itemToAdd.getZipEntryFileName(), newZifFileName);
                return newZifFileName;
            }

            if (sameDate(newZifFileName, fileLastModified.longValue(), zipEntryFileLastModified)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("do not include: duplicate with same datetime found '{}' for '{}'",
                            newZifFileName, zipEntryFileName);
                }
                return null;
            }

            id++;
        }
    }
    /**
     * package to allow unittesting: <br/>
     * gets a fixed (renamed) name for the zip entry or null if file
     * should not be added to zip.
     */
    String getRenamedZipEntryFileName_Deprecated(ZipFile zipFile, ZipEntry zipEntry,
                                                 long lastModified) {
        String zipFileName = zipEntry.getName();
        if (!optRenameExistingOldEntry) {
            logger.debug("do not include: optRenameExistingOldEntry disabled {}", zipFileName);
            return null;
        }

        if (sameDate_Deprecated(zipEntry, lastModified)) {
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
                logger.debug("renamed zipentry from '{}' to '{}'", zipEntry.getName(), newZifFileName);
                return newZifFileName;
            }

            if (sameDate_Deprecated(newZipEntry, lastModified)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("do not include: duplicate with same datetime found '{}' for '{}'",
                            newZifFileName, zipFileName);
                }
                return null;
            }

            id++;
        }
    }

    /**
     * return true, if zipEntry has same date as fileLastModified
     */
    private boolean sameDate_Deprecated(ZipEntry zipEntry, long fileLastModified) {
        // may varay in millisec
        long zipLastModified = zipEntry.getTime();
        final String zipEntryFileName = zipEntry.getName();
        return sameDate(zipEntryFileName, fileLastModified, zipLastModified);
    }

    private boolean sameDate(String zipEntryFileName, long fileLastModified, long zipLastModified) {
        long timeDiff = Math.abs(fileLastModified - zipLastModified);

        if (logger.isDebugEnabled()) {
            DateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            logger.debug("sameDate({}): {} <=> {} : diff {} millisecs"
                    , zipEntryFileName
                    , f.format(new Date(zipLastModified))
                    , f.format(new Date(fileLastModified))
                    , timeDiff
            );
        }

        return timeDiff < 10000; // are same if diff < 10 seconds
    }

    /**
     * Processes the compressQue: renaming duplicates and add items to zip.
     *
     * @return number of compressQue in the result zip or RESULT_XXX
     * @param renameDouplicateTextFile
     */
    public int compress(boolean renameDouplicateTextFile) {
        // to make shure that orginal is not broken if there is an error:
        // 1) Workflow addToCompressQue to somefile.zip.tmp, (a) old content, (b) new content
        // 2) rename existing to somefile.zip.bak
        // 3) rename somefile.zip.tmp to somefile.zip
        // 4) delete existing to somefile.zip.bak

        // if text is to be appended to "last text-entry" do not use handleDuplicates_Depricated for that
        boolean preventTextFromRenaming = (!renameDouplicateTextFile) && (this.compressTextItem != null) && !this.compressTextItem.isProcessed();

        if (useOldImpl_Depricated) {
            if (preventTextFromRenaming) this.compressTextItem.setProcessed(true);
            handleDuplicates_Depricated();
            if (preventTextFromRenaming) this.compressTextItem.setProcessed(false);
        }

        if (compressQue.size() == 0) {
            logger.debug("aboard: no (more) files to addToCompressQue to zip");
            return RESULT_NO_CHANGES;
        }

        // global to allow garbage collection if there is an exception
        ZipOutputStream out = null;
        ZipInputStream zipInputStream = null;
        InputStream inputStream = null;
        String context = "";
        int itemCount = 0;

        try {
            // map from path to lastModifiedDate used to find the duplicates
            Map<String, Long> existingEntries = new HashMap<>();

            // i.e. /path/to/somefile.zip.tmp
            IFile newZip = new IFile(this.destZip.getAbsolutePath() + ".tmp");
            IFile oldZip = null;

            newZip.delete();
            context = traceMessage("(0) create new result file {0}", newZip);
            out = new ZipOutputStream(new FileOutputStream(newZip));

            if (this.destZip.exists()) {
                context = traceMessage("(1a) copy existing compressQue from {0} to {1}",
                        this.destZip, newZip);
                zipInputStream = new ZipInputStream(new FileInputStream(
                        this.destZip));

                for (ZipEntry zipOldEntry = zipInputStream.getNextEntry(); zipOldEntry != null; zipOldEntry = zipInputStream
                        .getNextEntry()) {
                    if (null != zipOldEntry) {
                        InputStream prependInputStream = this.getPrependInputStream(zipOldEntry, this.compressTextItem);
                        if (prependInputStream != null) {
                            context = traceMessage(
                                    "- (1a+) add text to existing item from {0} to {1} : {2}",
                                    this.destZip, newZip, zipOldEntry);
                            existingEntries.put(zipOldEntry.getName(), zipOldEntry.getTime());
                        } else {
                            context = traceMessage(
                                    "- (1a) copy existing item from {0} to {1} : {2}",
                                    this.destZip, newZip, zipOldEntry);
                        }
                        add(out, zipOldEntry, prependInputStream, zipInputStream);
                        itemCount++;
                    }
                }
                zipInputStream.close();
                zipInputStream = null;
                // i.e. /path/to/somefile.zip.bak
                oldZip = new IFile(this.destZip.getAbsolutePath() + ".bak");
            }

            if (!useOldImpl_Depricated) {
                if (preventTextFromRenaming) this.compressTextItem.setProcessed(true);
                handleDuplicates(existingEntries);
                if (preventTextFromRenaming) this.compressTextItem.setProcessed(false);
            }

            if ((this.compressTextItem != null) && !this.compressTextItem.isProcessed()) {
                this.compressTextItem.addText(this.getTextFooter());
            }

            // (1b) copy new compressQue
            for (CompressItem item : this.compressQue) {
                if (!item.isProcessed()) {
                    String newFullDestZipItemName = item.getZipEntryFileName();
                    context = traceMessage("(1b) copy new item {0} as {1} to {2}",
                            item, newFullDestZipItemName, newZip);
                    inputStream = item.getFileInputStream();
                    ZipEntry zipEntry = createZipEntry(newFullDestZipItemName,
                            item.getLastModified(), null);
                    add(out, zipEntry, null, inputStream);
                    inputStream.close();
                    inputStream = null;
                    itemCount++;
                    item.setProcessed(true);
                }
            }

            out.close();
            out = null;

            // no exception yet: Assume it is save to change the old zip
            // (2) rename existing-old somefile.zip to somefile.zip.bak
            if (oldZip != null) {
                oldZip.delete(); // should ignore error

                context = traceMessage(
                        "(2) rename old zip file from {0}  to {1}",
                        this.destZip, oldZip);
                // i.e. /path/to/somefile.zip => /path/to/somefile.zip.bak
                if (!this.destZip.renameTo(oldZip)) {
                    thowrError(context);
                }
            }

            // 3) rename new created somefile.zip.tmp to somefile.zip
            context = traceMessage("(3) rename new created zip file {0} to {1}",
                    newZip, this.destZip);
            if (!newZip.renameTo(this.destZip)) {
                // something went wrong. try to restore old zip
                // i.e. somefile.zip.bak => somefile.zip
                if (oldZip != null) {
                    oldZip.renameTo(this.destZip);
                }

                thowrError(context);
            }

            // 4) delete existing renamed old somefile.zip.bak
            if ((optDeleteBakFileWhenFinished) && (oldZip != null)) {
                context = traceMessage(
                        "(4) delete existing renamed old zip file {0}", oldZip);
                oldZip.delete();
            }
            context = traceMessage("(5a) successfull updated zip file {0}",
                    this.destZip);

        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (!errorMessage.contains(context)) {
                errorMessage = "Error in " + context + ":" + errorMessage;
            }
            logger.error(errorMessage, e);
            addError(errorMessage);
            return RESULT_ERROR_ABOART;
        } finally {
            // 3) rename new created somefile.zip.tmp to somefile.zip
            context = traceMessage("(5b) free resources");

            try {
                if (inputStream != null)
                    inputStream.close();
                if (zipInputStream != null)
                    zipInputStream.close();
                if (out != null)
                    out.close();
            } catch (IOException e) {
                logger.info("Error in " + context, e);
            }
        }
        return itemCount;
    }

    /** footer added to text collector. null means no text. */
    protected String getTextFooter() {
        return null;
    }

    /** get stream to be prepended before zip-content or null if there is nothing to prepend. */
    private static InputStream getPrependInputStream(ZipEntry zipEntry, TextCompressItem compressTextItem) throws IOException {
        if ((compressTextItem == null) ||
                (!zipEntry.getName().equalsIgnoreCase(compressTextItem.getZipEntryFileName()))) {
            // not matching current zip: do not prepend.
            return null;
        }

        long newLastModified = compressTextItem.getLastModified();

        compressTextItem.setProcessed(true); // do not add later
        zipEntry.setTime(newLastModified);

        // "------ date ----" between new content and old content
        compressTextItem.addText(getTextDelimiter(newLastModified, zipEntry.getTime()));

        InputStream prependInputStream = compressTextItem.getFileInputStream();

        return prependInputStream;
    }

    /**
     * @return append text delimiter if last text file write had different date than current. Else null.
     */
    private static String getTextDelimiter(long newLastModified, long oldLastModified) {
        DateFormat f = new SimpleDateFormat(" -------- yyyy-MM-dd --------", Locale.US);
        String currentDelimiter = f.format(new java.util.Date(newLastModified));
        String previousDelimiter = f.format(new java.util.Date(oldLastModified));
        if (!currentDelimiter.equalsIgnoreCase(previousDelimiter)) {
            return previousDelimiter;
        }

        return null;
    }

    /**
     * local helper to generate a ZipEntry.
     */
    private ZipEntry createZipEntry(String renamedFile, long time,
                                    String comment) {
        ZipEntry result = new ZipEntry(renamedFile);
        if (time != 0)
            result.setTime(time);
        if (comment != null)
            result.setComment(comment);

        return result;
    }

    private void thowrError(String message) throws Exception {
        throw new Exception("failed in " + message);
    }

    /**
     * add one item to zip. closing outZipStream when done.
     */
    private void add(ZipOutputStream outZipStream, ZipEntry zipEntry,
                     InputStream prependInputStream, InputStream inputStream) throws IOException {
        outZipStream.putNextEntry(zipEntry);

        if (prependInputStream != null) {
            copyStream(outZipStream, prependInputStream, buffer);
            prependInputStream.close();
        }
        copyStream(outZipStream, inputStream, buffer);
        outZipStream.closeEntry();
    }

    /**
     * return number of remaining itemes that should be added to zip
     */
    public int getAddCount() {
        return this.compressQue.size();
    }

    @Override
    public String traceMessage(String format, Object... params) {
        if (zipLog != null) return zipLog.traceMessage(format, params);
        return format;
    }

    @Override
    public void addError(String errorMessage) {
        if (zipLog != null) zipLog.addError(errorMessage);
    }

    @Override
    public String getLastError(boolean detailed) {
        if (zipLog != null) return zipLog.getLastError(detailed);
        return "";
    }
}
