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
import net.thauvin.erik.frankfurter.internal.Validation;
import net.thauvin.erik.frankfurter.models.CurrencyCode;

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
 *
 * <p>This class is thread-safe. All state is immutable.</p>
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public final class RateConfig {

    private static final String QUOTE = "quote";

    @Nullable
    private final String base;

    @Nullable
    private final LocalDate date;

    @NonNull
    private final String[] providers;

    @NonNull
    private final String quote;

    /**
     * Creates a new immutable configuration.
     *
     * @param base      the base currency ISO code, or {@code null} to use API default
     * @param quote     the quote currency ISO code (must not be {@code null})
     * @param date      the historical date, or {@code null} for latest
     * @param providers the provider IDs, or empty array for all
     * @throws NullPointerException if {@code quote} is {@code null}
     */
    @SuppressWarnings("PMD.UseVarargs")
    private RateConfig(@Nullable String base,
                       @NonNull String quote,
                       @Nullable LocalDate date,
                       @NonNull String[] providers) {
        this.base = base;
        this.quote = Objects.requireNonNull(quote, QUOTE);
        this.date = date;
        this.providers = providers.clone(); // defensive copy
    }

    /**
     * Returns a hash code value for this configuration.
     *
     * <p>The hash code is computed from the base currency, quote currency, date, and
     * providers. It is consistent with {@link #equals(Object)}: equal objects have
     * equal hash codes.</p>
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(base, quote, date);
        result = 31 * result + Arrays.hashCode(providers);
        return result;
    }

    /**
     * Compares this configuration to the specified object for equality.
     *
     * <p>Two {@code RateConfig} instances are equal if they have the same base currency,
     * quote currency, date, and providers. The comparison is order-sensitive for
     * providers and case-sensitive for currency codes.</p>
     *
     * @param o the object to compare with
     * @return {@code true} if the objects are equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RateConfig that)) {
            return false;
        }
        return Objects.equals(base, that.base)
                && Objects.equals(quote, that.quote)
                && Objects.equals(date, that.date)
                && Arrays.equals(providers, that.providers);
    }

    /**
     * Returns a string representation of this configuration.
     *
     * @return a string describing the configuration parameters
     */
    @Override
    public String toString() {
        return "RateConfig{base=" + base + ", quote=" + quote + ", date=" + date
                + ", providers=" + Arrays.toString(providers) + '}';
    }

    /**
     * Applies this configuration to the given base URI.
     *
     * <p>Constructs a URI for the {@code /rate/{base}/{quote}} endpoint with optional
     * query parameters for {@code date} and {@code providers}.</p>
     *
     * @param baseUri the base URI, e.g. {@code https://api.frankfurter.app/}
     * @return the fully constructed URI for the rate query
     * @throws NullPointerException     if {@code baseUri} is {@code null}
     * @throws IllegalArgumentException if the resulting URI is invalid
     */
    @NonNull
    public URI applyTo(@NonNull URI baseUri) {
        Objects.requireNonNull(baseUri, Validation.formatNullMessage("baseUri"));

        var params = new LinkedHashMap<String, String>();
        if (date != null) {
            params.put("date", date.toString()); // yyyy-MM-dd is URL-safe
        }
        if (providers.length > 0) {
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
     *
     * <p>All setter methods return {@code this} for method chaining. Call {@link #build()}
     * to create the immutable configuration.</p>
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
         * @param base the base currency
         * @return this builder
         * @throws NullPointerException if {@code base} is {@code null}
         */
        @NonNull
        public Builder base(@NonNull CurrencyCode base) {
            this.base = Objects.requireNonNull(base, Validation.formatNullMessage("base")).getCode();
            return this;
        }

        /**
         * Sets the base currency. Optional.
         *
         * <p>If not set, the Frankfurter API defaults to {@code EUR}.</p>
         *
         * @param base 3-letter ISO 4217 currency code, e.g. "USD"
         * @return this builder
         * @throws NullPointerException     if {@code base} is {@code null}
         * @throws IllegalArgumentException if blank or not 3 letters
         */
        @NonNull
        public Builder base(@NonNull String base) {
            this.base = Validation.requireIsoCurrency("base", base);
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
            if (base != null && base.equalsIgnoreCase(quote)) {
                throw new IllegalArgumentException("base and quote currencies must be different");
            }
            return new RateConfig(base, quote, date, providers);
        }

        /**
         * Sets an optional historical date.
         *
         * @param date the date to query, must not be before 1994-01-04
         * @return this builder
         * @throws NullPointerException     if {@code date} is {@code null}
         * @throws IllegalArgumentException if date is earlier than the minimum supported
         */
        @NonNull
        public Builder date(@NonNull LocalDate date) {
            this.date = Validation.requireSupportedDate("date", date);
            return this;
        }

        /**
         * Sets optional provider filters.
         *
         * <p>Blank entries are ignored. Duplicates are removed. The array is defensively copied.</p>
         *
         * @param providers provider IDs, e.g. "ECB", "BANXICO"
         * @return this builder
         * @throws NullPointerException if array or any element is {@code null}
         */
        @NonNull
        public Builder providers(@NonNull String... providers) {
            this.providers = Validation.requireNonBlankDistinct("providers", providers);
            return this;
        }

        /**
         * Sets the quote currency. Required.
         *
         * @param quote the quote currency
         * @return this builder
         * @throws NullPointerException if {@code quote} is {@code null}
         */
        @NonNull
        public Builder quote(@NonNull CurrencyCode quote) {
            this.quote = Objects.requireNonNull(quote, Validation.formatNullMessage(QUOTE)).getCode();
            return this;
        }

        /**
         * Sets the quote currency. Required.
         *
         * @param quote 3-letter ISO 4217 currency code, e.g. "EUR"
         * @return this builder
         * @throws NullPointerException     if {@code quote} is {@code null}
         * @throws IllegalArgumentException if blank or not 3 letters
         */
        @NonNull
        public Builder quote(@NonNull String quote) {
            this.quote = Validation.requireIsoCurrency(QUOTE, quote);
            return this;
        }
    }
}

