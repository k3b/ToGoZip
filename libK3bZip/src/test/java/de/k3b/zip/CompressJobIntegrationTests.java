/*
 * Copyright (C) 2014 k3b
 * 
 * This file is part of de.k3b.Add2GoZip (https://github.com/k3bAdd2GoZip/) .
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

import org.junit.Before;
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

/**
 * Integration-Tests using real zip files in the temp-folder<br/>
 * <br/>
 * Created by k3b on 03.11.2014.
 */
public class CompressJobIntegrationTests {
    static private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HHmmss-S");
    static private File root = new File(System.getProperty("java.io.tmpdir")
            + "/k3bZipTests/"
//          + format.format(new Date())
    );
    static private File testZip = new File(root, "test.zip");
    static private File testContent = new File(root, "testFile.txt");
    static private File testContent2 = new File(root, "testFile2.txt");
    static private File testDirWith2SubItems = new File(root, "dir");
    static private File testContent3 = new File(testDirWith2SubItems, "testFile3.txt");
    static private File testContent4 = new File(testDirWith2SubItems, "testFile4.txt");

    private CompressJob sut;

    @BeforeClass
    static public void createTestData() throws IOException, ParseException {
        root.mkdirs();
        testDirWith2SubItems.mkdirs();
        createTestFile(testContent, format.parse("1980-12-24_123456-123"));
        createTestFile(testContent2, new Date());
        createTestFile(testContent3, format.parse("1981-12-24_123456-123"));
        createTestFile(testContent4, format.parse("1982-12-24_123456-123"));
    }

    @Before
    public void setup() throws IOException {
        testZip.delete();
        CompressJob sut = new CompressJob(testZip);
        sut.add("", testContent.getAbsolutePath());
        int itemCount = sut.compress();
        Assert.assertEquals(1, itemCount);
    }

    @Test
    public void shouldNotAddDouplicate() {
        CompressJob sut = new CompressJob(testZip);
        sut.add("", testContent.getAbsolutePath());
        int itemCount = sut.compress();
        Assert.assertEquals(CompressJob.RESULT_NO_CHANGES, itemCount);
    }

    @Test
    public void shouldAppendDifferentFile() {
        CompressJob sut = new CompressJob(testZip);
        sut.add("", testContent2.getAbsolutePath());
        int itemCount = sut.compress();
        Assert.assertEquals(2, itemCount);
    }

    @Test
    public void shouldAppendDir() {
        CompressJob sut = new CompressJob(testZip);
        sut.add("", testDirWith2SubItems);
        int itemCount = sut.compress();
        Assert.assertEquals(3, itemCount);
    }

    @Test
    public void shouldRenameSameFileNameWithDifferentDate() {
        CompressJob sut = new CompressJob(testZip);
        CompressItem item = sut.add("", testContent2);
        item.setZipFileName(testContent.getName());
        int itemCount = sut.compress();
        Assert.assertEquals(2, itemCount);
        Assert.assertEquals("testFile(1).txt", item.getZipFileName());
    }

    private static void createTestFile(File testContent, Date fileDate) throws IOException {
        OutputStream testContentFile = new FileOutputStream(testContent);

        final String someContent = "some test data for " +
                testZip;
        InputStream someContentStream = new ByteArrayInputStream(someContent.getBytes("UTF-8"));

        CompressJob.copyStream(
                testContentFile,
                someContentStream, new byte[1024]);
        testContentFile.close();
        someContentStream.close();
        testContent.setLastModified(fileDate.getTime());
    }
}
