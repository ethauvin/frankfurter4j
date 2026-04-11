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

import net.thauvin.erik.frankfurter.Validation;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.time.LocalDate;
import java.util.LinkedHashMap;

/**
 * Immutable configuration for constructing Frankfurter {@code /rate} queries.
 *
 * <p>This type models the parameters accepted by the {@code /rate/{base}/{quote}}
 * endpoint, which returns a single exchange rate. Use {@link Builder} to create
 * instances.</p>
 *
 * <p>Supported options include:</p>
 * <ul>
 *   <li>a required base currency</li>
 *   <li>a required quote currency</li>
 *   <li>an optional historical date</li>
 *   <li>optional provider filtering</li>
 * </ul>
 *
 * <p>Date ranges are not supported for this endpoint.</p>
 */
@NullMarked
public final class RateConfig {

    private final String base;
    private final @Nullable LocalDate date;
    private final String[] providers;
    private final String quote;

    /**
     * Creates a new immutable configuration.
     *
     * @param base      the base currency
     * @param quote     the quote currency
     * @param date      the optional historical date
     * @param providers the optional provider filters
     */
    @SuppressWarnings("PMD.UseVarargs")
    private RateConfig(String base,
                       String quote,
                       @Nullable LocalDate date,
                       String[] providers) {
        this.base = base;
        this.quote = quote;
        this.date = date;
        this.providers = providers;
    }

    /**
     * Applies this configuration to the given base URI.
     *
     * <p>The resulting URI has the form:</p>
     *
     * <pre>{@code
     *   /rate/{base}/{quote}?date=YYYY-MM-DD&providers=A,B,C
     * }</pre>
     *
     * @param baseUri the base API URI
     * @return the fully constructed request URI
     */
    public URI applyTo(URI baseUri) {
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
     *
     * <p>Instances are mutable and not thread‑safe. Use {@link #build()} to
     * create an immutable configuration.</p>
     */
    public static final class Builder {

        private @Nullable String base;
        private @Nullable LocalDate date;
        private String[] providers = new String[0];
        private @Nullable String quote;

        /**
         * Sets the base currency.
         *
         * @param base the ISO 4217 base currency code
         * @return this builder
         * @throws IllegalArgumentException if the code is invalid
         */
        public Builder base(String base) {
            Validation.requireIsoCurrency(base, "base");
            this.base = base;
            return this;
        }

        /**
         * Builds the configuration.
         *
         * @return a new {@link RateConfig}
         * @throws IllegalArgumentException if base or quote is missing
         */
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
         *
         * @param date the date to query, or {@code null} for the latest rate
         * @return this builder
         * @throws IllegalArgumentException if the date is earlier than the minimum supported date
         */
        public Builder date(@Nullable LocalDate date) {
            if (date != null) {
                Validation.requireSupportedDate(date, "date");
            }
            this.date = date;
            return this;
        }

        /**
         * Sets optional provider filters.
         *
         * @param providers provider codes such as {@code "ECB"}
         * @return this builder
         */
        public Builder providers(String... providers) {
            //noinspection ConstantValue
            this.providers = providers == null ? new String[0] : providers.clone();
            return this;
        }

        /**
         * Sets the quote currency.
         *
         * @param quote the ISO 4217 quote currency code
         * @return this builder
         * @throws IllegalArgumentException if the code is invalid
         */
        public Builder quote(String quote) {
            Validation.requireIsoCurrency(quote, "quote");
            this.quote = quote;
            return this;
        }
    }
}
