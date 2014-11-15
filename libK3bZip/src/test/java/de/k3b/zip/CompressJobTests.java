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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        zeA = new ZipEntry("a.txt");
        zeA.setTime(10000);
        zeA1 = new ZipEntry("a(1).txt");
        zeA1.setTime(20000);
        zipFile = mock(ZipFile.class);
        when(zipFile.getEntry(eq("a.txt"))).thenReturn(zeA);
        when(zipFile.getEntry(eq("a(1).txt"))).thenReturn(zeA1);
    }

    @Test
    public void shouldRenameExisting() {

        CompressJob sut = new CompressJob(null, false);
        sut.addItem("", new File("a.txt"));

        List<CompressItem> result = sut.handleDuplicates(zipFile);

        Assert.assertEquals("a(2).txt", result.get(0).getZipFileName());
    }

    @Test
    public void shouldNotAddDuplicate() {

        CompressJob sut = new CompressJob(null, false);
        sut.addItem("", new File("a.txt"));
        CompressItem result = sut.addItem("", new File("a.txt"));

        Assert.assertEquals(null, result);
    }

    @Test
    public void shouldNotRenameNew() {

        CompressJob sut = new CompressJob(null, false);
        sut.add("", "b.txt");

        List<CompressItem> result = sut.handleDuplicates(zipFile);

        Assert.assertEquals(null, result);
    }

    @Test
    public void shouldIgnoreSame() {

        CompressJob sut = new CompressJob(null, false);
        sut.add("", "a.txt");

        String result = sut.getFixedZipFileName(zipFile, zeA1, zeA1.getTime());

        Assert.assertEquals(null, result);
    }


    @Test
    public void shouldRenameIfDifferentDate() {

        CompressJob sut = new CompressJob(null, false);

        String result = sut.getFixedZipFileName(zipFile, zeA, zeA.getTime() + 99900);

        Assert.assertEquals("a(2).txt", result);
    }

}
