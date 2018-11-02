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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
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
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import de.k3b.io.StringUtils;

/**
 * Android independant enginge that adds files to a zip file via a compressQue.
 */
public class CompressJob implements ZipLog {
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

    /** this item from the que will receive all short texts */
    private TextCompressItem compressTextItem = null;

    /** this item from the que will receive all log texts */
    private TextCompressItem compressLogItem = null;

    private ZipStorage destZipFile;

    // used to copy content
    private byte[] buffer = new byte[4096];

    /**
     * Creates a job.
     *
     * @param zipLog  if not null use this for logging.
     * @param fileLogInZip
     */
    public CompressJob(ZipLog zipLog, String fileLogInZip) {
        this.zipLog = zipLog;
        if (!StringUtils.isNullOrEmpty(fileLogInZip)) {
            this.compressLogItem = addLog2CompressQue(fileLogInZip, null);

            // do not process this item in qoueOutPutLoop
            this.compressLogItem.setProcessed(true);
        }
    }

    public static String readAll(InputStream is, byte[] buffer) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        copyStream(byteArrayOutputStream, is, buffer);
        String result = new String(byteArrayOutputStream.toByteArray());
        byteArrayOutputStream.flush();
        byteArrayOutputStream.close();
        return result;
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
                    item = addItemToCompressQue(destZipPath, srcFile, null);
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
    CompressItem addItemToCompressQue(String destZipPath, File srcFile, String zipEntryComment) {
        CompressItem item = new FileCompressItem(destZipPath, srcFile, zipEntryComment);
        if (addToCompressQueue(item)) {
            return item;
        }
        return null;
    }

    public void addToCompressQueue(CompressItem[] items) {
        if (items != null) {
            for (CompressItem item : items) {
                addToCompressQueue(item);
            }
        }
    }

    public TextCompressItem addTextToCompressQue(String textfile, String textToBeAdded) {
        this.compressTextItem = addTextToCompressQue(this.compressTextItem, textfile, textToBeAdded);
        return this.compressTextItem;
    }

    public TextCompressItem addLog2CompressQue(String textfile, String textToBeAdded) {
        this.compressLogItem = addTextToCompressQue(this.compressLogItem, textfile, textToBeAdded);
        return this.compressLogItem;
    }

    private TextCompressItem addTextToCompressQue(TextCompressItem textItem, String zipEntryPath,
                                                  String textToBeAdded) {
        if (textItem == null) {

            textItem = new TextCompressItem(zipEntryPath, null);
            textItem.setLastModified(new Date().getTime());
            addToCompressQueue(textItem);
        }
        if ((textToBeAdded!=null) && (textToBeAdded.length() > 0)) {
            textItem.addText(textToBeAdded);
        }
        return textItem;
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
     * a unittest friendly version of handleDuplicates:<br/>
     * depending on global options: duplicate zip entries are either ignored or renamed
     *
     * @param existingZipEntries
     * @return For unittests-only: collection of items that where renamed or null if no renaming
     *         took place.
     */
    List<CompressItem> handleDuplicates(Map<String, Long> existingZipEntries) {

        // used by unittest to find out if renaming duplicates works as expected.
        List<CompressItem> renamedItems = new ArrayList<CompressItem>();
        for (CompressItem itemToAdd : this.compressQue) {
            if (!itemToAdd.isProcessed()) {
                String addFileName = (itemToAdd != null) ? itemToAdd.getZipEntryFileName() : null;
                Long zipEntryFileLastModified = (addFileName != null) ? existingZipEntries.get(addFileName) : null;

                if (zipEntryFileLastModified != null) {
                    // Threre is already an entry with the same name as the new item

                    // null means means do not add this
                    // else new entry gets a new name
                    final String newEntryName = getRenamedZipEntryFileName(existingZipEntries, itemToAdd,
                            zipEntryFileLastModified);
                    itemToAdd.setZipEntryFileName(newEntryName);
                    // itemToAdd.setZipEntryComment(createItemComment());
                    renamedItems.add(itemToAdd);
                    addFileName = newEntryName;
                }

                if (addFileName != null) {
                    // add new item to existing so that a second instance with the same filename will be renamed
                    existingZipEntries.put(addFileName, itemToAdd.getLastModified());
                }
            }
        }

        // remove the ithems that are marked with null filename
        for (int i = this.compressQue.size() - 1; i >= 0; i--) {
            if (getCompressItemAt(i).getZipEntryFileName() == null) {
                this.compressQue.remove(i);
            }
        }

        if (renamedItems.size() > 0) {
            return renamedItems;
        }
        return null;
    }

    /** scope package to allow unittests */
    CompressItem getCompressItemAt(int i) {
        return this.compressQue.get(i);
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
     * @param renameDuplicateTextFile true: add an additional renamed entry for the texts.
     *                                 False: append to existing entry
     * @return number of modified items (compressQue and/or text appended)
     */
    public int compress(boolean renameDuplicateTextFile) {
        // Workflow (that makes shure that orginal is not broken if there is an error):
        // 1) addToCompressQue to somefile.tmp.zip, (a) old content from somefile.zip, (b) new content
        // 2) rename existing somefile.zip to somefile.bak.zip
        // 3) rename somefile.tmp.zip to somefile.zip
        // 4) delete existing somefile.bak.zip
        // 5) free resources

        boolean preventTextFromRenaming = (!renameDuplicateTextFile) && (this.compressTextItem != null) && !this.compressTextItem.isProcessed();

        int emptyCount = (compressLogItem != null) ? 1 : 0;
        if (compressQue.size() <= emptyCount) {
            logger.debug("aboard: no (more) files to addToCompressQue to zip");
            return RESULT_NO_CHANGES;
        }

        // global to allow garbage collection if there is an exception
        ZipOutputStream zipOutputStream = null;
        ZipInputStream zipInputStream = null;
        InputStream zipEntryInputStream = null;
        String context = "";
        int itemCount = 0;

        try {
            // map from path to lastModifiedDate used to find the duplicates
            Map<String, Long> existingEntries = new HashMap<>();

            String curZipFileName = this.destZipFile.getName(ZipStorage.ZipInstance.current);
            String newZipFileName = this.destZipFile.getName(ZipStorage.ZipInstance.new_);

            context = traceMessage("(0) create new result file {0}", newZipFileName);
            zipOutputStream = new ZipOutputStream(this.destZipFile.createOutputStream(ZipStorage.ZipInstance.new_));

            String oldZipFileName = null;
            if (this.destZipFile.exists()) {
                context = traceMessage("(1a) copy existing compressQue from {0} to {1}",
                        curZipFileName, newZipFileName);
                zipInputStream = new ZipInputStream(this.destZipFile.createInputStream());

                InputStream prependInputStream = null;
                for (ZipEntry zipOldEntry = zipInputStream.getNextEntry(); zipOldEntry != null; zipOldEntry = zipInputStream
                        .getNextEntry()) {
                    if (null != zipOldEntry) {
                        if (null != (prependInputStream = this.getPrependInputStream(zipOldEntry, this.compressTextItem))) {
                            itemCount++;
                            context = traceMessage(
                                    "- (1a+) add text to existing item from {0} to {1} : {2}",
                                    curZipFileName, newZipFileName, zipOldEntry);
                        } else if (isSameFile(this.compressLogItem, zipOldEntry)) {
                            context = traceMessage(
                                    "- (1a+) read old log text from {0} to {1} : {2}",
                                    curZipFileName, newZipFileName, zipOldEntry);

                            this.compressLogItem.addText(readAll(zipInputStream, this.buffer ));
                            this.compressLogItem.setZipEntryComment(zipOldEntry.getComment());

                            // add latter when all log entries are written
                            zipOldEntry = null;
                        } else {
                            context = traceMessage(
                                    "- (1a) copy existing item from {0} to {1} : {2}",
                                    curZipFileName, newZipFileName, zipOldEntry);
                        }
                    }

                    if (null != zipOldEntry) {
                        add(zipOutputStream, zipOldEntry, prependInputStream, zipInputStream);
                        existingEntries.put(zipOldEntry.getName(), zipOldEntry.getTime());
                    }
                }
                zipInputStream.close();
                zipInputStream = null;
                // i.e. /path/to/somefile.bak.zip
                oldZipFileName = this.destZipFile.getName(ZipStorage.ZipInstance.old);
            }

            boolean oldProcessed = false;
			if (preventTextFromRenaming) {
                oldProcessed = this.compressTextItem.isProcessed();
                this.compressTextItem.setProcessed(true);
            }
			handleDuplicates(existingEntries);
			if (preventTextFromRenaming) this.compressTextItem.setProcessed(oldProcessed);

            if ((this.compressTextItem != null) && !this.compressTextItem.isProcessed()) {
                this.compressTextItem.addText(this.getTextFooter());
                // this.compressTextItem.setProcessed(true);
                // itemCount++;
            }

            if (this.compressLogItem != null) {
                this.compressLogItem.addText(";;;;;---");
            }

            // (1b) copy new compressQue
            for (CompressItem item : this.compressQue) {
                if (!item.isProcessed()) {
                    String newFullDestZipItemName = item.getZipEntryFileName();
                    context = traceMessage("(1b) copy new item {0} as {1} to {2}",
                            item, newFullDestZipItemName, newZipFileName);
                    zipEntryInputStream = item.getFileInputStream();
                    ZipEntry zipEntry = createZipEntry(newFullDestZipItemName,
                            item.getLastModified(), item.getZipEntryComment());
                    add(zipOutputStream, zipEntry, null, zipEntryInputStream);
                    zipEntryInputStream.close();
                    zipEntryInputStream = null;
                    itemCount++;
                    item.setProcessed(true);

                    if (this.compressLogItem != null) {
                        this.compressLogItem.addText(item.getLogEntry(null).toString());
                    }
                }
            }

            if (compressLogItem != null) {
                CompressItem item = compressLogItem;
                String newFullDestZipItemName = item.getZipEntryFileName();
                context = traceMessage("(1b) copy new item {0} as {1} to {2}",
                        item, newFullDestZipItemName, newZipFileName);
                zipEntryInputStream = item.getFileInputStream();
                ZipEntry zipEntry = createZipEntry(newFullDestZipItemName,
                        item.getLastModified(), item.getZipEntryComment());
                add(zipOutputStream, zipEntry, null, zipEntryInputStream);
                zipEntryInputStream.close();
                zipEntryInputStream = null;
                itemCount++;
            }

            zipOutputStream.close();
            zipOutputStream = null;

            // no exception yet: Assume it is save to change the old zip
            // (2) rename existing-old somefile.zip to somefile.bak.zip
            if (oldZipFileName != null) {
                this.destZipFile.delete(ZipStorage.ZipInstance.old);

                context = traceMessage(
                        "(2) rename old zip file from {0}  to {1}",
                        curZipFileName, oldZipFileName);
                // i.e. /path/to/somefile.zip => /path/to/somefile.bak.zip

                if (!destZipFile.rename(ZipStorage.ZipInstance.current, ZipStorage.ZipInstance.old)) {
                    thowrError(context);
                }
            }

            // 3) rename new created somefile.tmp.zip to somefile.zip
            context = traceMessage("(3) rename new created zip file {0} to {1}",
                    newZipFileName, curZipFileName);

            if (!destZipFile.rename(ZipStorage.ZipInstance.new_, ZipStorage.ZipInstance.current)) {
                // something went wrong. try to restore old zip
                // i.e. somefile.bak.zip => somefile.zip
                if (oldZipFileName != null) {
                    destZipFile.rename(ZipStorage.ZipInstance.old, ZipStorage.ZipInstance.current);
                }

                thowrError(context);
            }

            // 4) delete existing renamed old somefile.bak.zip
            if ((optDeleteBakFileWhenFinished) && (oldZipFileName != null)) {
                context = traceMessage(
                        "(4) delete existing renamed old zip file {0}", oldZipFileName);
                this.destZipFile.delete(ZipStorage.ZipInstance.old);
            }
            context = traceMessage("(5a) successfull updated zip file {0}",
                    curZipFileName);

        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (!errorMessage.contains(context)) {
                errorMessage = "Error in " + context + ":" + errorMessage;
            }
            logger.error(errorMessage, e);
            addError(errorMessage);
            return RESULT_ERROR_ABOART;
        } finally {
            context = traceMessage("(5b) free resources");

            try {
                if (zipEntryInputStream != null)
                    zipEntryInputStream.close();
                if (zipInputStream != null)
                    zipInputStream.close();
                if (zipOutputStream != null)
                    zipOutputStream.close();
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
    private static InputStream getPrependInputStream(ZipEntry zipEntry,
                                     TextCompressItem textItem) throws IOException {
        if ((textItem == null) ||
                (!isSameFile(textItem, zipEntry))) {
            // not matching current zip: do not prepend.
            return null;
        }

        long newLastModified = textItem.getLastModified();

        textItem.setProcessed(true); // do not add later
        zipEntry.setTime(newLastModified);

        // "------ date ----" between new content and old content
        textItem.addText(getTextDelimiter(newLastModified, zipEntry.getTime()));

        InputStream prependInputStream = textItem.getFileInputStream();

        return prependInputStream;
    }

    private static boolean isSameFile(CompressItem compressItem, ZipEntry zipEntry) {
        if ((compressItem == null) || (zipEntry == null)) return false;
        return zipEntry.getName().equalsIgnoreCase(compressItem.getZipEntryFileName());
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

    public CompressJob setDestZipFile(ZipStorage destZipFile) {
        this.destZipFile = destZipFile;
        return this;
    }

    public String getAbsolutePath() {return this.destZipFile.getAbsolutePath();}
}
