/*
 * Copyright (c) 2015-2018 by k3b.
 *
 * This file is part of AndroFotoFinder / #APhotoManager.
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
 
package de.k3b.io;

import java.io.Closeable;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper functions for java.io.File.
 * Changes here should also be added to https://github.com/k3b/APhotoManager/
 *
 * Created by k3b on 06.10.2015.
 */
public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger("FileUtils");
    private static final String DBG_CONTEXT = "FileUtils:";

    public static void close(Closeable stream, Object source) {
		if (stream != null) {
			try {			
				stream.close();
			} catch (IOException e) {
                logger.warn(DBG_CONTEXT + "Error close " + source, e);
			}
		}
	}
		
    /** find cildren by regular expression */
    public static File[] listFiles(File parent, final Pattern fileOrDirThatMustBeInTheRoot) {
        return parent.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File owner, String fileName) {
                final boolean found = fileOrDirThatMustBeInTheRoot.matcher(fileName).matches();
                return found;
            }
        });
    }

    // #118 app specific content uri convert
    // from {content://approvider}//storage/emulated/0/DCIM/... to /storage/emulated/0/DCIM/
    public static String fixPath(String path) {
        if (path != null) {
            while (path.startsWith("//")) {
                path = path.substring(1);
            }
        }
        return path;
    }

    /** tryGetCanonicalFile without exception */
    public static File tryGetCanonicalFile(String path) {
        if (path == null) return null;

        final File file = new File(path);
        return tryGetCanonicalFile(file, file);
    }

    /** tryGetCanonicalFile without exception */
    public static File tryGetCanonicalFile(File file, File errorValue) {
        if (file == null) return null;

        try {
            return file.getCanonicalFile();
        } catch (IOException ex) {
            logger.warn(DBG_CONTEXT + "Error tryGetCanonicalFile('" + file.getAbsolutePath() + "') => '" + errorValue + "' exception " + ex.getMessage(), ex);
            return errorValue;
        }
    }

    /** tryGetCanonicalFile without exception */
    public static File tryGetCanonicalFile(File file) {
        return tryGetCanonicalFile(file, file);
    }

    /** tryGetCanonicalFile without exception */
    public static String tryGetCanonicalPath(File file, String errorValue) {
        if (file == null) return null;

        try {
            return file.getCanonicalPath();
        } catch (IOException ex) {
            logger.warn(DBG_CONTEXT + "Error tryGetCanonicalPath('" + file.getAbsolutePath() + "') => '" + errorValue + "' exception " + ex.getMessage(), ex);
            return errorValue;
        }
    }
}
