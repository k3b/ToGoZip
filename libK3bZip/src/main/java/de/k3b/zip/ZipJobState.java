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
 * Steps that happen in {@Link CompressJob}
 */
public enum ZipJobState {
    UNKNOWN,

    PARSER_ADD_TEXT,
    PARSER_ADD_CLIP_ITEM,
    PARSER_ADD_FILE,
    PARSER_ADD_URI,
    PARSER_ERROR,

    RENAME_COMMENT,

    CREATE_NEW_ZIP_0,
    COPY_EXISTING_ITEM_1A,
    ADD_TEXT_TO_EXISTING_1A1,
    READ_OLD_EXISTING_LOG_ITEM_1A2,
    COPY_NEW_ITEM_1B,
    COPY_LOG_ITEM_1C,
    RENAME_OLD_ZIP_2,
    RENAME_NEW_CREATED_ZIP_3,
    DELETE_EXISTING_RENAMED_ZIP_4,
    SUCCESSFULL_UPDATED_ZIP_5A,
    FREE_RESOURCES_5B,

}
