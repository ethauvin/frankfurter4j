/*
 * TimeSeries.java
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
import net.thauvin.erik.frankfurter.models.LocalDateAdapter;
import net.thauvin.erik.frankfurter.models.SeriesRates;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.TreeMap;

/**
 * Represents a time series of currency exchange rate data for a specified base currency over a defined date range.
 * <p>
 * This class provides functionality to retrieve historical exchange rates using a builder pattern for flexible
 * configuration of parameters.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 0.9.0
 */
@SuppressWarnings("PMD.DataClass")
public class TimeSeries {
    private final Double amount;
    private final String base;
    private final LocalDate endDate;
    private final LocalDate startDate;
    private final Collection<String> symbols;

    /**
     * Constructs a new instance of the TimeSeries class using the specified builder object.
     *
     * @param builder the Builder instance containing the configuration for the TimeSeries object
     */
    public TimeSeries(Builder builder) {
        this.amount = builder.amount;
        this.symbols = builder.symbols;
        this.base = builder.base;
        this.endDate = builder.endDate;
        this.startDate = builder.startDate;
    }

    /**
     * Retrieves the amount associated with the current time series.
     *
     * @return a {@code Double} representing the value of the amount field
     */
    public Double amount() {
        return amount;
    }

    /**
     * Retrieves the base currency for the current time series.
     *
     * @return a string representing the base currency
     */
    public String base() {
        return base;
    }

    /**
     * Retrieves the end date associated with the time series.
     *
     * @return The end date as a {@link LocalDate} object
     */
    public LocalDate endDate() {
        return endDate;
    }

    /**
     * Retrieves the periodic exchange rates for a time series based on the specified input parameters.
     * <p>
     * This method constructs a time series request using defined start and end dates, base currency,
     * symbols, and amount, and fetches the corresponding data.
     *
     * @return a {@link SeriesRates} object containing the exchange rate information for the specified time range and
     * conditions.
     * @throws IOException              if an I/O error occurs during data retrieval
     * @throws URISyntaxException       if the generated URI for the data request is invalid
     * @throws IllegalArgumentException if required parameters such as the start date are not set, or
     *                                  if the end date is before the start date
     * @throws JsonSyntaxException      if the JSON response from the API does not match the expected format
     */
    public SeriesRates periodicRates()
            throws IOException, URISyntaxException, JsonSyntaxException, InterruptedException {
        var gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .setFieldNamingPolicy(com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .setPrettyPrinting()
                .create();

        var dateRangePath = new StringBuilder();

        if (startDate != null) {
            dateRangePath.append(startDate);
        } else {
            throw new IllegalArgumentException("The start date is required.");
        }

        dateRangePath.append("..");

        if (endDate != null) {
            if (!endDate.isBefore(startDate)) {
                dateRangePath.append(endDate);
            } else {
                throw new IllegalArgumentException("The end date must be on or after the start date.");

            }
        }

        var query = new TreeMap<String, String>();

        if (amount != null && amount > 1.0) {
            query.put("amount", String.valueOf(amount));
        }

        if (!FrankfurterUtils.EUR.equals(base)) {
            query.put("base", base);
        }

        if (!symbols.isEmpty()) {
            query.put("symbols", String.join(",", symbols));
        }

        var uri = FrankfurterUtils.uriBuilder(dateRangePath.toString(), query);
        return gson.fromJson(FrankfurterUtils.fetchUri(uri), SeriesRates.class);
    }

    /**
     * Retrieves the start date associated with the time series.
     *
     * @return The start date as a {@link LocalDate} object
     */
    public LocalDate startDate() {
        return startDate;
    }

    /**
     * Retrieves the collection of currency symbols associated with the time series.
     *
     * @return a collection containing the currency symbols
     */
    public Collection<String> symbols() {
        return symbols;
    }

    /**
     * Builder class to construct instances of the {@link TimeSeries} class.
     * <p>
     * This class allows incremental configuration of the {@link TimeSeries} object via method chaining.
     */
    public static class Builder {
        private final Collection<String> symbols = new ArrayList<>();
        private Double amount = 1.0;
        private String base = FrankfurterUtils.EUR;
        private LocalDate endDate;
        private LocalDate startDate;

        /**
         * Sets the amount for the builder.
         *
         * @param amount the monetary amount to be set; can be null
         * @return the current {@code Builder} instance for method chaining
         */
        public Builder amount(Double amount) {
            this.amount = amount;
            return this;
        }

        /**
         * Sets the amount for the builder.
         *
         * @param amount the monetary amount to be set; can be null
         * @return the current {@code Builder} instance for method chaining
         */
        public Builder amount(int amount) {
            this.amount = (double) amount;
            return this;
        }


        /**
         * Sets the base currency symbol for the builder.
         * <p>
         * The provided symbol will be formatted to uppercase if it is a valid 3-letter currency symbol.
         *
         * @param base the base currency symbol to be set; must consist of exactly three alphabetical characters
         * @return the current {@code Builder} instance for method chaining
         * @throws IllegalArgumentException if the provided symbol is not exactly three alphabetical characters
         */
        public Builder base(String base) {
            this.base = FrankfurterUtils.normalizeSymbol(base);
            return this;
        }

        /**
         * Builds and returns a new instance of the {@code TimeSeries} class using the current state of the Builder.
         *
         * @return a new {@code TimeSeries} instance configured according to the properties set in the Builder
         */
        public TimeSeries build() {
            return new TimeSeries(this);
        }

        /**
         * Sets the end date for the builder using a {@code LocalDate} object.
         * <p>
         * The provided date must be on or after January 4, 1994.
         *
         * @param endDate the {@code LocalDate} object representing the end date to be set
         * @return the current {@code Builder} instance for method chaining
         * @throws DateTimeParseException   if the provided {@code LocalDate} is invalid or cannot be processed
         * @throws IllegalArgumentException if the provided {@code LocalDate} is earlier than January 4, 1994
         */
        public Builder endDate(LocalDate endDate) {
            FrankfurterUtils.validateDate(endDate);
            this.endDate = endDate;
            return this;
        }

        /**
         * Sets the start date for the builder using a {@code LocalDate} object.
         * <p>
         * The provided date must not be earlier than January 4, 1994.
         *
         * @param startDate the {@code LocalDate} object representing the start date to be set
         * @return the current {@code Builder} instance for method chaining
         * @throws DateTimeParseException   if the provided {@code LocalDate} is invalid or cannot be processed
         * @throws IllegalArgumentException if the provided {@code LocalDate} is earlier than January 4, 1994
         */
        public Builder startDate(LocalDate startDate) {
            FrankfurterUtils.validateDate(startDate);
            this.startDate = startDate;
            return this;
        }

        /**
         * Sets the currency symbols for the builder using a variable number of string arguments.
         * <p>
         * Each provided symbol will be formatted and added to the builder's symbols collection.
         *
         * @param symbols an array of strings representing currency symbols to be added
         * @return the current {@code Builder} instance for method chaining
         */
        public Builder symbols(String... symbols) {
            Arrays.stream(symbols).forEach(symbol -> this.symbols.add(FrankfurterUtils.normalizeSymbol(symbol)));
            return this;
        }

        /**
         * Sets the currency symbols for the builder using a collection of string arguments.
         * <p>
         * Each provided symbol will be formatted to uppercase and added to the builder's symbols collection.
         *
         * @param symbols a collection of strings representing currency symbols to be added
         * @return the current {@code Builder} instance for method chaining
         * @throws IllegalArgumentException if any symbol in the collection is not exactly three alphabetical characters
         */
        public Builder symbols(Collection<String> symbols) {
            symbols.forEach(symbol -> this.symbols.add(FrankfurterUtils.normalizeSymbol(symbol)));
            return this;
        }
    }
}
