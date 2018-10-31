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

}
