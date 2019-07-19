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

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    /** bug #14: Duplicate file detection/renaming does not work correctly for files in zip-subdirectories */
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
    public void shouldCalculateRelPath() {
        File srcFile = new File("/path/to/my/source/file.txt");

        Assert.assertEquals("source/file.txt",
                getCalRelPath("root/", srcFile, "/path/to/my/"));
        Assert.assertEquals("root/file.txt",
                getCalRelPath("root/", srcFile, "/path/to/other/"));
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

    private static String getCalRelPath(String notFoundRelPath, File srcFile, String refPath) {
        String result = FileCompressItem.calculateZipEntryName(notFoundRelPath, srcFile,
                FileCompressItem.getCanonicalPath(new File(refPath)));

        // fix windows path seperator
        return fixPathDelimiter(result);
    }

    private static String fixPathDelimiter(String result) {
        return result.replaceAll("\\\\","/");
    }

    private static void createTestFile(File testContent, Date fileDate) throws IOException {
        OutputStream testContentFile = new FileOutputStream(testContent);

        final String someContent = "some test data";
        InputStream someContentStream = new ByteArrayInputStream(someContent.getBytes("UTF-8"));

        CompressJob.copyStream(
                testContentFile,
                someContentStream, new byte[1024]);
        testContentFile.close();
        someContentStream.close();
        testContent.setLastModified(fileDate.getTime());
    }

    private CompressJob createCompressJob(String testName) {
        return createCompressJob(testName, "", testContent);
    }

    private CompressJob createCompressJob(String testName, String destZipPath, File... srcFiles) {
        ZipStorage testZip = new ZipStorageFile(root+ testName + ".zip");
        testZip.delete(ZipStorage.ZipInstance.current);

        CompressJob initialContent = createCompressJob(testZip);
        initialContent.addToCompressQue(destZipPath, srcFiles);
        int itemCount = initialContent.compress(false);
        Assert.assertEquals("exampleItem + log == 2",
                srcFiles.length + NUMBER_OF_LOG_ENTRIES, itemCount);

        return createCompressJob(testZip);
    }

    private CompressJob createCompressJob(ZipStorage testZip) {
        return new CompressJob(null, "changeHistory.txt").setZipStorage(testZip);
    }

}
