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

import edu.umd.cs.findbugs.annotations.NonNull;
import net.thauvin.erik.frankfurter.internal.Validation;
import net.thauvin.erik.frankfurter.models.CurrencyCode;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * Provides locale‑aware formatting of monetary amounts using ISO 4217
 * currency codes. The formatter uses a static lookup table mapping each
 * currency to a representative locale. The locale determines grouping,
 * decimal separators, and symbol placement.
 *
 * @apiNote This class is thread‑safe. All state is immutable and formatting
 * operations create new {@link NumberFormat} instances, so no external
 * synchronization is required.
 */
public final class CurrencyFormatter {

    /**
     * Disables the default constructor.
     */
    private CurrencyFormatter() {
        // no-op
    }

    /**
     * Formats a monetary amount using the locale conventions associated
     * with the given currency.
     *
     * @param amount the numeric amount
     * @param code   the currency (must not be {@code null})
     * @return the formatted currency string
     * @throws NullPointerException if {@code code} is {@code null}
     * @see #format(double, CurrencyCode, boolean)
     */
    @NonNull
    public static String format(double amount, @NonNull CurrencyCode code) {
        return format(amount, code, false);
    }

    /**
     * Formats a monetary amount using the locale conventions associated
     * with the given currency.
     *
     * @param amount  the numeric amount
     * @param code    the currency (must not be {@code null})
     * @param rounded whether to round to the locale's default fraction digits
     * @return the formatted currency string
     * @throws NullPointerException if {@code code} is {@code null}
     * @see #format(double, String, boolean)
     */
    @NonNull
    public static String format(double amount, @NonNull CurrencyCode code, boolean rounded) {
        Objects.requireNonNull(code, Validation.formatNullMessage("code"));
        return format(amount, code.getLocale(), rounded);
    }

    /**
     * Formats a monetary amount using the locale conventions associated
     * with the given ISO currency code.
     *
     * @param amount  the numeric amount
     * @param isoCode the ISO 4217 currency code (must not be {@code null} or blank)
     * @return the formatted currency string
     * @throws IllegalArgumentException if {@code isoCode} is {@code null}, blank, or unknown
     * @see #format(double, Locale, boolean)
     */
    @NonNull
    public static String format(double amount, @NonNull String isoCode) {
        return format(amount, isoCode, false);
    }

    /**
     * Formats a monetary amount using the locale conventions associated
     * with the given ISO currency code.
     *
     * @param amount  the numeric amount
     * @param isoCode the ISO 4217 currency code (must not be {@code null} or blank)
     * @param rounded whether to round to the locale's default fraction digits
     * @return the formatted currency string
     * @throws IllegalArgumentException if {@code isoCode} is {@code null}, blank, or unknown
     * @see #format(double, String, boolean)
     */
    @NonNull
    public static String format(double amount, @NonNull String isoCode, boolean rounded) {
        return CurrencyCode.fromCode(isoCode)
                .map(c -> format(amount, c.getLocale(), rounded))
                .orElseThrow(() -> new IllegalArgumentException("Unknown ISO currency code: " + isoCode));
    }

    /**
     * Formats a monetary amount using the locale's currency and conventions.
     *
     * <ul>
     *   <li><b>Currency</b> – Determined by {@code locale} only. Passing {@code Locale.US} formats as USD.</li>
     *   <li><b>Rounding</b> – Always uses {@link RoundingMode#HALF_UP}
     *     <ul>
     *       <li><b>{@code rounded=true}</b> – Uses currency's default fraction digits. USD=2, JPY=0.</li>
     *       <li><b>{@code rounded=false}</b> – Uses up to 15 fraction digits to preserve precision.</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * @param amount  the numeric amount to format
     * @param locale  locale for currency + format rules; must not be {@code null}
     * @param rounded {@code true} for currency digits, {@code false} for 15 digits max
     * @return formatted currency string, never {@code null}
     * @throws NullPointerException if {@code locale} is {@code null}
     */
    @NonNull
    public static String format(double amount, @NonNull Locale locale, boolean rounded) {
        Objects.requireNonNull(locale, Validation.formatNullMessage("locale"));
        var formatter = NumberFormat.getCurrencyInstance(locale);
        formatter.setRoundingMode(RoundingMode.HALF_UP);

        if (!rounded) {
            formatter.setMaximumFractionDigits(15);
        }
        return formatter.format(amount);
    }
}