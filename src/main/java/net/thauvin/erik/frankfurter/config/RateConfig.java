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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.thauvin.erik.frankfurter.internal.Validation;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Immutable configuration for constructing Frankfurter {@code /rate} queries.
 *
 * <p>Use {@code new RateConfig.Builder()} to create instances. Only the quote currency is required.
 * If base is omitted, the API defaults to {@code EUR}.</p>
 */
public final class RateConfig {

    @Nullable
    private final String base;

    @Nullable
    private final LocalDate date;

    @NonNull
    private final String[] providers;

    @NonNull
    private final String quote;

    @SuppressWarnings("PMD.UseVarargs")
    private RateConfig(@Nullable String base,
                       @NonNull String quote,
                       @Nullable LocalDate date,
                       @NonNull String[] providers) {
        this.base = base;
        this.quote = Objects.requireNonNull(quote, "quote");
        this.date = date;
        this.providers = providers.clone(); // defensive copy
    }

    @Override
    public String toString() {
        return "RateConfig{base=" + base + ", quote=" + quote + ", date=" + date
                + ", providers=" + Arrays.toString(providers) + '}';
    }

    /**
     * Applies this configuration to the given base URI.
     *
     * @param baseUri the base URI, e.g. {@code https://api.frankfurter.app/}
     * @return the fully constructed URI for the rate query
     * @throws IllegalArgumentException if the resulting URI is invalid
     */
    @NonNull
    @SuppressFBWarnings("EXS_EXCEPTION_SOFTENING_NO_CONSTRAINTS")
    public URI applyTo(@NonNull URI baseUri) {
        Objects.requireNonNull(baseUri, Validation.formatNullMessage("baseUri"));

        var params = new LinkedHashMap<String, String>();
        if (date != null) {
            params.put("date", date.toString()); // yyyy-MM-dd is URL-safe
        }
        if (providers.length > 0) {
            // Encode each provider separately, then join with literal comma
            var encodedProviders = Arrays.stream(providers)
                    .map(p -> URLEncoder.encode(p, StandardCharsets.UTF_8))
                    .collect(Collectors.joining(","));
            params.put("providers", encodedProviders);
        }

        try {
            var basePath = baseUri.getPath();
            if (basePath == null) {
                basePath = "/";
            } else if (!basePath.endsWith("/")) {
                basePath += "/";
            }

            var path = base != null
                    ? basePath + "rate/" + base + "/" + quote
                    : basePath + "rate/" + quote;

            if (params.isEmpty()) {
                return new URI(
                        baseUri.getScheme(),
                        baseUri.getAuthority(),
                        path,
                        null,
                        baseUri.getFragment()
                );
            }

            // Params already contain encoded values, so don't encode again
            var query = params.entrySet().stream()
                    .map(e -> e.getKey() + '=' + e.getValue())
                    .collect(Collectors.joining("&"));

            return new URI(
                    baseUri.getScheme(),
                    baseUri.getAuthority(),
                    path,
                    query,
                    baseUri.getFragment()
            );
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Failed to build URI for RateConfig", e);
        }
    }

    /**
     * Builder for {@link RateConfig}.
     */
    public static final class Builder {

        private String base;
        private LocalDate date;
        private String[] providers = new String[0];
        private String quote;

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
         * Builds the configuration.
         *
         * @return the immutable config
         * @throws IllegalStateException    if quote currency not set
         * @throws IllegalArgumentException if base and quote are the same
         */
        @NonNull
        public RateConfig build() {
            if (quote == null) {
                throw new IllegalStateException("quote currency is required");
            }
            if (base != null && base.equals(quote)) {
                throw new IllegalArgumentException("base and quote currencies must be different");
            }
            return new RateConfig(base, quote, date, providers);
        }

        /**
         * Sets an optional historical date.
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
         * Sets optional provider filters.
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

        /**ecb
         * Sets the quote currency. Required.
         *
         * @param quote 3-letter ISO 4217 currency code, e.g. "EUR"
         * @return this builder
         * @throws IllegalArgumentException if blank or not 3 letters
         */
        @NonNull
        public Builder quote(@NonNull String quote) {
            this.quote = Validation.requireIsoCurrency(quote, "quote");
            return this;
        }
    }
}
