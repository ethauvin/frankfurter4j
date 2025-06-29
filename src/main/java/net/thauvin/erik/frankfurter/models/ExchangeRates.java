/*
 * ExchangeRates.java
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

package net.thauvin.erik.frankfurter.models;

import net.thauvin.erik.frankfurter.FrankfurterUtils;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

/**
 * Represents the latest exchange rates for a base currency.
 */
public class ExchangeRates {
    private final Double amount;
    private final String base;
    private final LocalDate date;
    private final Map<String, Double> rates;

    /**
     * Constructs an instance of the ExchangeRates class.
     *
     * @param amount The amount of the base currency
     * @param base   The base currency
     * @param date   The date of the exchange rates
     * @param rates  The exchange rates
     */
    ExchangeRates(Double amount, String base, LocalDate date, Map<String, Double> rates) {
        this.amount = amount;
        this.base = base;
        this.date = date;
        this.rates = rates;
    }

    /**
     * Retrieves the amount of the base currency.
     *
     * @return the amount of the base currency
     */
    public Double getAmount() {
        return amount;
    }

    /**
     * Retrieves the base currency.
     *
     * @return the base currency
     */
    public String getBase() {
        return base;
    }

    /**
     * Retrieves the date of the exchange rates.
     *
     * @return the date of the exchange rates
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Retrieves the exchange rate for the specified currency symbol.
     *
     * @param symbol The currency symbol
     */
    public Double getRateFor(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return null;
        }
        return rates.get(FrankfurterUtils.normalizeSymbol(symbol));
    }

    /**
     * Retrieves the exchange rates for various currencies.
     *
     * @return a map where the keys are currency symbols (e.g., "USD", "EUR") and the values are
     * the corresponding exchange rates against the base currency.
     */
    public Map<String, Double> getRates() {
        return rates;
    }

    /**
     * Retrieves the set of all currency symbols available in the exchange rates.
     *
     * @return a set of strings representing the currency symbols
     */
    public Set<String> getSymbols() {
        return rates.keySet();
    }

    /**
     * Checks if the exchange rates contain the specified currency symbol.
     *
     * @param symbol The currency symbol to check for
     * @return {@code true} if the exchange rates contain the symbol, {@code false} otherwise
     */
    public boolean hasRateFor(String symbol) {
        return rates.containsKey(symbol);
    }

    @Override
    public String toString() {
        return "ExchangeRates{" +
                "amount=" + amount +
                ", base='" + base + '\'' +
                ", date='" + date + '\'' +
                ", rates=" + rates +
                '}';
    }
}
