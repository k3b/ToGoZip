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

    private CompressJob sut;

    @BeforeClass
    static public void createTestData() throws IOException, ParseException {
        root.mkdirs();
        createTestFile(testContent, format.parse("1980-12-24_123456-123"));
        createTestFile(testContent2, new Date());
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
    public void shouldNotAddDouplicate()
    {
        CompressJob sut = new CompressJob(testZip);
        sut.add("", testContent.getAbsolutePath());
        int itemCount = sut.compress();
        Assert.assertEquals(-1,itemCount);
    }

    @Test
    public void shouldAppendDifferentFile()
    {
        CompressJob sut = new CompressJob(testZip);
        sut.add("", testContent2.getAbsolutePath());
        int itemCount = sut.compress();
        Assert.assertEquals(2,itemCount);
    }

    @Test
    public void shouldRenameSameFileNameWithDifferentDate()
    {
        CompressJob sut = new CompressJob(testZip);
        CompressItem item = sut.addItem("", testContent2);
        item.setZipFileName(testContent.getName());
        int itemCount = sut.compress();
        Assert.assertEquals(2,itemCount);
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
