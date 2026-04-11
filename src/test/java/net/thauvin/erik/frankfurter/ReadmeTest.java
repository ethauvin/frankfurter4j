/*
 * ReadmeTest.java
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

package net.thauvin.erik.frankfurter;

import net.thauvin.erik.frankfurter.config.RateConfig;
import net.thauvin.erik.frankfurter.config.RatesConfig;
import net.thauvin.erik.frankfurter.models.ExchangeRates;
import net.thauvin.erik.frankfurter.models.Group;
import net.thauvin.erik.frankfurter.models.Providers;
import net.thauvin.erik.frankfurter.models.Rate;
import net.thauvin.erik.frankfurter.util.CurrencyFormatter;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.IOException;
import java.time.LocalDate;

import static org.junit.Assert.*;

@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.SystemPrintln"})
class ReadmeTest {

    @Test
    void currencies() throws IOException, InterruptedException {
        var client = new Frankfurter();
        var currencies = client.getCurrencies();

        if (currencies instanceof net.thauvin.erik.frankfurter.models.Currencies c) {
            assertFalse(c.isEmpty());
        }
    }

    @Test
    void currency() throws IOException, InterruptedException {
        var client = new Frankfurter();
        var currency = client.getCurrency("EUR");

        if (currency instanceof net.thauvin.erik.frankfurter.models.Currency eur) {
            assertEquals("Euro", eur.name());
        } else {
            throw new AssertionFailedError("Expected Currency, got " + currency.getClass());
        }
    }

    @Test
    void currencyFormat() throws IOException, InterruptedException {
        var client = new Frankfurter();
        var rate = client.getRate("USD", "GBP");

        if (rate instanceof Rate r) {
            var amount = 12;
            var usd = CurrencyFormatter.format(amount, "USD");
            var gbp = CurrencyFormatter.format(r.exchangeRate() * amount, "GBP");
            assertEquals("$12.00", usd);
            assertTrue(gbp.startsWith("£"));
        }
    }

    @Test
    void error() throws IOException, InterruptedException {
        var client = new Frankfurter();
        var rate = client.getRate("FOO", "BAR");

        if (rate instanceof net.thauvin.erik.frankfurter.models.ErrorResponse error) {
            System.out.println(error.status() + ": " + error.message());
            assertEquals(422, error.status());
            assertEquals("invalid currency: FOO,BAR", error.message());
        }
    }

    @Test
    void examples() throws IOException, InterruptedException {
        var client = new Frankfurter();

        var latestRates = client.getRates();
        if (latestRates instanceof ExchangeRates latest) {
            var pound = latest.find("GBP");
            pound.ifPresent(rate -> System.out.println("1 GBP: " + rate.exchangeRate() + " EUR"));
            assertTrue(pound.isPresent());
            assertTrue(pound.get().exchangeRate() > 0);
        } else {
            throw new AssertionFailedError("Expected ExchangeRates, got " + latestRates.getClass());
        }

        var rate = client.getRate("USD", "EUR");
        if (rate instanceof Rate dollar) {
            System.out.println("1 USD: " + dollar.exchangeRate() + " EUR");
            assertTrue(dollar.exchangeRate() > 0);
        } else {
            throw new AssertionFailedError("Expected Rate, got " + rate.getClass());
        }
    }

    @Test
    void filterByProvider() throws IOException, InterruptedException {
        var client = new Frankfurter();
        var filtered = client.getRates(
                new RatesConfig.Builder()
                        .providers("ECB", "BAM")
                        .build()
        );

        if (filtered instanceof ExchangeRates latest) {
            assertFalse(latest.isEmpty());
            assertTrue(latest.size() > 10);
        } else {
            throw new AssertionFailedError("Expected ExchangeRates, got " + filtered.getClass());
        }
    }

    @Test
    void grouping() throws IOException, InterruptedException {
        var client = new Frankfurter();
        var group = client.getRates(
                new RatesConfig.Builder()
                        .from(LocalDate.of(2024, 1, 1))
                        .group(Group.MONTH)
                        .build()
        );

        if (group instanceof ExchangeRates rates) {
            assertTrue(rates.size() > 50);
        } else {
            throw new AssertionFailedError("Expected ExchangeRates, got " + group.getClass());
        }
    }

    @Test
    void historicalRates() throws IOException, InterruptedException {
        var client = new Frankfurter();
        var historicalRates = client.getRates(
                new RatesConfig.Builder().date(LocalDate.parse("1999-01-04")).build()
        );

        if (historicalRates instanceof ExchangeRates rates) {
            assertTrue(rates.find("GBP").isPresent());
        } else {
            throw new AssertionFailedError("Expected ExchangeRates, got " + historicalRates.getClass());
        }
    }

    @Test
    void providers() throws IOException, InterruptedException {
        var client = new Frankfurter();
        var providers = client.getProviders();

        if (providers instanceof Providers p) {
            assertFalse(p.isEmpty());
            assertTrue(p.size() > 10);

            var ecb = p.find("ECB");
            assertTrue(ecb.isPresent());
            assertEquals("European Central Bank", ecb.get().name());
        } else {
            throw new AssertionFailedError("Expected Providers, got " + providers.getClass());
        }
    }

    @Test
    void rate() throws IOException, InterruptedException {
        var client = new Frankfurter();
        var rate = client.getRate(
                new RateConfig.Builder().base("USD").quote("EUR").build()
        );

        if (rate instanceof net.thauvin.erik.frankfurter.models.Rate eur) {
            assertTrue(eur.exchangeRate() > 0);
        } else {
            throw new AssertionFailedError("Expected Rate, got " + rate.getClass());
        }

        rate = client.getRate(
                new RateConfig.Builder()
                        .base("USD")
                        .quote("EUR")
                        .date(LocalDate.of(2026, 1, 1))
                        .build()
        );

        if (rate instanceof net.thauvin.erik.frankfurter.models.Rate usd) {
            assertTrue(usd.exchangeRate() > 0);
        } else {
            throw new AssertionFailedError("Expected Rate, got " + rate.getClass());
        }
    }

    @Test
    void rates() throws IOException, InterruptedException {
        var client = new Frankfurter();
        var latestRates = client.getRates();


        if (latestRates instanceof ExchangeRates rates) {
            var gbp = rates.find("GBP").orElse(null);
            assertNotNull(gbp);
        } else {
            throw new AssertionFailedError("Expected ExchangeRates, got " + latestRates.getClass());
        }
    }

    @Test
    void ratesWithQuotes() throws IOException, InterruptedException {
        var client = new Frankfurter();
        var latestResult = client.getRates(
                new RatesConfig.Builder().base("USD").quotes("EUR", "GBP").build()
        );


        if (latestResult instanceof ExchangeRates latest) {
            assertTrue(latest.find("GBP").isPresent());
            assertTrue(latest.find("EUR").isPresent());
            assertFalse(latest.find("USD").isPresent());
        } else {
            throw new AssertionFailedError("Expected ExchangeRates, got " + latestResult.getClass());
        }
    }

    @Test
    void timeSeries() throws IOException, InterruptedException {
        var client = new Frankfurter();
        var timeSeries = client.getRates(
                new RatesConfig.Builder()
                        .from(LocalDate.parse("2024-01-01"))
                        .to(LocalDate.parse("2024-01-10")).build()
        );

        if (timeSeries instanceof ExchangeRates rates) {
            assertFalse(rates.isEmpty());
            assertTrue(rates.size() > 10);
        } else {
            throw new AssertionFailedError("Expected ExchangeRates, got " + timeSeries.getClass());
        }
    }
}
