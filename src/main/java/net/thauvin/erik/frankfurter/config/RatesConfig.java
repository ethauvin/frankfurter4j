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
import net.thauvin.erik.frankfurter.internal.Validation;
import net.thauvin.erik.frankfurter.models.Group;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Immutable configuration for constructing Frankfurter {@code /rates} queries.
 */
public final class RatesConfig {

    @NonNull
    private final Map<String, String> params;

    private RatesConfig(@NonNull Map<String, String> params) {
        this.params = params; // already unmodifiable from Map.copyOf
    }

    @Override
    public String toString() {
        return "RatesConfig" + params;
    }

    /**
     * URL-encodes a string using UTF-8.
     *
     * @param s the string to encode
     * @return the encoded string
     */
    @NonNull
    private static String encode(@NonNull String s) {
        return UrlEncoder.encode(s);
    }

    /**
     * Applies this configuration to the given base URI.
     *
     * @param baseUri the base API endpoint
     * @return a new URI with the query parameters appended
     * @throws IllegalArgumentException if the URI cannot be built
     */
    @NonNull
    public URI applyTo(@NonNull URI baseUri) {
        Objects.requireNonNull(baseUri, Validation.formatNullMessage("baseUri"));

        if (params.isEmpty()) {
            return baseUri;
        }

        var sb = new StringBuilder();
        var first = true;

        for (var e : params.entrySet()) {
            if (!first) {
                sb.append('&');
            }
            first = false;

            sb.append(encode(e.getKey()))
                    .append('=')
                    .append(e.getValue()); // values already encoded in build()
        }

        try {
            return new URI(
                    baseUri.getScheme(),
                    baseUri.getAuthority(),
                    baseUri.getPath(),
                    sb.toString(),
                    baseUri.getFragment()
            );
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Failed to build URI for RatesConfig", e);
        }
    }

    /**
     * Builder for creating {@link RatesConfig} instances.
     */
    public static final class Builder {

        private String base;
        private LocalDate date;
        private LocalDate from;
        private Group group;
        private String[] providers = new String[0];
        private String[] quotes = new String[0];
        private LocalDate to;

        /**
         * Sets the base currency. Optional.
         *
         * <p>If not set, the Frankfurter API defaults to {@code EUR}.</p>
         *
         * @param base 3-letter ISO 4217 currency code, e.g. "USD"
         * @return this builder
         * @throws IllegalArgumentException if blank or not 3 letters
         */
        @NonNull
        public Builder base(@NonNull String base) {
            this.base = Validation.requireIsoCurrency(base, "base");
            return this;
        }

        /**
         * Builds and returns an immutable configuration.
         *
         * @return the immutable config
         * @throws IllegalArgumentException if date is combined with from/to,
         *                                  or if to is before from
         * @throws IllegalStateException    if group is set without a date or date range
         */
        @NonNull
        public RatesConfig build() {
            if (date != null && (from != null || to != null)) {
                throw new IllegalArgumentException("date is mutually exclusive with from and to");
            }
            if (from != null && to != null && to.isBefore(from)) {
                throw new IllegalArgumentException("to must be on or after from");
            }
            if (group != null && from == null && to == null && date == null) {
                throw new IllegalStateException("group requires a date or date range");
            }

            var params = new LinkedHashMap<String, String>();

            if (base != null) {
                params.put("base", base); // ISO codes are URL-safe, no encode needed
            }
            if (quotes.length > 0) {
                // Encode each quote, then join with literal comma
                var encodedQuotes = Arrays.stream(quotes)
                        .map(UrlEncoder::encode)
                        .collect(Collectors.joining(","));
                params.put("quotes", encodedQuotes);
            }
            if (date != null) {
                params.put("date", date.toString()); // yyyy-MM-dd is URL-safe
            }
            if (from != null) {
                params.put("from", from.toString()); // yyyy-MM-dd is URL-safe
            }
            if (to != null) {
                params.put("to", to.toString()); // yyyy-MM-dd is URL-safe
            }
            if (group != null) {
                params.put("group", group.value()); // enum values are URL-safe
            }
            if (providers.length > 0) {
                // Encode each provider, then join with literal comma
                var encodedProviders = Arrays.stream(providers)
                        .map(UrlEncoder::encode)
                        .collect(Collectors.joining(","));
                params.put("providers", encodedProviders);
            }

            return new RatesConfig(Map.copyOf(params));
        }

        /**
         * Sets a single date for the rate query.
         *
         * @param date the date to query, must not be before 1994-01-04
         * @return this builder
         * @throws IllegalArgumentException if date is earlier than the minimum supported
         */
        @NonNull
        public Builder date(@NonNull LocalDate date) {
            this.date = Validation.requireSupportedDate(date, "date");
            return this;
        }

        /**
         * Sets the start of a date range query.
         *
         * @param from the start date, must not be before 1994-01-04
         * @return this builder
         * @throws IllegalArgumentException if date is earlier than the minimum supported
         */
        @NonNull
        public Builder from(@NonNull LocalDate from) {
            this.from = Validation.requireSupportedDate(from, "from");
            return this;
        }

        /**
         * Sets the grouping period.
         *
         * @param group the grouping, e.g. DAY, MONTH, YEAR
         * @return this builder
         */
        @NonNull
        public Builder group(@NonNull Group group) {
            this.group = Objects.requireNonNull(group, Validation.formatNullMessage("group"));
            return this;
        }

        /**
         * Sets one or more providers.
         *
         * <p>Blank entries are ignored. Duplicates are removed.</p>
         *
         * @param providers provider IDs, e.g. "ECB", "BANXICO"
         * @return this builder
         * @throws NullPointerException if array or any element is null
         */
        @NonNull
        public Builder providers(@NonNull String... providers) {
            this.providers = Validation.requireNonBlankDistinct("providers", providers);
            return this;
        }

        /**
         * Sets one or more quote currencies.
         *
         * <p>Blank entries are ignored. Duplicates are removed.</p>
         *
         * @param quotes 3-letter ISO 4217 currency codes, e.g. "USD", "GBP"
         * @return this builder
         * @throws NullPointerException     if array or any element is null
         * @throws IllegalArgumentException if any quote is not 3 letters
         */
        @NonNull
        public Builder quotes(@NonNull String... quotes) {
            this.quotes = Validation.requireIsoCurrencyArray("quotes", quotes);
            return this;
        }

        /**
         * Sets the end of a date range query.
         *
         * @param to the end date, must not be before 1994-01-04
         * @return this builder
         * @throws IllegalArgumentException if date is earlier than the minimum supported
         */
        @NonNull
        public Builder to(@NonNull LocalDate to) {
            this.to = Validation.requireSupportedDate(to, "to");
            return this;
        }
    }
}
