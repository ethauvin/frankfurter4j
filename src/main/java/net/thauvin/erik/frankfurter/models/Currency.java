/*
 * Currency.java
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

import com.google.gson.annotations.SerializedName;
import edu.umd.cs.findbugs.annotations.Nullable;
import net.thauvin.erik.frankfurter.Validation;

import java.time.LocalDate;

/**
 * Represents a currency entry returned by the Frankfurter {@code /currencies} endpoint.
 *
 * <p>The API provides metadata for each currency, including its ISO codes,
 * display name, symbol, and the date range for which data is available.</p>
 *
 * <p>All fields except {@code isoCode} and {@code name} may be {@code null}
 * depending on the currency and the data available from the API.</p>
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public record Currency(
        @SerializedName("iso_code")
        String isoCode,

        @SerializedName("iso_numeric")
        @Nullable String isoNumeric,

        @SerializedName("name")
        String name,

        @SerializedName("symbol")
        @Nullable String symbol,

        @SerializedName("start_date")
        @Nullable LocalDate startDate,

        @SerializedName("end_date")
        @Nullable LocalDate endDate
) implements CurrencyResult {

    /**
     * Creates a new currency entry.
     *
     * @param isoCode    the ISO 4217 alphabetic currency code
     * @param isoNumeric the ISO 4217 numeric currency code, or {@code null}
     * @param name       the currency name
     * @param symbol     the currency symbol, or {@code null}
     * @param startDate  the first date for which data is available, or {@code null}
     * @param endDate    the last date for which data is available, or {@code null}
     * @throws IllegalArgumentException if {@code isoCode} or {@code name} is blank
     */
    public Currency {
        if (Validation.isNullOrBlank(isoCode)) {
            throw new IllegalArgumentException("isoCode must not be blank");
        }
        if (Validation.isNullOrBlank(name)) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }
}
