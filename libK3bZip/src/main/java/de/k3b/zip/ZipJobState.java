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

import java.io.Serializable;

/**
 * Steps that happen in {@Link CompressJob}
 *
 * Small footprint replacement for java enum
 * because enums in android should be avoided (even if compressed)
 */
public final class ZipJobState implements Comparable<ZipJobState> {
    public static final ZipJobState UNKNOWN = new ZipJobState(-1);

    public static final ZipJobState PARSER_ADD_TEXT = new ZipJobState(1);
    public static final ZipJobState PARSER_ADD_CLIP_ITEM = new ZipJobState(2);
    public static final ZipJobState PARSER_ADD_FILE = new ZipJobState(3);
    public static final ZipJobState PARSER_ADD_URI = new ZipJobState(4);
    public static final ZipJobState PARSER_ERROR = new ZipJobState(1001);

    public static final ZipJobState RENAME_COMMENT = new ZipJobState(10);

    public static final ZipJobState CREATE_NEW_ZIP_0 = new ZipJobState(101);
    public static final ZipJobState COPY_EXISTING_ITEM_1A = new ZipJobState(110);
    public static final ZipJobState ADD_TEXT_TO_EXISTING_1A1 = new ZipJobState(111);
    public static final ZipJobState READ_OLD_EXISTING_LOG_ITEM_1A2 = new ZipJobState(112);
    public static final ZipJobState COPY_NEW_ITEM_1B = new ZipJobState(120);
    public static final ZipJobState COPY_LOG_ITEM_1C = new ZipJobState(130);
    public static final ZipJobState RENAME_OLD_ZIP_2 = new ZipJobState(200);
    public static final ZipJobState RENAME_NEW_CREATED_ZIP_3 = new ZipJobState(300);
    public static final ZipJobState DELETE_EXISTING_RENAMED_ZIP_4 = new ZipJobState(400);
    public static final ZipJobState SUCCESSFULL_UPDATED_ZIP_5A = new ZipJobState(501);
    public static final ZipJobState FREE_RESOURCES_5B = new ZipJobState(502);

    public static final ZipJobState ERROR = new ZipJobState(1000);
    public static final ZipJobState CANCELED = new ZipJobState(1100);

    private final int value;

    private ZipJobState(int value) {
        this.value = value;
    }

    @Override
    public int compareTo(ZipJobState other) {
        if (other == null) return -1;
        return Integer.compare(this.value, other.value);
    }

    @Override public int hashCode() {
        return this.value;
    }

    @Override public boolean equals(Object var1) {
        if (var1 instanceof ZipJobState) {
            return this.value == ((ZipJobState)var1).value;
        } else {
            return false;
        }
    }

    @Override public String toString() {
        return Integer.toString(value);
    }
}
