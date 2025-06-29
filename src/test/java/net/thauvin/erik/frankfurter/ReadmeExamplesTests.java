/*
 * ReadmeExamplesTests.java
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@ExtendWith(BeforeAllTests.class)
class ReadmeExamplesTests {
    @Test
    void currenciesExample() throws IOException {
        var currencies = AvailableCurrencies.getCurrencies();

        assertEquals(currencies.get("USD"), currencies.getFullNameFor("usd"));
        assertEquals("EUR", currencies.getSymbolFor("euro"));
        assertEquals("JPY", currencies.getSymbolFor(Pattern.compile(".*Japan.*")));

    }

    @Test
    @SuppressWarnings("PMD.SystemPrintln")
    void currencyConversionExample() throws IOException, URISyntaxException {
        var latestRates = new LatestRates.Builder()
                .amount(10)
                .base("USD")
                .symbols("EUR")
                .build();
        var exchangeRates = latestRates.getExchangeRates();
        var euro = exchangeRates.getRateFor("EUR");

        System.out.println("$10 = €" + euro);

        System.out.println(FrankfurterUtils.formatCurrency(exchangeRates.getBase(), exchangeRates.getAmount())
                + " = " + FrankfurterUtils.formatCurrency("EUR", euro));

        assertTrue(euro > 0);
    }

    @Test
    void formatCurrency() {
        assertEquals("$100.00", FrankfurterUtils.formatCurrency("USD", 100.0));
        assertEquals("1.234,567 €", FrankfurterUtils.formatCurrency("EUR", 1234.567));
        assertEquals("1.234,57 €", FrankfurterUtils.formatCurrency("EUR", 1234.567, true));
    }

    @Test
    void historicalRatesExamples() throws IOException, URISyntaxException {
        var date = LocalDate.of(1999, 1, 4);
        var latestRates = new LatestRates.Builder()
                .date(date)
                .build();
        var ratesDate = latestRates.getExchangeRates();
        assertEquals(date, ratesDate.getDate());
    }

    @Test
    void historicalRatesExamplesWithBaseAndCurrencies() throws IOException, URISyntaxException {
        var latestRates = new LatestRates.Builder()
                .base("USD")
                .date(LocalDate.of(1999, 1, 4))
                .symbols("EUR")
                .build();

        var ratesDate = latestRates.getExchangeRates();
        assertTrue(ratesDate.hasRateFor("EUR"));
    }

    @Test
    void latestRatesExample() throws IOException, URISyntaxException {
        var latestRates = new LatestRates.Builder().build();
        var exchangeRates = latestRates.getExchangeRates();

        if (exchangeRates.hasRateFor("JPY")) {
            var jpy = exchangeRates.getRateFor("JPY");
            assertTrue(jpy > 0);
        } else {
            fail("JPY not found");
        }
    }

    @Test
    void latestRatesExampleWithBase() throws IOException, URISyntaxException {
        var latestRates = new LatestRates.Builder()
                .base("USD")
                .build();

        var ratesDate = latestRates.getExchangeRates();
        assertTrue(ratesDate.hasRateFor("CHF"));
    }

    @Test
    @SuppressWarnings("PMD.SystemPrintln")
    void periodicRatesExample() throws IOException, URISyntaxException {
        var timeSeries = new TimeSeries.Builder()
                .startDate(LocalDate.of(2000, 1, 1))
                .endDate(LocalDate.of(2000, 12, 31))
                .build();
        var periodicRates = timeSeries.getPeriodicRates();

        var firstMarketDay = LocalDate.of(2000, 1, 4);
        if (periodicRates.hasRatesFor(firstMarketDay)) {
            var rates = periodicRates.getRatesFor(firstMarketDay);
            var yen = periodicRates.getRateFor(firstMarketDay, "JPY");
            assertTrue(yen > 0);
            if (rates.containsKey("USD")) {
                var usd = rates.get("USD");
                assertTrue(usd > 0);
            } else {
                fail("USD not found");
            }
        } else {
            fail("2000-01-04 not found");
        }

        periodicRates.getDates().forEach(date -> {
            System.out.println("Rates for " + date);
            periodicRates.getRatesFor(date).forEach((symbol, rate) -> {
                System.out.println("    " + symbol + ": " + rate);
                assertTrue(rate > 0,
                        "Value should be greater than 0 for date: " + date + ", key: " + symbol);
            });
        });
    }

    @Test
    void periodicRatesExampleToPresent() throws IOException, URISyntaxException {
        var timeSeries = new TimeSeries.Builder()
                .startDate(LocalDate.of(2025, 1, 1))
                .build();

        var periodicRates = timeSeries.getPeriodicRates();
        assertTrue(periodicRates.hasRatesFor(LocalDate.of(2025, 1, 3)));
    }

    @Test
    void periodicRatesExampleWithCurrencies() throws IOException, URISyntaxException {
        var timeSeries = new TimeSeries.Builder()
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.now())
                .symbols("USD")
                .build();

        var periodicRates = timeSeries.getPeriodicRates();
        assertTrue(periodicRates.getRatesFor(LocalDate.of(2025, 1, 3)).containsKey("USD"));
    }

    @Test
    void tldrExample() throws IOException, URISyntaxException {
        var latestRates = new LatestRates.Builder()
                .amount(100.0)
                .base("USD")
                .symbols("EUR", "GBP")
                .build();
        var exchangeRates = latestRates.getExchangeRates();
        var euro = exchangeRates.getRateFor("EUR");
        var britishPound = exchangeRates.getRateFor("GBP");

        assertTrue(euro > 0);
        assertTrue(britishPound > 0);
    }

    @Test
    void workingDaysExamples() {
        var workingDays = FrankfurterUtils.workingDays(LocalDate.of(2021, 1, 1),
                LocalDate.of(2021, 1, 31));

        var firstWorkingDay = workingDays.get(0); // 2021-01-04
        var lastWorkingDay = workingDays.get(workingDays.size() - 1); // 2025-01-29

        assertEquals(LocalDate.of(2021, 1, 4), firstWorkingDay);
        assertEquals(LocalDate.of(2021, 1, 29), lastWorkingDay);
    }
}
