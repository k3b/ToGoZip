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
 * Interface for a consumer that can receive log messages that shows what is going on.
 *
 * Created by k3b on 25.11.2014.
 */
public interface ZipLog {

    /* Needs java8 :-(; Android supports only java7

    default String traceMessage(String format, Object... params) {
        return traceMessage(ZipJobState.UNKNOWN, 0,0, format, params);
    }
    */

    /**
     * formats context message and does low level logging
     */
    String traceMessage(int zipStateID, int itemNumber, int itemTotal, String format, Object... params);

    /** adds an errormessage to error-result */
    void addError(String errorMessage);

    /**
     * get last error plus debugLogMessages if available
     */
    String getLastError(boolean detailed);
}
