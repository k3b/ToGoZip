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

/**
 * Steps that happen in a {@link CompressJob}
 *
 * Small footprint replacement for java enum
 * because enums in android should be avoided (even if compressed)
 */
public final class ZipJobState {
    public static final int UNKNOWN = -1;

    public static final int PARSER_ADD_TEXT = 1;
    public static final int PARSER_ADD_CLIP_ITEM = 2;
    public static final int PARSER_ADD_FILE = 3;
    public static final int PARSER_ADD_URI = 4;
    public static final int PARSER_ERROR = 1001;

    public static final int RENAME_COMMENT = 10;

    public static final int CREATE_NEW_ZIP_0 = 101;
    public static final int COPY_EXISTING_ITEM_1A = 110;
    public static final int ADD_TEXT_TO_EXISTING_1A1 = 111;
    public static final int READ_OLD_EXISTING_LOG_ITEM_1A2 = 112;
    public static final int COPY_NEW_ITEM_1B = 120;
    public static final int COPY_LOG_ITEM_1C = 130;
    public static final int RENAME_OLD_ZIP_2 = 200;
    public static final int RENAME_NEW_CREATED_ZIP_3 = 300;
    public static final int DELETE_EXISTING_RENAMED_ZIP_4 = 400;
    public static final int SUCCESSFULL_UPDATED_ZIP_5A = 501;
    public static final int FREE_RESOURCES_5B = 502;

    public static final int ERROR = 1000;
    public static final int CANCELED = 1100;

}
