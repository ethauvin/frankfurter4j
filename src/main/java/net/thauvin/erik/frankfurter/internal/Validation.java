/*
 * Validation.java
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

package net.thauvin.erik.frankfurter.internal;


import edu.umd.cs.findbugs.annotations.NonNull;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;

/**
 * Internal validation helpers used throughout the Frankfurter client.
 *
 * <p>This class centralizes common input checks such as ISO 4217 currency
 * validation, date range validation, and minimum supported date enforcement.
 * It is intended for internal use by configuration builders and endpoint
 * methods, and is not part of the public API surface.</p>
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public final class Validation {

    private static final LocalDate MIN_SUPPORTED_DATE = LocalDate.of(1994, 1, 4);

    /**
     * You can't call the constructor.
     */
    private Validation() {

    }

    /**
     * Appends a standard "must not be {@code null}" message to the given field description.
     * <p>
     * This is typically used for constructing exception messages when a required
     * parameter is {@code null}. Pass a descriptive field name, not the value itself.
     *
     * @param name a descriptive name for the parameter being validated
     * @return a string consisting of the given field name followed by " must not be {@code null}"
     * @throws NullPointerException if {@code name} is {@code null}
     */
    public static String formatNullMessage(@NonNull String name) {
        Objects.requireNonNull(name, "name must not be {@code null}");
        return name + " must not be {@code null}";
    }

    /**
     * Ensures that the given varargs array and all of its elements are non‑null.
     *
     * <p>This method delegates to the list‑based validator after converting the
     * varargs array to a list using {@link List#of(Object[])}. The exception
     * semantics and messages are identical to the list‑based version.</p>
     *
     * @param name   a descriptive name for the parameter being validated
     * @param values the array to validate
     * @throws NullPointerException if the array or any element is {@code null}
     */
    public static void requireAllNonNull(@NonNull String name, @NonNull String... values) {
        Objects.requireNonNull(values, formatNullMessage(name));

        // Delegate to the list-based version
        requireAllNonNull(name, List.of(values));
    }

    /**
     * Ensures that the given collection and all of its elements are non‑null.
     *
     * <p>This method validates both the collection reference and each individual
     * element. If the collection itself is {@code null}, or if any element within
     * it is {@code null}, a {@link NullPointerException} is thrown. The exception
     * message includes the supplied field name to make validation errors easier
     * to diagnose.</p>
     *
     * @param name   a descriptive name for the parameter being validated
     * @param values the collection to validate
     * @param <T>    the element type
     * @throws NullPointerException if the collection or any element is {@code null}
     */
    @SuppressWarnings("PMD.AvoidThrowingNullPointerException")
    public static <T> void requireAllNonNull(@NonNull String name, @NonNull Collection<T> values) {
        Objects.requireNonNull(values, formatNullMessage(name));

        int i = 0;
        for (T v : values) {
            if (v == null) {
                throw new NullPointerException(formatNullMessage(name + '[' + i + ']'));
            }
            i++;
        }
    }

    /**
     * Validates that the given value is a non-blank ISO 4217 alphabetic currency code.
     *
     * <p>The code must consist of exactly three letters. This method performs only
     * structural validation; it does not verify that the code corresponds to a
     * known currency.</p>
     *
     * @param name a descriptive name for the parameter being validated
     * @param code the currency code to validate
     * @return the validated currency code
     * @throws IllegalArgumentException if the {@code code} blank, or not three letters
     * @throws NullPointerException     if the {@code code} or {@code name} are {@code null}
     */
    public static String requireIsoCurrency(@NonNull String name, @NonNull String code) {
        Objects.requireNonNull(code, formatNullMessage(name + " currency"));

        if (code.isBlank()) {
            throw new IllegalArgumentException(name + " currency must not be blank");
        }

        if (!code.matches("[A-Za-z]{3}")) {
            throw new IllegalArgumentException(name + " currency must be a 3-letter ISO code");
        }

        return code.toUpperCase(Locale.ROOT);
    }

    /**
     * Validates an array of ISO currency codes, filters out blanks, trims, uppercases, and removes duplicates.
     *
     * @param name   a descriptive name for the parameter being validated
     * @param values the currency codes to validate and clean
     * @return a new array with validated, normalized currency codes
     * @throws NullPointerException     if array or any element is {@code null}
     * @throws IllegalArgumentException if any code is blank or not 3 letters
     */
    @NonNull
    @SuppressWarnings("PMD.UseVarargs")
    public static String[] requireIsoCurrencyArray(@NonNull String name, @NonNull String[] values) {
        Objects.requireNonNull(values, formatNullMessage(name));
        requireAllNonNull(name, values);
        return Arrays.stream(values)
                .filter(Predicate.not(String::isBlank))
                .map(code -> requireIsoCurrency(name, code))
                .distinct()
                .toArray(String[]::new);
    }

    /**
     * Validates an array of strings, filters out blanks, trims, and removes duplicates.
     *
     * @param name   a descriptive name for the parameter being validated
     * @param values the array to validate and clean
     * @return a new array with blanks removed, values trimmed, and duplicates removed
     * @throws NullPointerException if array or any element is {@code null}
     */
    @NonNull
    @SuppressWarnings("PMD.UseVarargs")
    public static String[] requireNonBlankDistinct(@NonNull String name, @NonNull String[] values) {
        Objects.requireNonNull(values, formatNullMessage(name));
        requireAllNonNull(name, values);
        return Arrays.stream(values)
                .filter(Predicate.not(String::isBlank))
                .map(String::trim)
                .distinct()
                .toArray(String[]::new);
    }

    /**
     * Checks that the specified string is neither {@code null} nor blank.
     *
     * @param name  a descriptive name for the parameter being validated
     * @param value the string value to validate
     * @return {@code value} if it is not {@code null} and not blank
     * @throws NullPointerException     if {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code value} is blank
     * @throws IllegalArgumentException if {@code name} is {@code null} or blank
     */
    public static String requireNonNullOrBlank(String name, String value) {
        Objects.requireNonNull(name, "name must not be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException(" must not be blank");
        }

        Objects.requireNonNull(value, name + " must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }

        return value;
    }

    /**
     * Validates that the given date is not earlier than the earliest supported
     * Frankfurter reference date.
     *
     * <p>This method enforces the minimum supported date of {@code 1994-01-04}.
     * It does not validate future dates, which are permitted by the API.</p>
     *
     * @param name a descriptive name for the parameter being validated
     * @param date the date to validate
     * @return the validated date
     * @throws IllegalArgumentException if the date is earlier than the minimum supported date
     */
    public static LocalDate requireSupportedDate(@NonNull String name, @NonNull LocalDate date) {
        Objects.requireNonNull(date, formatNullMessage(name));

        if (date.isBefore(MIN_SUPPORTED_DATE)) {
            throw new IllegalArgumentException(name + " must not be earlier than " + MIN_SUPPORTED_DATE);
        }

        return date;
    }
}