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
package de.k3b.zip.example;

import java.io.File;

import de.k3b.zip.CompressItem;
import de.k3b.zip.CompressJob;
import de.k3b.zip.FileCompressItem;
import de.k3b.zip.ZipStorage;
import de.k3b.zip.ZipStorageFile;

/**
 * A Minimal program to demonstrate how to add files to an existing zip file.
 */
public class HelloZip {
    void addSomeItemsToExistingZip() {
        ZipStorage testZip = new ZipStorageFile("/tmp/hello.zip");

        /**
        // under android with SAF use ZipStorageDocumentFile instead of ZipStorageFile. i.e.
        DocumentFile docDir = android.support.v4.provider.DocumentFile.fromTreeUri(getActivity(), uriToAndroidDirectory)
        ZipStorage testZip = new de.k3b.android.zip.ZipStorageDocumentFile(getActivity(), docDir, "hello.zip");
         */

        /**
        // This code adds files to existing zip.
        // to replace existing old zip delete it before. i.e.
        testZip.delete(ZipStorage.ZipInstance.current);
        */

        CompressJob job = new CompressJob(null, null).setZipStorage(testZip);

        // since adding files to zip means time consuming copy zip data from existing zip file
        // to a new generated zip file
        // we first create a list of changes as a job
        job.addToCompressQue(null, new File("/tmp/myImage.jpg"), new File("/tmp/myDocument.txt"));

        job.addTextToCompressQue("path/in/zip/fox.txt",
                "The quick brown fox jumpes over the lazy dogg`s back 1234567890 times");

        // alternative way to add
        final CompressItem itemToBeAdded = new FileCompressItem("path/in/zip",
                new File("/tmp/hello.txt"),
                "This is the descritpion of path/in/zip/hello.txt that was loaded from /tmp/hello.txt");
        job.addToCompressQueue(itemToBeAdded);

        /*
        // you can also add from android specific content-uri-s. i.e.

        itemToBeAdded = new de.k3b.android.zip.AndroidUriCompressItem(getActivity(),
                "content://com.mediatek.calendarimporter/1282",
                "text/ics",
                "Android Calendar Entry #1282 exported as path/in/zip/calendar-1282.ics");

        // optiononal if you onmit this path and filename in zip will be generated from analysing uri.
        itemToBeAdded.setZipEntryFileName("path/in/zip/calendar-1282.ics");

        job.addToCompressQueue(itemToBeAdded);
        */

        // this will create a new zip file with all entries from the existing old zip file plus
        // three new entires.
        job.compress(true);
    }
}
