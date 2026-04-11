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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.thauvin.erik.frankfurter.Validation;
import net.thauvin.erik.frankfurter.models.Group;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable configuration for constructing Frankfurter {@code /rates} queries.
 *
 * <p>This type models the query parameters accepted by the Frankfurter API.
 * Use {@link Builder} to construct instances.</p>
 *
 * <p>The API supports three date modes:</p>
 * <ul>
 *   <li><strong>Latest</strong> — no date parameters</li>
 *   <li><strong>Single date</strong> — {@code date=YYYY-MM-DD}</li>
 *   <li><strong>Date range</strong> — {@code from=YYYY-MM-DD} and {@code to=YYYY-MM-DD}</li>
 * </ul>
 *
 * <p>All dates are represented as {@link LocalDate} and must not be earlier
 * than {@code 1994-01-04}, the earliest supported date.</p>
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
@NullMarked
public final class RatesConfig {

    private final Map<String, String> params;

    @SuppressFBWarnings("DMC_DUBIOUS_MAP_COLLECTION")
    private RatesConfig(Map<String, String> params) {
        this.params = params;
    }

    private static String encode(String s) {
        return UrlEncoder.encode(s);
    }

    /**
     * Applies this configuration to the given base URI.
     *
     * @param base the base API endpoint
     * @return a new URI with the query parameters appended
     */
    public URI applyTo(URI base) {
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
     *
     * @since 1.0
     */
    @NullMarked
    public static final class Builder {

        private @Nullable String base;
        private @Nullable LocalDate date;
        private @Nullable LocalDate from;
        private @Nullable Group group;
        private String[] providers = new String[0];
        private String[] quotes = new String[0];
        private @Nullable LocalDate to;


        /**
         * Sets the base currency (e.g. {@code "EUR"}).
         *
         * @param base the ISO 4217 base currency code
         * @return this builder instance
         */
        public Builder base(String base) {
            //noinspection ConstantValue
            if (base != null) {
                Validation.requireIsoCurrency(base, "base");
                this.base = base;
            }
            return this;
        }

        /**
         * Builds and returns an immutable configuration.
         *
         * @return a new {@link RatesConfig}
         */
        public RatesConfig build() {

            // date vs range exclusivity
            if (date != null && (from != null || to != null)) {
                throw new IllegalArgumentException("date is mutually exclusive with from and to");
            }

            // chronological order
            if (from != null && to != null && to.isBefore(from)) {
                throw new IllegalArgumentException("to must be on or after from");
            }

            var params = new LinkedHashMap<String, String>();

            if (base != null && !base.isBlank()) {
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
         *
         * @param date the date to query
         * @return this builder instance
         */
        public Builder date(LocalDate date) {
            Validation.requireSupportedDate(date, "date");
            this.date = date;
            return this;
        }

        /**
         * Sets the start of a date range query.
         *
         * @param from the first date in the range
         * @return this builder instance
         */
        public Builder from(LocalDate from) {
            Validation.requireSupportedDate(from, "from");
            this.from = from;
            return this;
        }

        /**
         * Sets the grouping period for downsampled results.
         *
         * @param group the grouping value, or {@code null} to omit
         * @return this builder instance
         */
        public Builder group(@Nullable Group group) {
            this.group = group;
            return this;
        }

        /**
         * Sets one or more providers (e.g. {@code "ECB"}, {@code "BAM" })
         *
         * @param providers one or more providers
         * @return this builder instance
         */
        public Builder providers(String... providers) {
            //noinspection ConstantValue
            if (providers != null) {
                if (providers.length == 0) {
                    throw new IllegalArgumentException("At least one providers must be specified");
                }
                this.providers = providers.clone();
            }
            return this;
        }

        /**
         * Sets one or more quote currencies (e.g. {@code "USD"}, {@code "JPY"}).
         *
         * @param quotes one or more ISO 4217 currency codes
         * @return this builder instance
         */
        public Builder quotes(String... quotes) {
            //noinspection ConstantValue
            if (quotes != null) {
                if (quotes.length == 0) {
                    throw new IllegalArgumentException("At least one quote currency must be specified");
                }
                for (var q : quotes) {
                    Validation.requireIsoCurrency(q, "quote");
                }
                this.quotes = quotes.clone();
            }
            return this;
        }

        /**
         * Sets the end of a date range query.
         *
         * @param to the last date in the range
         * @return this builder instance
         */
        public Builder to(LocalDate to) {
            Validation.requireSupportedDate(to, "to");
            this.to = to;
            return this;
        }
    }
}