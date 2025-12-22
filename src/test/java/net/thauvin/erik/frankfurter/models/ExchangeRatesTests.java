/*
 * RatesTest.java
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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
class ExchangeRatesTests {
    @Test
    void recordCreationAndAccessors() {
        var amount = 10.0;
        var base = "USD";
        var date = LocalDate.of(2024, 1, 13);
        Map<String, Double> ratesMap = new ConcurrentHashMap<>();
        ratesMap.put("EUR", 0.96);
        ratesMap.put("JPY", 151.0);

        var exchangeRates = new ExchangeRates(amount, base, date, ratesMap);

        assertEquals(amount, exchangeRates.amount(), "Amount should match provided value");
        assertEquals(base, exchangeRates.base(), "Base currency should match provided value");
        assertEquals(date, exchangeRates.date(), "Date string should match provided value");
        assertEquals(ratesMap, exchangeRates.rates(), "Rates map should match");
    }

    @Test
    void recordCreationWithEmptyRates() {
        var amount = 5.0;
        var base = "EUR";
        var date = LocalDate.of(2024, 1, 15);
        Map<String, Double> rates = Collections.emptyMap();

        var exchangeRates = new ExchangeRates(amount, base, date, rates);

        assertEquals(amount, exchangeRates.amount());
        assertEquals(base, exchangeRates.base());
        assertEquals(date, exchangeRates.date());
        assertTrue(exchangeRates.rates().isEmpty(), "Rates map should be empty");
    }

    @Test
    void recordCreationWithNullRates() {
        var amount = 10.25;
        var base = "GBP";
        var date = LocalDate.of(2023, 12, 31);

        var exchangeRates = new ExchangeRates(amount, base, date, null);

        assertEquals(amount, exchangeRates.amount());
        assertEquals(base, exchangeRates.base());
        assertEquals(date, exchangeRates.date());
        assertNull(exchangeRates.rates(), "Rates map should be null");
    }

    @Nested
    @DisplayName("Date Tests")
    class DateTests {
        @Test
        void dateWithAnotherValidDate() {
            var date = LocalDate.of(2025, 2, 28);
            var exchangeRates = new ExchangeRates(1.0, "EUR", date, Collections.singletonMap("USD", 1.08));
            var expectedDate = LocalDate.of(2025, 2, 28);

            assertEquals(expectedDate, exchangeRates.date(),
                    "Parsed LocalDate should match for another valid date");
        }

        @Test
        void dateWithValidDate() {
            var date = LocalDate.of(2023, 11, 1);
            var exchangeRates = new ExchangeRates(1.0, "USD", date, Collections.emptyMap());
            var expectedDate = LocalDate.of(2023, 11, 1);

            assertEquals(expectedDate, exchangeRates.date(), "Parsed LocalDate should match");
        }
    }

    @Nested
    @DisplayName("Rate For Symbol Tests")
    class RateForSymbolTests {
        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   "})
        void rateForBlankSymbol(String input) {
            var ratesMap = Map.of("USD", 1.08, "EUR", 0.96);
            var exchangeRates = new ExchangeRates(1.0, "AUD",
                    LocalDate.of(2025, 2, 28), ratesMap);

            assertNull(exchangeRates.rateFor(input),
                    "Should return null when the symbol is a blank string");
        }

        @Test
        void rateForInvalidSymbol() {
            var ratesMap = Map.of("USD", 1.08, "EUR", 0.96);
            var exchangeRates = new ExchangeRates(1.0, "AUD",
                    LocalDate.of(2025, 2, 28), ratesMap);

            assertNull(exchangeRates.rateFor("JPY"),
                    "Should return null for non-existent currency symbol");
        }

        @Test
        void rateForValidSymbol() {
            var ratesMap = Map.of("USD", 1.08, "EUR", 0.96);
            var exchangeRates = new ExchangeRates(1.0, "AUD",
                    LocalDate.of(2025, 2, 28), ratesMap);

            assertEquals(1.08, exchangeRates.rateFor("USD"),
                    "Should retrieve correct rate for USD");
            assertEquals(0.96, exchangeRates.rateFor("EUR"),
                    "Should retrieve correct rate for EUR");
        }
    }

    @Nested
    @DisplayName("Symbols Tests")
    class SymbolsTests {
        @Test
        void symbolsFromEmptyRates() {
            var exchangeRates = new ExchangeRates(1.0, "AUD",
                    LocalDate.of(2025, 2, 28), Collections.emptyMap());

            var symbols = exchangeRates.symbols();

            assertTrue(symbols.isEmpty(), "Symbols should be empty when rates map is empty");
        }

        @Test
        void symbolsFromNonEmptyRates() {
            var ratesMap = Map.of("USD", 1.08, "EUR", 0.96, "JPY", 151.0);
            var exchangeRates = new ExchangeRates(1.0, "AUD",
                    LocalDate.of(2025, 2, 28), ratesMap);

            var symbols = exchangeRates.symbols();

            assertEquals(ratesMap.keySet(), symbols, "Symbols should match the key set of the rates map");
        }

        @Test
        void symbolsFromNullRates() {
            var exchangeRates = new ExchangeRates(1.0, "AUD",
                    LocalDate.of(2025, 2, 28), null);

            assertThrows(NullPointerException.class, exchangeRates::symbols,
                    "Should throw NullPointerException when rates map is null");
        }
    }
}
