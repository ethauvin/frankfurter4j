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

package net.thauvin.erik.frankfurter;


import edu.umd.cs.findbugs.annotations.NonNull;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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
     * Checks if a string is null or blank.
     *
     * @param s the string to check
     * @return {@code true} if the string is null or blank, {@code false} otherwise
     */
    public static boolean isNullOrBlank(String s) {
        return s == null || s.isBlank();
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
     * @param fieldName a descriptive name for the parameter being validated
     * @param values    the collection to validate
     * @param <T>       the element type
     * @throws NullPointerException if the collection or any element is {@code null}
     */
    @SuppressWarnings("PMD.AvoidThrowingNullPointerException")
    public static <T> void requireAllNonNull(@NonNull Collection<T> values,
                                             @NonNull String fieldName) {
        Objects.requireNonNull(fieldName, "fieldName must not be null");
        Objects.requireNonNull(values, fieldName + " must not be null");

        int i = 0;
        for (T v : values) {
            if (v == null) {
                throw new NullPointerException(fieldName + "[" + i + "] must not be null");
            }
            i++;
        }
    }


    /**
     * Ensures that the given varargs array and all of its elements are non‑null.
     *
     * <p>This method delegates to the list‑based validator after converting the
     * varargs array to a list using {@link List#of(Object[])}. The exception
     * semantics and messages are identical to the list‑based version.</p>
     *
     * @param fieldName a descriptive name for the parameter being validated
     * @param values    the array to validate
     * @throws NullPointerException if the array or any element is {@code null}
     */
    public static void requireAllNonNull(@NonNull String fieldName, @NonNull String... values) {
        Objects.requireNonNull(fieldName, "fieldName must not be null");
        Objects.requireNonNull(values, fieldName + " must not be null");

        // Delegate to the list-based version
        requireAllNonNull(List.of(values), fieldName);
    }

    /**
     * Validates that the given value is a non-blank ISO 4217 alphabetic currency code.
     *
     * <p>The code must consist of exactly three letters. This method performs only
     * structural validation; it does not verify that the code corresponds to a
     * known currency.</p>
     *
     * @param code  the currency code to validate
     * @param field the field name used in exception messages
     * @return the validated currency code
     * @throws IllegalArgumentException if the {@code code} blank, or not three letters
     * @throws NullPointerException     if the {@code code} or {@code field} are {@code null}
     */
    public static String requireIsoCurrency(@NonNull String code, @NonNull String field) {
        Objects.requireNonNull(field, "field must not be null");
        Objects.requireNonNull(code, field + " currency must not be null");

        if (code.isBlank()) {
            throw new IllegalArgumentException(field + " currency must not be blank");
        }
        if (code.length() != 3) {
            throw new IllegalArgumentException(field + " currency must be a 3-letter ISO code");
        }

        return code;
    }

    /**
     * Validates that the given date is not earlier than the earliest supported
     * Frankfurter reference date.
     *
     * <p>This method enforces the minimum supported date of {@code 1994-01-04}.
     * It does not validate future dates, which are permitted by the API.</p>
     *
     * @param date  the date to validate
     * @param field the field name used in exception messages
     * @return the validated date
     * @throws IllegalArgumentException if the date is earlier than the minimum supported date
     */
    public static LocalDate requireSupportedDate(@NonNull LocalDate date, @NonNull String field) {
        Objects.requireNonNull(field, "field must not be null");
        Objects.requireNonNull(date, field + " must not be null");

        if (date.isBefore(MIN_SUPPORTED_DATE)) {
            throw new IllegalArgumentException(field + " must not be earlier than " + MIN_SUPPORTED_DATE);
        }

        return date;
    }
}
