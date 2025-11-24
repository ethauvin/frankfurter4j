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
import org.junit.jupiter.api.extension.RegisterExtension;
import rife.bld.extension.testing.LoggingExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@ExtendWith(LoggingExtension.class)
class ReadmeExamplesTests {
    @RegisterExtension
    @SuppressWarnings("unused")
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(FrankfurterUtils.LOGGER);

    @Test
    void availableCurrencies() {
        var currencies = CurrencyRegistry.getInstance();
        var usd = currencies.findBySymbol("USD"); // case-insensitive
        if (usd.isPresent()) {
            var name = usd.get().name(); // United States Dollar
            var symbol = usd.get().symbol(); // USD
            var locale = usd.get().locale(); // Locale.US

            assertEquals("United States Dollar", name);
            assertEquals("USD", symbol);
            assertEquals(java.util.Locale.US, locale);
        }

        var dollar = currencies.findBySymbol("usd"); // case-insensitive
        assertEquals(usd, dollar);

        var eur = currencies.findBySymbol("EUR"); // the record for Euro
        assertEquals("Euro", eur.orElseThrow().name());

        var euro = currencies.findByName("euro"); // the record for EUR
        assertEquals("EUR", euro.orElseThrow().symbol());

        var jpy = currencies.findByName(".*Japan.*"); // the record for JPY
        assertEquals("JPY", jpy.orElseThrow().symbol());

        var dollars = currencies.search(".*Dollar$"); //  list of matching currencies
        assertTrue(dollars.contains(usd.orElseThrow()));

        assertTrue(currencies.contains("JPY"));
    }

    @Test
    @SuppressWarnings("PMD.SystemPrintln")
    void currencyConversionExample() throws IOException, URISyntaxException, InterruptedException {
        var latestRates = new LatestRates.Builder()
                .amount(10)
                .base("USD")
                .symbols("EUR")
                .build();
        var exchangeRates = latestRates.exchangeRates();
        var euro = exchangeRates.rateFor("EUR");

        System.out.println("$10 = €" + euro);

        System.out.println(CurrencyFormatter.format(exchangeRates.base(), exchangeRates.amount())
                + " = " + CurrencyFormatter.format("EUR", euro));

        assertTrue(euro > 0);
    }

    @Test
    void formatCurrencyExamples() {
        assertEquals(FormatCurrencyUtils.toDollar(100.0)
                , CurrencyFormatter.format("USD", 100.0));
        assertEquals(FormatCurrencyUtils.toEur(1234.567),
                CurrencyFormatter.format("EUR", 1234.567));
        assertEquals(FormatCurrencyUtils.toEur(1234.57),
                CurrencyFormatter.format("EUR", 1234.567, true));
    }

    @Test
    void historicalRatesExample() throws IOException, URISyntaxException, InterruptedException {
        var date = LocalDate.of(1999, 1, 4);
        var latestRates = new LatestRates.Builder()
                .date(date)
                .build();
        var ratesDate = latestRates.exchangeRates();
        assertEquals(date, ratesDate.date());
    }

    @Test
    void historicalRatesExampleWithBaseAndCurrencies() throws IOException, URISyntaxException, InterruptedException {
        var latestRates = new LatestRates.Builder()
                .base("USD")
                .date(LocalDate.of(1999, 1, 4))
                .symbols("EUR")
                .build();

        var ratesDate = latestRates.exchangeRates();
        assertTrue(ratesDate.hasRateFor("EUR"));
    }

    @Test
    void latestRatesExample() throws IOException, URISyntaxException, InterruptedException {
        var latestRates = new LatestRates.Builder().build();
        var exchangeRates = latestRates.exchangeRates();

        if (exchangeRates.hasRateFor("JPY")) {
            var jpy = exchangeRates.rateFor("JPY");
            assertTrue(jpy > 0);
        } else {
            fail("JPY not found");
        }
    }

    @Test
    void latestRatesExampleWithBase() throws IOException, URISyntaxException, InterruptedException {
        var latestRates = new LatestRates.Builder()
                .base("USD")
                .build();

        var ratesDate = latestRates.exchangeRates();
        assertTrue(ratesDate.hasRateFor("CHF"));
    }

    @Test
    @SuppressWarnings("PMD.SystemPrintln")
    void periodicRatesExample() throws IOException, URISyntaxException, InterruptedException {
        var timeSeries = new TimeSeries.Builder()
                .startDate(LocalDate.of(2000, 1, 1))
                .endDate(LocalDate.of(2000, 12, 31))
                .build();
        var periodicRates = timeSeries.periodicRates();

        var firstMarketDay = LocalDate.of(2000, 1, 4);
        if (periodicRates.hasRatesFor(firstMarketDay)) {
            var rates = periodicRates.ratesFor(firstMarketDay);
            var yen = periodicRates.rateFor(firstMarketDay, "JPY");
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

        periodicRates.dates().forEach(date -> {
            System.out.println("Rates for " + date);
            periodicRates.ratesFor(date).forEach((symbol, rate) -> {
                System.out.println("    " + symbol + ": " + rate);
                assertTrue(rate > 0,
                        "Value should be greater than 0 for date: " + date + ", key: " + symbol);
            });
        });
    }

    @Test
    void periodicRatesExampleToPresent() throws IOException, URISyntaxException, InterruptedException {
        var timeSeries = new TimeSeries.Builder()
                .startDate(LocalDate.of(2025, 1, 1))
                .build();

        var periodicRates = timeSeries.periodicRates();
        assertTrue(periodicRates.hasRatesFor(LocalDate.of(2025, 1, 3)));
    }

    @Test
    void periodicRatesExampleWithCurrencies() throws IOException, URISyntaxException, InterruptedException {
        var timeSeries = new TimeSeries.Builder()
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.now())
                .symbols("USD")
                .build();

        var periodicRates = timeSeries.periodicRates();
        assertTrue(periodicRates.ratesFor(LocalDate.of(2025, 1, 3)).containsKey("USD"));
    }

    @Test
    void tldrExample() throws IOException, URISyntaxException, InterruptedException {
        var latestRates = new LatestRates.Builder()
                .amount(100.0)
                .base("USD")
                .symbols("EUR", "GBP")
                .build();
        var exchangeRates = latestRates.exchangeRates();
        var euro = exchangeRates.rateFor("EUR");
        var britishPound = exchangeRates.rateFor("GBP");

        assertTrue(euro > 0);
        assertTrue(britishPound > 0);
    }

    @Test
    void workingDaysExample() {
        var workingDays = FrankfurterUtils.workingDays(LocalDate.of(2021, 1, 1),
                LocalDate.of(2021, 1, 31));

        var firstWorkingDay = workingDays.get(0); // 2021-01-04
        var lastWorkingDay = workingDays.get(workingDays.size() - 1); // 2025-01-29

        assertEquals(LocalDate.of(2021, 1, 4), firstWorkingDay);
        assertEquals(LocalDate.of(2021, 1, 29), lastWorkingDay);
    }
}
