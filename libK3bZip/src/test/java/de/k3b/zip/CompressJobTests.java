package de.k3b.zip;

import static org.mockito.Mockito.*;
import org.junit.*;
// import org.mockito.Mock;

import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * unit-tests using mocked Zip*-api
 * Created by k3b on 02.11.2014.
 */
public class CompressJobTests {

    private ZipFile zipFile = null;
    private ZipEntry zeA;
    private ZipEntry zeA1;

    @Before
    public void setup() {
        zeA = new ZipEntry("a.txt");zeA.setTime(10000);
        zeA1 = new ZipEntry("a(1).txt");zeA1.setTime(20000);
        zipFile = mock(ZipFile.class);
        when(zipFile.getEntry(eq("a.txt"))).thenReturn(zeA);
        when(zipFile.getEntry(eq("a(1).txt"))).thenReturn(zeA1);
    }

    @Test
    public void shouldRenameExisting() {

        CompressJob sut = new CompressJob(null);
        sut.add("", "a.txt");

        List<CompressItem> result = sut.handleDuplicates(zipFile);

        Assert.assertEquals("a(2).txt", result.get(0).getZipFileName());
    }

    @Test
    public void shouldNotRenameNew() {

        CompressJob sut = new CompressJob(null);
        sut.add("", "b.txt");

        List<CompressItem> result = sut.handleDuplicates(zipFile);

        Assert.assertEquals(null, result);
    }

    @Test
    public void shouldIgnoreSame() {

        CompressJob sut = new CompressJob(null);
        sut.add("", "a.txt");

        String result = sut.getFixedZipFileName(zipFile, zeA1, zeA1.getTime());

        Assert.assertEquals(null, result);
    }


    @Test
    public void shouldRenameIfDifferentDate() {

        CompressJob sut = new CompressJob(null);

        String result = sut.getFixedZipFileName(zipFile, zeA, zeA.getTime()+9900);

        Assert.assertEquals("a(2).txt", result);
    }

}
