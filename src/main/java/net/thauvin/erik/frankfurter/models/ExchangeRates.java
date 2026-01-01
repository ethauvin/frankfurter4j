/*
 * ExchangeRates.java
 *
 * Copyright (c) 2025-2026 Erik C. Thauvin (erik@thauvin.net)
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

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

/**
 * Represents the exchange rates for a base currency.
 *
 * @param amount the amount of the base currency
 * @param base   the base currency
 * @param date   the date of the exchange rates
 * @param rates  the exchange rates
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 0.9.0
 */
public record ExchangeRates(Double amount, String base, LocalDate date, Map<String, Double> rates) {
    /**
     * Constructs a new instance of the ExchangeRates record.
     *
     * @param amount the amount of the base currency
     * @param base   the base currency
     * @param date   the date of the exchange rates
     * @param rates  the exchange rates map, where keys are currency symbols and values are their respective rates
     */
    public ExchangeRates {
        if (rates != null) {
            rates = Map.copyOf(rates);
        }
    }

    /**
     * Checks if the exchange rates contain the specified currency symbol.
     *
     * @param symbol the currency symbol to check for
     * @return {@code true} if the exchange rates contain the symbol, {@code false} otherwise
     */
    public boolean hasRateFor(String symbol) {
        return rates.containsKey(symbol);
    }

    /**
     * Retrieves the exchange rate for the specified currency symbol.
     *
     * @param symbol the currency symbol
     */
    public Double rateFor(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return null;
        }
        return rates.get(FrankfurterUtils.normalizeSymbol(symbol));
    }

    /**
     * Retrieves the set of all currency symbols available in the exchange rates.
     *
     * @return a set of strings representing the currency symbols
     */
    public Set<String> symbols() {
        return rates.keySet();
    }
}
