/*
 * Copyright (C) 2014-2019 k3b
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

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.k3b.io.FileUtils;

/**
 * Integration-Tests using real zip files in the temp-folder<br/>
 * <br/>
 * Created by k3b on 03.11.2014.
 */
public class CompressJobIntegrationTests {
    private static final int NUMBER_OF_LOG_ENTRIES = 1;
    static private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HHmmss-S");
    // static private File root = new File(System.getProperty("java.io.tmpdir")
    static private String root = FileUtils.fixPath(System.getProperty("java.io.tmpdir")
            + "/k3bZipTests/CompressJobIntegrationTests/");
    static private String rootInput = root + "inputFiles/";
    static private File testContent = new File(rootInput + "testFile.txt");
    static private File testContent2 = new File(rootInput + "testFile2.txt");
    static private File testDirWith2SubItems = new File(rootInput + "dir");
    static private File testContent3 = new File(testDirWith2SubItems, "testFile3.txt");
    static private File testContent4 = new File(testDirWith2SubItems, "testFile4.txt");

    @BeforeClass
    static public void createTestData() throws IOException, ParseException {
        // root.mkdirs();
        testDirWith2SubItems.mkdirs();
        createTestFile(testContent, format.parse("1980-12-24_123456-123"));
        createTestFile(testContent2, new Date());
        createTestFile(testContent3, format.parse("1981-12-24_123456-123"));
        createTestFile(testContent4, format.parse("1982-12-24_123456-123"));

        System.out.println("CompressJobIntegrationTests: files in " + root);
    }

    @Test
    public void shouldPrependShortText() throws IOException {
        final String textToBeAdded = "new added text should be before old";

        // arrange
        ZipStorage testZip = createZipStorage("shouldPrependShortText");

        CompressJob initialContent = createCompressJob(testZip, null);
        initialContent.addToCompressQue("", testContent);
        initialContent.compress(false);

        // act
        CompressJob sut = createCompressJob(testZip, null);
        sut.addTextToCompressQue(testContent.getName(), textToBeAdded);
        sut.compress(false);

        // assert
        Uncompressor uncompress = new Uncompressor(testZip);
        String content = uncompress.getContentOfZipEntryAsText(testContent.getName());

        Assert.assertTrue("new text prepended", content.startsWith(textToBeAdded));
        Assert.assertTrue("read text longer than new text", content.length() > textToBeAdded.length() + 10);
    }

    private static void createTestFile(File testContent, Date fileDate) throws IOException {
        OutputStream testContentFile = new FileOutputStream(testContent);

        final String someContent = "some test data from '" + testContent + "' with date " + fileDate;
        InputStream someContentStream = new ByteArrayInputStream(someContent.getBytes(StandardCharsets.UTF_8));

        byte[] buffer = new byte[1024];
        FileUtils.copyStream(testContentFile, someContentStream, buffer);
        testContentFile.close();
        someContentStream.close();
        testContent.setLastModified(fileDate.getTime());
    }

    @Test
    public void shouldAppendWithLogging() {
        // arrange
        ZipStorage testZip = createZipStorage("shouldAppendWithLogging");

        CompressJob initialContent = createCompressJob(testZip, null);
        initialContent.addToCompressQue("", testContent);
        initialContent.compress(false);

        // act
        ZipLog log = new ZipLogImpl(true);
        CompressJob sut = createCompressJob(testZip, log);
        sut.addTextToCompressQue("b.txt", "text for a newly created file");
        sut.compress(false);

        // assert
        String content = log.getLastError(true);

        assertContains(content, testContent.getName(), "b.txt");
    }

    @Test
    public void shouldAsyncCanclelExisting() {
        // arrange
        ZipStorage testZip = createZipStorage("shouldAsyncCanclelExisting");

        CompressJob initialContent = createCompressJob(testZip, null);
        initialContent.addToCompressQue("", testContent);
        initialContent.compress(false);

        // act
        final CancelingZipLog log = new CancelingZipLog();
        CompressJob sut = createCompressJob(testZip, log);
        log.sut = sut;

        sut.addTextToCompressQue("b.txt", "text for a newly created file");
        sut.compress(false);

        // assert
        String content = log.getLastError(true);

        assertContains(content, "[cancel");
    }

    @Test
    public void shouldNotAddDuplicate() {
        CompressJob sut = createCompressJob("shouldNotAddDuplicate");
        sut.addToCompressQue("", testContent.getAbsolutePath());
        int itemCount = sut.compress(false) - NUMBER_OF_LOG_ENTRIES;
        Assert.assertEquals(CompressJob.RESULT_NO_CHANGES, itemCount);
    }


    @Test
    public void shouldAddDuplicateInDifferentDirs() {
        CompressJob sut = createCompressJob("shouldAddDuplicateInDifferentDirs", "", testContent);

        // same name as existing
        CompressItem item = sut.addToCompressQue("otherDir/", testContent);
        int itemCount = sut.compress(false) - NUMBER_OF_LOG_ENTRIES;
        Assert.assertEquals(1, itemCount);

        Assert.assertEquals("otherDir/testFile.txt", fixPathDelimiter(item.getZipEntryFileName()));
    }

    @Test
    public void shouldAddDuplicateInDifferentDirs2() {
        CompressJob sut = createCompressJob("shouldAddDuplicateInDifferentDirs2", "otherDir/", testContent);

        // same name as existing
        CompressItem item = sut.addToCompressQue("", testContent);
        int itemCount = sut.compress(false) - NUMBER_OF_LOG_ENTRIES;
        Assert.assertEquals(1, itemCount);

        Assert.assertEquals("testFile.txt", fixPathDelimiter(item.getZipEntryFileName()));
    }

    @Test
    public void shouldAppendDifferentFile() {
        CompressJob sut = createCompressJob("shouldAppendDifferentFile");
        sut.addToCompressQue("", testContent2.getAbsolutePath());
        int itemCount = sut.compress(false) - NUMBER_OF_LOG_ENTRIES;
        Assert.assertEquals(1, itemCount);
    }

    @Test
    public void shouldAppendDir() {
        CompressJob sut = createCompressJob("shouldAppendDir");
        sut.addToCompressQue("", testDirWith2SubItems);
        int itemCount = sut.compress(false) - NUMBER_OF_LOG_ENTRIES;
        Assert.assertEquals(2, itemCount);
    }

    @Test
    public void shouldRenameSameFileNameWithDifferentDate() {
        CompressJob sut = createCompressJob("shouldRenameSameFileNameWithDifferentDate");
        CompressItem item = sut.addToCompressQue("", testContent2);
        item.setZipEntryFileName(testContent.getName());
        int itemCount = sut.compress(false) - NUMBER_OF_LOG_ENTRIES;
        Assert.assertEquals(1, itemCount);
        Assert.assertEquals("testFile(1).txt", item.getZipEntryFileName());
    }

    /** bugfix for #14: Duplicate file detection/renaming does not work correctly for files in zip-subdirectories */
    @Test
    public void shouldRenameSameFileNameInSubfolderWithDifferentDate_14() {
        String relDir = "testdir/";
        CompressJob sut = createCompressJob(
                "shouldRenameSameFileNameInSubfolderWithDifferentDate",
                relDir, testContent);

        CompressItem item = sut.addToCompressQue(relDir, testContent2);

        // same name as existing
        item.setZipEntryFileName(FileCompressItem.calculateZipEntryName(relDir, testContent, null));

        int itemCount = sut.compress(false) - NUMBER_OF_LOG_ENTRIES;
        Assert.assertEquals(1, itemCount);
        Assert.assertEquals("testdir/testFile(1).txt", fixPathDelimiter(item.getZipEntryFileName()));
    }

    @Test
    public void shouldAppendTextAsFile() {
        CompressJob sut = createCompressJob("shouldAppendTextAsFile");
        sut.addTextToCompressQue("hello.txt", "hello world");
        int itemCount = sut.compress(false) - NUMBER_OF_LOG_ENTRIES;
        Assert.assertEquals(1, itemCount);
    }

    @Test
    public void shouldAppendTextAsFileToExisting() {
        CompressJob sut = createCompressJob("shouldAppendTextAsFileToExisting");
        sut.addTextToCompressQue("hello.txt", "hello world");
        sut.addTextToCompressQue("hello.txt", "once again: hello world");
        int itemCount = sut.compress(false) - NUMBER_OF_LOG_ENTRIES;
        Assert.assertEquals(1, itemCount);
    }

    @Test
    public void shouldAddWithRelPath() {
        CompressJob sut = createCompressJob("shouldAddWithRelPath");

        FileCompressItem.setZipRelPath(new File(root));
        sut.addToCompressQue("ignoredPath/", testContent2, testContent4);
        FileCompressItem.setZipRelPath(null);

        int itemCount = sut.compress(false) - NUMBER_OF_LOG_ENTRIES;
        Assert.assertEquals(2, itemCount);
        Assert.assertEquals(false, sut.getCompressItemAt(1).getZipEntryFileName().contains("ignore"));
    }

    // ----------- local test helper ------------------

    private void assertContains(String content, String... mustBeIncluded) {
        for(String candidate : mustBeIncluded) {
            Assert.assertTrue(candidate + " must be inside " + content, content.contains(candidate));
        }
    }

    private static String fixPathDelimiter(String result) {
        if (result == null) return null;
        return result.replaceAll("\\\\","/");
    }

    /**
     * cancels {@link CompressJob}  when log entry is added
     */
    private static class CancelingZipLog extends ZipLogImpl {
        CompressJob sut = null;

        CancelingZipLog() {
            super(true);
        }

        @Override
        public String traceMessage(int zipStateID, int itemNumber, int itemTotal, String format, Object... params) {
            if (zipStateID == ZipJobState.COPY_LOG_ITEM_1C) sut.cancel();
            return super.traceMessage(zipStateID, itemNumber, itemTotal, format, params);
        }
    }

    private CompressJob createCompressJob(String testName) {
        return createCompressJob(testName, "", testContent);
    }

    private CompressJob createCompressJob(String testName, String destZipPath, File... srcFiles) {
        ZipStorage testZip = createZipStorage(testName);

        CompressJob initialContent = createCompressJob(testZip, null);
        initialContent.addToCompressQue(destZipPath, srcFiles);
        int itemCount = initialContent.compress(false);
        Assert.assertEquals("exampleItem + log == 2",
                srcFiles.length + NUMBER_OF_LOG_ENTRIES, itemCount);

        return createCompressJob(testZip, null);
    }

    private ZipStorage createZipStorage(String testName) {
        ZipStorage testZip = new ZipStorageFile(root+ testName + ".zip");
        testZip.delete(ZipStorage.ZipInstance.current);
        return testZip;
    }

    private CompressJob createCompressJob(ZipStorage testZip, ZipLog zipLog) {
        return new CompressJob(zipLog, "changeHistory.txt").setZipStorage(testZip);
    }

}
