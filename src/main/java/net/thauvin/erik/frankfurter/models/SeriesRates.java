/*
 * SeriesRates.java
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.thauvin.erik.frankfurter.FrankfurterUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Represents exchange rates over a series of dates for multiple currencies.
 *
 * @param amount    the amount to be converted or used in calculations
 * @param base      the base currency for the time series of exchange rates
 * @param startDate the start date of the time series, formatted as a string
 * @param endDate   the end date of the time series, formatted as a string
 * @param rates     a map where the keys are dates ({@link LocalDate}) and the values are maps of currency codes
 *                  to their respective exchange rates as doubles
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 0.9.0
 */
@SuppressFBWarnings("EI_EXPOSE_REP")
public record SeriesRates(Double amount,
                          String base,
                          String startDate,
                          String endDate,
                          Map<LocalDate, Map<String, Double>> rates) {
    /**
     * Constructs an instance of {@code SeriesRates} using the provided exchange rate time series.
     *
     * @param amount    the amount to be converted or used in calculations
     * @param base      the base currency for the time series of exchange rates
     * @param startDate the start date of the time series, formatted as a string
     * @param endDate   the end date of the time series, formatted as a string
     * @param rates     a map where the keys are dates ({@link LocalDate}) and the values are maps of currency codes
     *                  to their respective exchange rates as doubles
     */
    public SeriesRates {
        if (rates != null) {
            rates = Map.copyOf(rates);
        }
    }

    /**
     * Retrieves the set of dates for which exchange rates are available in the time series.
     *
     * @return the set of dates
     */
    public Set<LocalDate> dates() {
        return rates.keySet();
    }

    /**
     * Retrieves the end date of the time series.
     *
     * @return the end date as a {@link LocalDate}
     */
    public LocalDate endLocalDate() {
        return LocalDate.parse(endDate);
    }

    /**
     * Checks if exchange rates are available for the specified date.
     *
     * @param date the date for which to check the availability of exchange rates
     * @return {@code true} if exchange rates are available for the specified date, {@code false} otherwise
     */
    public boolean hasRatesFor(LocalDate date) {
        return rates.containsKey(date);
    }

    /**
     * Checks if a symbol is available for a specified date in the exchange rate time series.
     *
     * @param date   the {@link LocalDate} for which to check the availability of the symbol
     * @param symbol the currency symbol to check for availability
     * @return {@code true} if the symbol is available for the specified date, {@code false} otherwise
     */
    public boolean hasSymbolFor(LocalDate date, String symbol) {
        if (date == null || symbol == null || symbol.isBlank() || !rates.containsKey(date)) {
            return false;
        }
        return rates.get(date).containsKey(FrankfurterUtils.normalizeSymbol(symbol));
    }

    /**
     * Retrieves the exchange rate for a specific date and currency symbol from the time series.
     *
     * @param date           the date for which the exchange rate is to be retrieved, formatted as a string
     * @param currencySymbol the currency symbol for which the exchange rate is to be retrieved
     * @return The exchange rate for the specified date and currency symbol, or null if no rate is available
     */
    public Double rateFor(LocalDate date, String currencySymbol) {
        if (date == null || currencySymbol == null || currencySymbol.isBlank() ||
                !rates.containsKey(date)) {
            return null;
        }
        return rates.get(date).get(FrankfurterUtils.normalizeSymbol(currencySymbol));
    }

    /**
     * Retrieves the exchange rates for all currencies on the specified date.
     *
     * @param date the date for which the exchange rates are to be retrieved as a {@link LocalDate}
     * @return a map of currency symbols to their respective exchange rates on the specified date,
     * or null if no rates are available for the date
     */
    public Map<String, Double> ratesFor(LocalDate date) {
        if (date == null || !rates.containsKey(date)) {
            return Collections.emptyMap();
        }
        return rates.get(date);
    }

    /**
     * Retrieves the start date of the time series.
     *
     * @return the start date as a {@link LocalDate}
     */
    public LocalDate startLocalDate() {
        return LocalDate.parse(startDate);
    }
}
