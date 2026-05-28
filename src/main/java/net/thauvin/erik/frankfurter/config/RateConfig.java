/*
 * RateConfig.java
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

package net.thauvin.erik.frankfurter.config;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import net.thauvin.erik.frankfurter.Validation;

import java.net.URI;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Immutable configuration for constructing Frankfurter {@code /rate} queries.
 */
public final class RateConfig {

    @NonNull
    private final String base;

    @Nullable
    private final LocalDate date;

    @NonNull
    private final String[] providers;

    @NonNull
    private final String quote;

    @SuppressWarnings("PMD.UseVarargs")
    private RateConfig(@NonNull String base,
                       @NonNull String quote,
                       @Nullable LocalDate date,
                       @NonNull String[] providers) {

        this.base = Objects.requireNonNull(base, Validation.formatNullMessage("base"));
        this.quote = Objects.requireNonNull(quote, Validation.formatNullMessage("quote"));
        this.date = date;
        this.providers = Objects.requireNonNull(providers, Validation.formatNullMessage("providers"));
    }

    /**
     * Applies this configuration to the given base URI.
     */
    @NonNull
    public URI applyTo(@NonNull URI baseUri) {
        Objects.requireNonNull(baseUri, Validation.formatNullMessage("baseUri"));

        var path = "rate/" + base + "/" + quote;
        var uri = baseUri.resolve(path);

        var params = new LinkedHashMap<String, String>();

        if (date != null) {
            params.put("date", date.toString());
        }

        if (providers.length > 0) {
            params.put("providers", String.join(",", providers));
        }

        if (params.isEmpty()) {
            return uri;
        }

        var sb = new StringBuilder(uri.toString()).append('?');
        var first = true;

        for (var e : params.entrySet()) {
            if (!first) {
                sb.append('&');
            }
            first = false;
            sb.append(e.getKey()).append('=').append(e.getValue());
        }

        return URI.create(sb.toString());
    }

    /**
     * Builder for {@link RateConfig}.
     */
    public static final class Builder {

        @Nullable
        private String base; // optional, but never null once set


        @Nullable
        private LocalDate date; // optional, but never null once set

        @NonNull
        private String[] providers = new String[0];

        @Nullable
        private String quote; // optional, but never null once set

        /**
         * Sets the base currency.
         */
        @NonNull
        public Builder base(@NonNull String base) {
            this.base = Validation.requireIsoCurrency(base, "base");
            return this;
        }

        /**
         * Builds the configuration.
         */
        @NonNull
        public RateConfig build() {
            if (base == null) {
                throw new IllegalArgumentException("base currency is required");
            }
            if (quote == null) {
                throw new IllegalArgumentException("quote currency is required");
            }

            return new RateConfig(base, quote, date, providers.clone());
        }

        /**
         * Sets an optional historical date.
         */
        @NonNull
        public Builder date(@NonNull LocalDate date) {
            this.date = Validation.requireSupportedDate(date, "date");

            return this;
        }

        /**
         * Sets optional provider filters.
         */
        @NonNull
        public Builder providers(@NonNull String... providers) {
            Validation.requireAllNonNull("providers", providers);
            this.providers = providers.clone();
            return this;
        }

        /**
         * Sets the quote currency.
         */
        @NonNull
        public Builder quote(@NonNull String quote) {
            this.quote = Validation.requireIsoCurrency(quote, "quote");
            return this;
        }
    }
}