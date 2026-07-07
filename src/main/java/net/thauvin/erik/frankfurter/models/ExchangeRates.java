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

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.thauvin.erik.frankfurter.internal.Validation;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a list of exchange rates returned by the Frankfurter API.
 *
 * <p>The API returns a JSON array of rate objects. For time series or grouped queries,
 * the same quote currency may appear multiple times with different dates.</p>
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @apiNote This class is immutable and thread-safe.
 * @since 1.0
 */
@SuppressFBWarnings(
        value = "SING_SINGLETON_HAS_NONPRIVATE_CONSTRUCTOR",
        justification = "Not a singleton. EMPTY is a cached constant like List.of()"
)
public final class ExchangeRates implements RatesResult {

    private static final ExchangeRates EMPTY = new ExchangeRates(List.of());
    private static final String PARAM_NAME_QUOTE = "quote";
    private static final int TO_STRING_PREVIEW_LIMIT = 10;
    private final List<Rate> rates;

    /**
     * Creates a new immutable container for the given list of rates.
     *
     * @param rates the list of rate entries
     * @throws NullPointerException if {@code rates} is {@code null} or contains null elements
     */
    public ExchangeRates(@NonNull Collection<Rate> rates) {
        Validation.requireAllNonNull("rates", rates);
        this.rates = List.copyOf(rates);
    }

    @Override
    public int hashCode() {
        return rates.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof ExchangeRates that && rates.equals(that.rates);
    }

    /**
     * Returns a string representation showing size and preview some entries for debugging.
     *
     * @return a string with class name, size, and preview of entries
     */
    @Override
    public String toString() {
        if (rates.isEmpty()) {
            return "ExchangeRates{size=0}";
        }
        var preview = rates.stream()
                .limit(TO_STRING_PREVIEW_LIMIT)
                .map(Rate::toString)
                .collect(Collectors.joining(", "));
        var suffix = rates.size() > TO_STRING_PREVIEW_LIMIT ? ", ..." : "";
        return "ExchangeRates{size=" + rates.size() + ", rates=[" + preview + suffix + "]}";
    }

    /**
     * Returns an empty {@code ExchangeRates} instance.
     *
     * @return an empty instance
     */
    public static ExchangeRates empty() {
        return EMPTY;
    }

    /**
     * Finds the first entry matching the given quote currency.
     * <p>For time series data with multiple entries per currency, this returns
     * the first match in iteration order, which matches API response order.</p>
     *
     * @param quote the ISO 4217 quote currency
     * @return an optional containing the first matching rate
     * @throws NullPointerException if {@code quote} is {@code null}
     */
    public Optional<Rate> find(@NonNull String quote) {
        Objects.requireNonNull(quote, Validation.formatNullMessage(PARAM_NAME_QUOTE));
        var normalized = quote.toUpperCase(Locale.ROOT);
        return rates.stream()
                .filter(r -> r.quote().equals(normalized))
                .findFirst();
    }

    /**
     * Finds the first entry matching the given quote currency.
     * <p>For time series data with multiple entries per currency, this returns
     * the first match in iteration order, which matches API response order.</p>
     *
     * @param quote the quote currency
     * @return an optional containing the first matching rate
     * @throws NullPointerException if {@code quote} is {@code null}
     */
    public Optional<Rate> find(@NonNull CurrencyCode quote) {
        Objects.requireNonNull(quote, Validation.formatNullMessage(PARAM_NAME_QUOTE));
        return find(quote.getCode());
    }

    /**
     * Returns all rate entries matching the given quote currency.
     * <p>Useful for time series where the same currency appears multiple times
     * with different dates.</p>
     *
     * @param quote the ISO 4217 quote currency
     * @return unmodifiable list of matching rates, empty if none found
     * @throws NullPointerException if {@code quote} is {@code null}
     */
    public List<Rate> findAll(@NonNull String quote) {
        Objects.requireNonNull(quote, Validation.formatNullMessage(PARAM_NAME_QUOTE));
        var normalized = quote.toUpperCase(Locale.ROOT);
        return rates.stream()
                .filter(r -> r.quote().equals(normalized))
                .toList();
    }

    /**
     * Returns all rate entries matching the given quote currency.
     * <p>Useful for time series where the same currency appears multiple times
     * with different dates.</p>
     *
     * @param quote the quote currency
     * @return unmodifiable list of matching rates, empty if none found
     * @throws NullPointerException if {@code quote} is {@code null}
     */
    public List<Rate> findAll(@NonNull CurrencyCode quote) {
        Objects.requireNonNull(quote, Validation.formatNullMessage(PARAM_NAME_QUOTE));
        return findAll(quote.getCode());
    }

    /**
     * Returns {@code true} if there are no rate entries.
     *
     * @return {@code true} if the list is empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        return rates.isEmpty();
    }

    /**
     * Returns all distinct quote currencies as {@link CurrencyCode}, filtering out unknown codes.
     *
     * @return unmodifiable list of known currency codes
     */
    @NonNull
    public List<CurrencyCode> knownQuotes() {
        return rates.stream()
                .map(Rate::quote)
                .distinct()
                .map(CurrencyCode::fromCode)
                .flatMap(Optional::stream)
                .toList();
    }

    /**
     * Returns an unmodifiable list of all rate entries.
     *
     * @return the list of rates
     */
    public List<Rate> list() {
        return rates;
    }

    /**
     * Returns all distinct quote currency codes in this result set.
     *
     * @return unmodifiable list of ISO 4217 codes
     */
    @NonNull
    public List<String> quotes() {
        return rates.stream()
                .map(Rate::quote)
                .distinct()
                .toList();
    }

    /**
     * Returns the number of rate entries.
     *
     * @return the number of entries
     */
    public int size() {
        return rates.size();
    }
}
