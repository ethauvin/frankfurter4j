/*
 * RatesConfig.java
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

import com.uwyn.urlencoder.UrlEncoder;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.thauvin.erik.frankfurter.Validation;
import net.thauvin.erik.frankfurter.models.Group;

import java.net.URI;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable configuration for constructing Frankfurter {@code /rates} queries.
 */
public final class RatesConfig {

    @NonNull
    private final Map<String, String> params;

    @SuppressFBWarnings("DMC_DUBIOUS_MAP_COLLECTION")
    private RatesConfig(@NonNull Map<String, String> params) {
        this.params = Objects.requireNonNull(params, "params must not be null");
    }

    @NonNull
    private static String encode(@NonNull String s) {
        Objects.requireNonNull(s, "encode string must not be null");
        return UrlEncoder.encode(s);
    }

    /**
     * Applies this configuration to the given base URI.
     *
     * @param base the base API endpoint
     * @return a new URI with the query parameters appended
     */
    @NonNull
    public URI applyTo(@NonNull URI base) {
        Objects.requireNonNull(base, "base must not be null");

        if (params.isEmpty()) {
            return base;
        }

        var sb = new StringBuilder(base.toString()).append('?');
        var first = true;

        for (var e : params.entrySet()) {
            if (!first) {
                sb.append('&');
            }
            first = false;

            sb.append(encode(e.getKey()))
                    .append('=')
                    .append(encode(e.getValue()));
        }

        return URI.create(sb.toString());
    }

    /**
     * Builder for creating {@link RatesConfig} instances.
     */
    public static final class Builder {

        @Nullable
        private String base; // optional, but never null once set

        @Nullable
        private LocalDate date; // optional, but never null once set

        @Nullable
        private LocalDate from; // optional, but never null once set

        @Nullable
        private Group group; // optional, but never null once set

        @NonNull
        private String[] providers = new String[0];

        @NonNull
        private String[] quotes = new String[0];

        @Nullable
        private LocalDate to; // optional, but never null once set

        /**
         * Sets the base currency.
         */
        @NonNull
        public Builder base(@NonNull String base) {
            this.base = Validation.requireIsoCurrency(base, "base");
            return this;
        }

        /**
         * Builds and returns an immutable configuration.
         */
        @NonNull
        public RatesConfig build() {

            if (date != null && (from != null || to != null)) {
                throw new IllegalArgumentException("date is mutually exclusive with from and to");
            }

            if (from != null && to != null && to.isBefore(from)) {
                throw new IllegalArgumentException("to must be on or after from");
            }

            var params = new LinkedHashMap<String, String>();

            if (!Validation.isNullOrBlank(base)) {
                params.put("base", base);
            }

            if (quotes.length > 0) {
                params.put("quotes", String.join(",", quotes));
            }

            if (date != null) {
                params.put("date", date.toString());
            }

            if (from != null) {
                params.put("from", from.toString());
            }

            if (to != null) {
                params.put("to", to.toString());
            }

            if (group != null) {
                params.put("group", group.value());
            }

            if (providers.length > 0) {
                params.put("providers", String.join(",", providers));
            }

            return new RatesConfig(Map.copyOf(params));
        }

        /**
         * Sets a single date for the rate query.
         */
        @NonNull
        public Builder date(@NonNull LocalDate date) {
            this.date = Validation.requireSupportedDate(date, "date");
            return this;
        }

        /**
         * Sets the start of a date range query.
         */
        @NonNull
        public Builder from(@NonNull LocalDate from) {
            this.from = Validation.requireSupportedDate(from, "from");
            return this;
        }

        /**
         * Sets the grouping period.
         */
        @NonNull
        public Builder group(@NonNull Group group) {
            this.group = Objects.requireNonNull(group, "group must not be null");
            return this;
        }

        /**
         * Sets one or more providers.
         */
        @NonNull
        public Builder providers(@NonNull String... providers) {
            Validation.requireAllNonNull("providers", providers);
            this.providers = providers.clone();
            return this;
        }

        /**
         * Sets one or more quote currencies.
         */
        @NonNull
        public Builder quotes(@NonNull String... quotes) {
            Validation.requireAllNonNull("quotes", quotes);
            this.quotes = quotes.clone();
            return this;
        }

        /**
         * Sets the end of a date range query.
         */
        @NonNull
        public Builder to(@NonNull LocalDate to) {
            this.to = Validation.requireSupportedDate(to, "to");
            return this;
        }
    }
}