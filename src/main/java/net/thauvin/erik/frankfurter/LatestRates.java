/*
 * LatestRates.java
 *
 * Copyright 2025 Erik C. Thauvin (erik@thauvin.net)
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

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.thauvin.erik.frankfurter.models.ExchangeRates;
import net.thauvin.erik.frankfurter.models.LocalDateAdapter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import static net.thauvin.erik.frankfurter.FrankfurterUtils.fetchUri;

/**
 * Represents the latest exchange rates based on a specific base currency, date, and symbols.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 0.9.0
 */
@SuppressWarnings("PMD.DataClass")
public class LatestRates {
    private final Double amount;
    private final String base;
    private final LocalDate date;
    private final List<String> symbols;

    /**
     * Constructs a new {@link LatestRates} instance using the specified Builder.
     *
     * @param builder the Builder instance containing the base currency, date, and symbols to initialize the
     *                {@link LatestRates} object
     */
    public LatestRates(Builder builder) {
        this.amount = builder.amount;
        this.base = builder.base;
        this.date = builder.date;
        this.symbols = new ArrayList<>(builder.symbols);
    }

    /**
     * Retrieves the amount of the latest exchange rate or time series.
     *
     * @return a {@code Double} representing the amount
     */
    public Double amount() {
        return amount;
    }

    /**
     * Retrieves the base currency for the current rates or time series.
     *
     * @return a string representing the base currency
     */
    public String base() {
        return base;
    }

    /**
     * Retrieves the date associated with the latest exchange rates.
     *
     * @return a {@link LocalDate} representing the date
     */
    public LocalDate date() {
        return date;
    }

    /**
     * Retrieves the latest exchange rates based on a specified base currency, date, and optional symbols.
     * <p>
     * This method constructs a request to the Frankfurter API, fetches the data, and parses the response
     * into a {@link ExchangeRates} object.
     *
     * @return an instance of {@link ExchangeRates} containing the base currency, date, and exchange rates
     * @throws IOException         if an error occurs during the API request or response handling
     * @throws URISyntaxException  if the URI syntax is invalid
     * @throws JsonSyntaxException if the JSON response from the API does not match the expected format
     */
    public ExchangeRates exchangeRates()
            throws IOException, URISyntaxException, JsonSyntaxException, InterruptedException {
        var gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .setPrettyPrinting()
                .create();

        String path;
        if (date != null) {
            path = date.toString();
        } else {
            path = "latest";
        }

        var query = new TreeMap<String, String>();
        if (amount != null && amount > 1.0) {
            query.put("amount", amount.toString());
        }

        if (!FrankfurterUtils.EUR.equals(base)) {
            query.put("base", base);
        }

        if (!symbols.isEmpty()) {
            query.put("symbols", String.join(",", symbols));
        }

        var uri = FrankfurterUtils.uriBuilder(path, query);

        return gson.fromJson(fetchUri(uri), ExchangeRates.class);
    }

    /**
     * Retrieves the list of currency symbols associated with the rates.
     *
     * @return a list of strings representing the currency symbols
     */
    public List<String> symbols() {
        return new ArrayList<>(symbols);
    }

    /**
     * Builder class to construct instances of the {@link LatestRates} class.
     * <p>
     * This class allows incremental configuration of the {@link LatestRates} object via method chaining.
     */
    public static class Builder {
        private final List<String> symbols = new ArrayList<>();
        private Double amount = 1.0;
        private String base = FrankfurterUtils.EUR;
        private LocalDate date;

        /**
         * Sets the amount for the builder.
         *
         * @param amount the amount value to be set, which can be a Double representing the desired amount
         * @return the current instance of the builder for method chaining
         */
        public Builder amount(Double amount) {
            this.amount = amount;
            return this;
        }

        /**
         * Sets the amount for the builder.
         *
         * @param amount the amount value to be set, which can be a Double representing the desired amount
         * @return the current instance of the builder for method chaining
         */
        public Builder amount(int amount) {
            this.amount = (double) amount;
            return this;
        }

        /**
         * Sets the base currency for the builder after formatting it to follow the required symbol format.
         *
         * @param base the base currency symbol to be set, which must consist of exactly three alphabetical characters
         * @return the current instance of the builder for method chaining
         * @throws IllegalArgumentException if the provided base symbol is not exactly three alphabetical characters
         */
        public Builder base(String base) {
            this.base = FrankfurterUtils.normalizeSymbol(base);
            return this;
        }

        /**
         * Builds and returns a new instance of {@link LatestRates} using the current state of the {@link Builder}.
         *
         * @return a new {@link LatestRates} instance configured with the base currency, date, and symbols set in the
         * Builder
         */
        public LatestRates build() {
            return new LatestRates(this);
        }

        /**
         * Sets the date for the builder after validating it to ensure it is not earlier than January 4, 1994.
         *
         * @param date the {@link LocalDate} to be set
         * @return the current instance of the builder for method chaining
         * @throws IllegalArgumentException if the provided date is earlier than January 4, 1994
         */
        public Builder date(LocalDate date) {
            FrankfurterUtils.validateDate(date);
            this.date = date;
            return this;
        }

        /**
         * Adds a collection of currency symbols to the builder after formatting each symbol to follow the required
         * format.
         *
         * @param symbols the collection of currency symbols to be added, each of which must consist of exactly three
         *                alphabetical characters
         * @return the current instance of the builder for method chaining
         * @throws IllegalArgumentException if any of the symbols in the collection is not exactly three alphabetical
         *                                  characters
         */
        public Builder symbols(Collection<String> symbols) {
            symbols.forEach(symbol -> this.symbols.add(FrankfurterUtils.normalizeSymbol(symbol)));
            return this;
        }

        /**
         * Adds one or more currency symbols to the builder after formatting each symbol to follow the required format.
         *
         * @param symbols one or more currency symbols to be added, each of which must consist of exactly three
         *                alphabetical characters
         * @return the current instance of the builder for method chaining
         * @throws IllegalArgumentException if any of the symbols provided is not exactly three alphabetical characters
         */
        public Builder symbols(String... symbols) {
            return symbols(List.of(symbols));
        }
    }
}
