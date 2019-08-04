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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 /**
 * A slf4j based, android inependant consumer that can receive log messages that shows what is going on.
 *
 * Created by k3b on 25.11.2014.
 */
public class ZipLogImpl  implements ZipLog {
    private static final Logger logger = LoggerFactory.getLogger(LibZipGlobal.LOG_TAG);
    private static final String BLANK = " ";
    /**
     * last errormessage
     */
    private StringBuilder lastError = new StringBuilder();

    /**
     * debug Log Messages if enabled or null
     */
    private StringBuilder debugLogMessages = null;

    public ZipLogImpl(boolean useDebugLog) {
        this.debugLogMessages = (useDebugLog) ? new StringBuilder() : null;
    }

    /**
     * formats context message and does low level logging
     */
    @Override
    public String traceMessage(int zipStateID, int itemNumber, int itemTotal, String format, Object... params) {
        String result = MessageFormat.format(format, params);
        logger.debug(result);
        if (this.debugLogMessages != null) {
            this.debugLogMessages
                    .append(itemNumber)
                    .append("/")
                    .append(itemTotal)
                    .append(": ")
                    .append(result)
                    .append(BLANK)
                    .append(zipStateID)
                    .append("\n");
        }
        // System.out.println(result);
        return result;
    }

    /** adds an errormessage to error-result */
    @Override
    public void addError(String errorMessage) {
        this.lastError.append(errorMessage).append("\n");
    }

    /**
     * get last error plus debugLogMessages if available
     */
    @Override
    public String getLastError(boolean detailed) {
        if ((!detailed) || (this.debugLogMessages == null)) return lastError.toString();
        return this.debugLogMessages + "\n\n" + lastError.toString();
    }
}
