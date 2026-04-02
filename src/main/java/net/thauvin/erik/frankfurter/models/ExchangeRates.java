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
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
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
public record ExchangeRates(Double amount, @NotNull String base, LocalDate date, @NotNull Map<String, Double> rates) {

    /**
     * Constructs a new instance of the ExchangeRates record.
     *
     * @param amount the amount of the base currency
     * @param base   the base currency
     * @param date   the date of the exchange rates
     * @param rates  the exchange rates map, where keys are currency symbols and values are their respective rates;
     *               must not be {@code null}
     * @throws NullPointerException if {@code rates} is {@code null}
     */
    public ExchangeRates {
        Objects.requireNonNull(rates, "rates must not be null");
        rates = Map.copyOf(rates);
    }

    /**
     * Checks if the exchange rates contain the specified currency symbol.
     *
     * <p>It is recommended to call this method before {@link #rateFor(String)} to avoid a {@code null} return value.
     *
     * @param symbol the currency symbol to check for
     * @return {@code true} if the exchange rates contain the symbol, {@code false} otherwise
     */
    public boolean hasRateFor(@NotNull String symbol) {
        var normalized = FrankfurterUtils.normalizeSymbol(symbol);
        return rates.containsKey(normalized);
    }

    /**
     * Retrieves the exchange rate for the specified currency symbol.
     *
     * <p>It is recommended to call {@link #hasRateFor(String)} before this method to avoid a {@code null}
     * return value.
     *
     * @param symbol the currency symbol
     * @return the exchange rate for the specified symbol, or {@code null} if no rate is available
     */
    public Double rateFor(@NotNull String symbol) {
        var normalized = FrankfurterUtils.normalizeSymbol(symbol);
        return rates.get(normalized);
    }

    /**
     * Retrieves the set of all currency symbols available in the exchange rates.
     *
     * @return an unmodifiable copy of the set of currency symbols
     */
    @NotNull
    public Set<String> symbols() {
        return Set.copyOf(rates.keySet());
    }
}