/*
 * CurrencyFormatter.java
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
package net.thauvin.erik.frankfurter;

import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.NoSuchElementException;

/**
 * Utility class for formatting currency values.
 */
public final class CurrencyFormatter {

    /**
     * Maximum fraction digits used to suppress rounding for arbitrary-precision display.
     * {@link Integer#MAX_VALUE} signals "no practical limit" to {@link NumberFormat}.
     */
    private static final int MAX_FRACTION_DIGITS = Integer.MAX_VALUE;

    private CurrencyFormatter() {
        // no-op
    }

    /**
     * Formats a currency amount based on the provided ISO currency symbol and amount.
     *
     * @param symbol the 3-letter ISO currency symbol (e.g., "USD", "EUR")
     * @param amount the monetary amount to format
     * @return a formatted currency string
     * @throws IllegalArgumentException if the currency symbol is unknown or invalid
     * @throws NullPointerException     if {@code symbol} is {@code null}
     */
    @NotNull
    public static String format(@NotNull String symbol, double amount) {
        return format(symbol, amount, false);
    }

    /**
     * Formats a currency amount based on the provided ISO currency symbol, amount, and rounding preference.
     *
     * @param symbol  the 3-letter ISO currency symbol (e.g., "USD", "EUR")
     * @param amount  the monetary amount to format
     * @param rounded whether to round the amount
     * @return a formatted currency string
     * @throws IllegalArgumentException if the currency symbol is unknown or invalid
     * @throws NoSuchElementException   if the currency symbol is not found in the registry
     * @throws NullPointerException     if {@code symbol} is {@code null}
     */
    @NotNull
    public static String format(@NotNull String symbol, double amount, boolean rounded) {
        var normalizedSymbol = FrankfurterUtils.normalizeSymbol(symbol);
        var locale = CurrencyRegistry.getInstance().findBySymbol(normalizedSymbol).orElseThrow().locale();
        var currencyFormatter = NumberFormat.getCurrencyInstance(locale);
        if (!rounded) {
            // Suppress rounding to preserve full precision of the raw amount.
            currencyFormatter.setMaximumFractionDigits(MAX_FRACTION_DIGITS);
        }
        return currencyFormatter.format(amount);
    }
}