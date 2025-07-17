/*
 * Currencies.java
 *
 * Copyright 2025 Erik C. Thauvin (erik@thauvin.net)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *   Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *   Neither the name of this project nor the names of its contributors may be
 *   used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.thauvin.erik.frankfurter.models;

import net.thauvin.erik.frankfurter.FrankfurterUtils;

import java.io.Serial;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Represents a map of available currency symbols to their full names.
 */
public class Currencies extends ConcurrentHashMap<String, String> {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Retrieves the full name of a currency by its symbol.
     *
     * @param symbol the currency symbol
     * @return the full name of the currency, or null if the symbol doesn't exist
     */
    public String getFullNameFor(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return null;
        }
        return get(FrankfurterUtils.normalizeSymbol(symbol));
    }

    /**
     * Retrieves the currency symbol corresponding to the given currency name.
     *
     * @param name the full name of the currency for which the symbol is to be retrieved
     * @return the symbol of the currency if the name matches an entry, or null if no match is found
     */
    public String getSymbolFor(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        for (var entry : this.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(name)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Retrieves the currency symbol corresponding to a currency name that matches the given regular expression pattern.
     *
     * @param regexPattern the pattern used to match currency names
     * @return the symbol of the currency if a matching name is found, or null if no match is found or if the pattern
     * is null.
     */
    public String getSymbolFor(Pattern regexPattern) {
        if (regexPattern == null) {
            return null;
        }
        for (var entry : this.entrySet()) {
            if (regexPattern.matcher(entry.getValue()).matches()) {
                return entry.getKey();
            }
        }
        return null;
    }
}

