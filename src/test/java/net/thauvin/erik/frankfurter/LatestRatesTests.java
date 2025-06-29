/*
 * LatestRatesTests.java
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
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"PMD.LinguisticNaming", "PMD.AvoidDuplicateLiterals"})
@ExtendWith(BeforeAllTests.class)
class LatestRatesTests {
    @Test
    void constructorAndGetters() {
        var date = LocalDate.of(2023, 5, 10);
        var symbols = Arrays.asList("USD", "JPY");
        var builder = new LatestRates.Builder()
                .amount(123.45)
                .base("GBP")
                .date(date)
                .symbols(symbols);
        var latestRates = new LatestRates(builder);

        assertEquals(123.45, latestRates.getAmount());
        assertEquals("GBP", latestRates.getBase());
        assertEquals(date, latestRates.getDate());
        assertEquals(symbols, latestRates.getSymbols()); // Builder stores formatted symbols
    }

    @Test
    void getExchangeRates() throws IOException, URISyntaxException {
        var builder = new LatestRates.Builder().build();
        var ratesData = builder.getExchangeRates();

        // Compare fields of Rates
        assertEquals(1.0, ratesData.getAmount());
        assertEquals(FrankfurterUtils.EUR, ratesData.getBase());
        assertNotNull(ratesData.getDate());

        var currencies = AvailableCurrencies.getCurrencies();
        assertFalse(currencies.isEmpty(), "AvailableCurrencies should not be empty");
        currencies.forEach((key, value) -> {
            if (!FrankfurterUtils.EUR.equals(key)) {
                assertTrue(ratesData.getRates().containsKey(key), "Rates should contain key: " + key);
                assertTrue(ratesData.getRates().get(key) > 0,
                        "Rates should contain value > 0 for key: " + key);
            }
        });
    }

    @Test
    void getExchangeRatesForHistoricalDate() throws IOException, URISyntaxException {
        var date = LocalDate.of(2010, 1, 4);
        var latestRates = new LatestRates.Builder()
                .date(date)
                .base("USD")
                .build();
        var exchangeRates = latestRates.getExchangeRates();
        assertNotNull(exchangeRates.getRates(), "rates() should not return null");
        assertEquals("USD", exchangeRates.getBase(), "base() should return 'USD");
        assertEquals(date, exchangeRates.getDate(), "date() should return '2010-01-04");
        assertEquals(0.69498, exchangeRates.getRates().get("EUR"), "EUR rate should match");
    }

    @Test
    void getExchangeRatesForHistoricalDateWithAmount() throws IOException, URISyntaxException {
        var date = LocalDate.of(2010, 1, 1);
        var latestRates = new LatestRates.Builder()
                .amount(10.0)
                .date(date)
                .base("USD")
                .build();
        var exchangeRates = latestRates.getExchangeRates();
        assertNotNull(exchangeRates.getRates(), "rates() should not return null");
        assertEquals("USD", exchangeRates.getBase(), "base() should return 'USD");
        assertEquals(date.minusDays(1), exchangeRates.getDate(),
                "date() should return '2010-01-01");
        assertEquals(6.9416, exchangeRates.getRates().get("EUR"), "EUR rate should match");
    }

    @Test
    void getExchangeRatesWithAllParameters() throws IOException, URISyntaxException {
        var testDate = LocalDate.of(2025, 1, 30);
        var builder = new LatestRates.Builder()
                .amount(10.0)
                .base("USD")
                .date(testDate)
                .symbols("EUR", "GBP")
                .build();

        var ratesData = builder.getExchangeRates();

        // Compare fields of Rates
        assertEquals(10.0, ratesData.getAmount());
        assertEquals("USD", ratesData.getBase());
        assertEquals(testDate, ratesData.getDate());
        assertTrue(ratesData.getRates().containsKey("EUR"), "Rates should contain key: EUR");
        assertEquals(8.0443, ratesData.getRates().get("GBP"));
        assertEquals(9.6126, ratesData.getRateFor("EUR"));

    }

    @Test
    void getExchangeRatesWithIntAmount() throws IOException, URISyntaxException {
        var builder = new LatestRates.Builder().amount(10).build();
        var ratesData = builder.getExchangeRates();

        assertEquals(10.0, ratesData.getAmount());
    }

    @Test
    void getExchangeRatesWithInvalidSymbol() {
        assertThrows(IllegalArgumentException.class, () -> new LatestRates.Builder().symbols("INVALID").build());
    }

    @Test
    void getExchangeRatesWithNullAmount() throws IOException, URISyntaxException {
        var builder = new LatestRates.Builder()
                .amount(null) // not set
                .build();
        var ratesData = builder.getExchangeRates();

        assertNull(builder.getAmount());
        assertEquals(1.0, ratesData.getAmount());
    }

    @Test
    void getExchangeRatesWithOutOfBoundsDate() {
        assertThrows(IllegalArgumentException.class, () -> new LatestRates.Builder()
                .date(LocalDate.of(1993, 12, 31))
                .build());
    }

    @Test
    void getExchangeRatesWithValidBaseAndSymbols() throws IOException, URISyntaxException {
        var latestRates = new LatestRates.Builder().base("USD").symbols("EUR", "GBP").build();
        var exchangeRates = latestRates.getExchangeRates();
        assertNotNull(exchangeRates);
        assertEquals("USD", exchangeRates.getBase(), "base() should return 'USD'");
        assertTrue(exchangeRates.getRates().containsKey("EUR"), "rates() should contain 'EUR'");
        assertTrue(exchangeRates.getRates().get("EUR") > 0, "EUR rate should be greater than 0");
        assertTrue(exchangeRates.getRates().containsKey("GBP"), "rates() should contain 'GBP");
        assertTrue(exchangeRates.getRates().get("GBP") > 0, "");
    }
}
