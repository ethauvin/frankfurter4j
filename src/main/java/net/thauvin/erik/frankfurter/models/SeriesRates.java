/*
 * SeriesRates.java
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.thauvin.erik.frankfurter.FrankfurterUtils;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "outer map is unmodifiable; inner maps are Map.copyOf()")
public record SeriesRates(@NotNull Double amount,
                          @NotNull String base,
                          @NotNull String startDate,
                          @NotNull String endDate,
                          @NotNull Map<LocalDate, Map<String, Double>> rates) {

    private static final String DATE_MUST_NOT_BE_NULL = "date must not be null";

    /**
     * Constructs an instance of {@code SeriesRates} using the provided exchange rate time series.
     *
     * <p>The {@code rates} map and all inner currency maps are deep-copied into unmodifiable maps.
     * Both {@code startDate} and {@code endDate} are validated as parseable {@link LocalDate} values
     * at construction time.
     *
     * @param amount    the amount to be converted or used in calculations
     * @param base      the base currency for the time series of exchange rates
     * @param startDate the start date of the time series, formatted as a string
     * @param endDate   the end date of the time series, formatted as a string
     * @param rates     a map where the keys are dates ({@link LocalDate}) and the values are maps of currency codes
     *                  to their respective exchange rates as doubles; must not be {@code null}
     * @throws NullPointerException                    if {@code rates} is {@code null}
     * @throws java.time.format.DateTimeParseException if {@code startDate} or {@code endDate} cannot be parsed
     */
    @SuppressFBWarnings(value = "STT_TOSTRING_STORED_IN_FIELD", justification = "fields are fully validated")
    public SeriesRates {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(rates, "rates must not be null");
        // Normalize and validate date strings ? throws DateTimeParseException if invalid
        startDate = LocalDate.parse(startDate).toString();
        endDate = LocalDate.parse(endDate).toString();
        // Deep-copy: make both the outer map and every inner currency map unmodifiable
        rates = rates.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        e -> Map.copyOf(e.getValue())
                ));
    }

    /**
     * Retrieves the set of dates for which exchange rates are available in the time series.
     *
     * @return an unmodifiable copy of the set of dates
     */
    @NotNull
    public Set<LocalDate> dates() {
        return Set.copyOf(rates.keySet());
    }

    /**
     * Retrieves the end date of the time series.
     *
     * @return the end date as a {@link LocalDate}
     */
    @NotNull
    public LocalDate endLocalDate() {
        return LocalDate.parse(endDate);
    }

    /**
     * Checks if exchange rates are available for the specified date.
     *
     * @param date the date for which to check the availability of exchange rates
     * @return {@code true} if exchange rates are available for the specified date, {@code false} otherwise
     */
    public boolean hasRatesFor(@NotNull LocalDate date) {
        Objects.requireNonNull(date, DATE_MUST_NOT_BE_NULL);
        return rates.containsKey(date);
    }

    /**
     * Checks if a currency symbol is available for a specified date in the exchange rate time series.
     *
     * @param date   the {@link LocalDate} for which to check the availability of the symbol
     * @param symbol the currency symbol to check for availability
     * @return {@code true} if the symbol is available for the specified date, {@code false} otherwise
     */
    public boolean hasSymbolFor(@NotNull LocalDate date, @NotNull String symbol) {
        Objects.requireNonNull(symbol, "symbol must not be null");
        Objects.requireNonNull(date, DATE_MUST_NOT_BE_NULL);
        if (!rates.containsKey(date)) {
            return false;
        }
        var normalized = FrankfurterUtils.normalizeSymbol(symbol);
        return rates.get(date).containsKey(normalized);
    }

    /**
     * Retrieves the exchange rate for a specific date and currency symbol from the time series.
     *
     * <p>It is recommended to call {@link #hasRatesFor(LocalDate)} and {@link #hasSymbolFor(LocalDate, String)}
     * before this method to avoid a {@code null} return value.
     *
     * @param date   the date for which the exchange rate is to be retrieved
     * @param symbol the currency symbol for which the exchange rate is to be retrieved
     * @return the exchange rate for the specified date and currency symbol,
     * or {@code null} if no rate is available
     */
    public Double rateFor(@NotNull LocalDate date, @NotNull String symbol) {
        Objects.requireNonNull(date, DATE_MUST_NOT_BE_NULL);
        if (!rates.containsKey(date)) {
            return null;
        }
        var normalized = FrankfurterUtils.normalizeSymbol(symbol);
        return rates.get(date).get(normalized);
    }

    /**
     * Retrieves the exchange rates for all currencies on the specified date.
     *
     * @param date the date for which the exchange rates are to be retrieved as a {@link LocalDate}
     * @return an unmodifiable map of currency symbols to their respective exchange rates on the specified date,
     * or an empty map if no rates are available for the date
     */
    @NotNull
    public Map<String, Double> ratesFor(@NotNull LocalDate date) {
        Objects.requireNonNull(date, DATE_MUST_NOT_BE_NULL);
        if (!rates.containsKey(date)) {
            return Collections.emptyMap();
        }
        return Map.copyOf(rates.get(date));
    }

    /**
     * Retrieves the start date of the time series.
     *
     * @return the start date as a {@link LocalDate}
     */
    @NotNull
    public LocalDate startLocalDate() {
        return LocalDate.parse(startDate);
    }
}