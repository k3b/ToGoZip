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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * unit-tests using mocked Zip*-api
 * Created by k3b on 02.11.2014.
 */
public class CompressJobTests {
    private ZipEntry zeA;
    private ZipEntry zeA1;

    // simulated content of zipFile: map from path to lastModifiedDate used to find the duplicates
    Map<String, Long> existingZipFileEntries;

    @Before
    public void setup() {
        this.zeA = new ZipEntry("a.txt");
        this.zeA.setTime(100000);
        this.zeA1 = new ZipEntry("a(1).txt");
        this.zeA1.setTime(200000);

        this.existingZipFileEntries = new HashMap<>();
        this.existingZipFileEntries.put(this.zeA.getName(), this.zeA.getTime());
        this.existingZipFileEntries.put(this.zeA1.getName(), this.zeA1.getTime());
    }

    private CompressJob createCompressJob(ZipStorage testZip) {
        return new CompressJob(null, null).setZipStorage(testZip);
    }

    private File createMockFile(String name, long date) {
        File mockedFile = mock(File.class);
        when(mockedFile.getName()).thenReturn(name);
        when(mockedFile.lastModified()).thenReturn(date);
        when(mockedFile.isFile()).thenReturn(true);
        return mockedFile;
    }

    @Test
    public void shouldAddAsList() {
        CompressJob sut = createCompressJob(null);
        int addCount = sut.addToCompressQueue(new CompressItem[] {
                new FileCompressItem("", new File("a.txt"), null)});

        Assert.assertEquals(1, addCount);
    }

    @Test
    public void shouldRenameExisting() {
        CompressJob sut = createCompressJob(null);
        File mockedFile = createMockFile("a.txt", 300000l);

        sut.addItemToCompressQue("", mockedFile, null);

        List<CompressItem> result = sut.handleDuplicates(this.existingZipFileEntries);

        Assert.assertEquals("a(2).txt", result.get(0).getZipEntryFileName());
    }

    @Test
    public void shouldNotAddDuplicate() {

        CompressJob sut = createCompressJob(null);
        sut.addItemToCompressQue("", new File("a.txt"), null);
        CompressItem result = sut.addItemToCompressQue("", new File("a.txt"), null);

        Assert.assertEquals(null, result);
    }

    @Test
    public void shouldNotRenameNew() {

        CompressJob sut = createCompressJob(null);
        sut.addToCompressQue("", "b.txt");

        List<CompressItem> result = sut.handleDuplicates(this.existingZipFileEntries);

        Assert.assertEquals(null, result);
    }

    @Test
    public void shouldIgnoreSame() {

        CompressJob sut = createCompressJob(null);
        CompressItem newItem = sut.addToCompressQue("", createMockFile("a.txt", 300000l));

        String result = sut.getRenamedZipEntryFileName(existingZipFileEntries, newItem, this.zeA1.getTime());

        Assert.assertEquals(null, result);
    }
}
