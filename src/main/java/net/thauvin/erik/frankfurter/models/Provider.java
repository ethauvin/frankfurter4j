/*
 * Provider.java
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
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import net.thauvin.erik.frankfurter.Validation;

import java.time.LocalDate;
import java.util.List;

/**
 * Represents a provider entry returned by the Frankfurter {@code /providers} endpoint.
 *
 * <p>The API supplies metadata for each provider, including its identifier,
 * descriptive information, data source URLs, the date range of available data,
 * and the list of currencies supported by the provider.</p>
 *
 * <p>Only {@code key} and {@code name} are guaranteed to be non‑null. All other
 * fields may be {@code null} depending on the provider and the data available
 * from the API.</p>
 *
 * <p>The {@code currencies} list is normalized to a non‑null, unmodifiable list
 * to ensure safe iteration and predictable behavior.</p>
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public record Provider(
        @SerializedName("key")
        @NonNull String key,

        @SerializedName("name")
        @NonNull String name,

        @SerializedName("description")
        @Nullable String description,

        @SerializedName("data_url")
        @Nullable String dataUrl,

        @SerializedName("terms_url")
        @Nullable String termsUrl,

        @SerializedName("start_date")
        @Nullable LocalDate startDate,

        @SerializedName("end_date")
        @Nullable LocalDate endDate,

        @SerializedName("currencies")
        @NonNull List<String> currencies
) {

    /**
     * Validates required fields and normalizes optional values.
     *
     * <p>This compact constructor enforces the non‑blank contract for
     * {@code key} and {@code name}, which are the only required fields
     * in a provider entry. All other fields may be {@code null} as
     * supplied by the API.</p>
     *
     * <p>The {@code currencies} list is defensively normalized to a
     * non‑null, unmodifiable list. When the API omits the field or
     * supplies {@code null}, it is replaced with an empty list.</p>
     *
     * @param key         the provider identifier (must not be blank)
     * @param name        the provider name (must not be blank)
     * @param description the provider description, or {@code null}
     * @param dataUrl     the data source URL, or {@code null}
     * @param termsUrl    the terms‑of‑use URL, or {@code null}
     * @param startDate   the first date for which data is available, or {@code null}
     * @param endDate     the last date for which data is available, or {@code null}
     * @param currencies  the list of supported currencies, or {@code null}
     * @throws IllegalArgumentException if {@code key} or {@code name} is blank
     */
    @SuppressWarnings("ConstantValue")
    public Provider {
        if (Validation.isNullOrBlank(key)) {
            throw new IllegalArgumentException("key must not be blank");
        }
        if (Validation.isNullOrBlank(name)) {
            throw new IllegalArgumentException("name must not be blank");
        }

        // Normalize null → empty list (API always provides an array, but defensive)
        currencies = currencies == null ? List.of() : List.copyOf(currencies);
    }
}