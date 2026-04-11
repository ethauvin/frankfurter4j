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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

/**
 * Internal validation helpers used throughout the Frankfurter client.
 *
 * <p>This class centralizes common input checks such as ISO 4217 currency
 * validation, date range validation, and minimum supported date enforcement.
 * It is intended for internal use by configuration builders and endpoint
 * methods, and is not part of the public API surface.</p>
 */
public final class Validation {

    private static final LocalDate MIN_SUPPORTED_DATE = LocalDate.of(1994, 1, 4);

    private Validation() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
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
     * @throws IllegalArgumentException if the code is null, blank, or not three letters
     */
    public static void requireIsoCurrency(@Nullable String code, @NonNull String field) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(field + " currency must not be blank");
        }
        if (code.length() != 3) {
            throw new IllegalArgumentException(field + " currency must be a 3-letter ISO code");
        }
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
     * @throws IllegalArgumentException if the date is earlier than the minimum supported date
     */
    public static void requireSupportedDate(@NonNull LocalDate date, @NonNull String field) {
        if (date.isBefore(MIN_SUPPORTED_DATE)) {
            throw new IllegalArgumentException(field + " must not be earlier than " + MIN_SUPPORTED_DATE);
        }
    }
}
