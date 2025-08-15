/*
 * SeriesRatesTests.java
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
class SeriesRatesTests {
    private final LocalDate endDate = LocalDate.of(2023, 1, 5);
    private final LocalDate fifth = LocalDate.of(2023, 1, 5);
    private final LocalDate first = LocalDate.of(2023, 1, 1);
    private final LocalDate nonExistentDate = LocalDate.of(2023, 1, 4);
    private final LocalDate second = LocalDate.of(2023, 1, 2);
    private final LocalDate startDate = LocalDate.of(2023, 1, 1);
    private final LocalDate third = LocalDate.of(2023, 1, 3);
    private SeriesRates dataWithInvalidDateStrings;
    private SeriesRates emptySeriesRates;
    private SeriesRates seriesRates;

    @BeforeEach
    void beforeEach() {
        Map<String, Double> ratesForStartDate = new ConcurrentHashMap<>();
        ratesForStartDate.put("USD", 1.1);
        ratesForStartDate.put(FrankfurterUtils.EUR, 0.9); // Example rate for EUR against EUR base

        Map<LocalDate, Map<String, Double>> rates = new ConcurrentHashMap<>();
        rates.put(first, ratesForStartDate);

        Map<String, Double> ratesForMidDate = new ConcurrentHashMap<>();
        ratesForMidDate.put("USD", 1.15);

        rates.put(second, ratesForMidDate);
        rates.put(third, Collections.emptyMap());

        seriesRates = new SeriesRates(1.0, FrankfurterUtils.EUR, first.toString(), fifth.toString(), rates);
        emptySeriesRates = new SeriesRates(
                1.5, "USD", "2020-01-01", "2020-01-01", Collections.emptyMap());
        dataWithInvalidDateStrings = new SeriesRates(
                2.0, "GBP", "invalid-start-date", "invalid-end-date", Collections.emptyMap());
    }

    @Test
    void checkAllGetters() {
        assertEquals(1.0, seriesRates.amount(), "Amount should be 1.0");
        assertEquals(FrankfurterUtils.EUR, seriesRates.base(), "Base should be EUR");
        assertEquals(first, seriesRates.startLocalDate(), "Start date should be 2023-01-01");
        assertEquals(fifth, seriesRates.endLocalDate(), "End date should be 2023-01-05");

        assertEquals(3, seriesRates.rates().size(), "All rates should have 3 entries");
        assertEquals(3, seriesRates.dates().size(), "All dates should have 3 entries");

        assertEquals(0.9, seriesRates.rateFor(first, "eur"),
                "Rate for EUR should be 0.9 on the 1st");
        assertEquals(1.1, seriesRates.rateFor(first, "USD"),
                "Rate for USD should be 1.1 on the 1st");
        assertEquals(1.15, seriesRates.rateFor(second, "usd"),
                "Rate for USD should be 1.15 on the 2nd");

        assertTrue(seriesRates.hasRatesFor(first), "Should have rates for 2023-01-01");
        assertFalse(seriesRates.ratesFor(first).isEmpty(), "Should have empty rates for 2023-01-01");
        assertEquals(1.1, seriesRates.rateFor(first, "usd"));

        assertTrue(seriesRates.hasRatesFor(second), "Should have rates for the 1st");
        assertFalse(seriesRates.ratesFor(second).isEmpty(), "Should have empty rates for the 2nd");


        assertTrue(seriesRates.ratesFor(third).isEmpty(), "Should have empty rates for the 3rd");

        assertFalse(seriesRates.hasRatesFor(fifth), "Should have no rates for the 5th");

        assertTrue(seriesRates.hasSymbolFor(first, "EUR"), "Should have EUR on the 1st");

        assertTrue(seriesRates.hasSymbolFor(second, "usd"), "Should have USD on the 2nd");
    }

    @Test
    void rateForEmptyRatesMap() {
        assertTrue(emptySeriesRates.ratesFor(startDate).isEmpty(),
                "Should return an empty map when rates map is empty.");
    }

    @ParameterizedTest
    @NullSource
    void rateForNullDate(LocalDate input) {
        assertTrue(seriesRates.ratesFor(input).isEmpty(),
                "Should return an empty map when a null date is provided.");
    }

    @Test
    void rateForValidDate() {
        Map<String, Double> expectedRates = new ConcurrentHashMap<>();
        expectedRates.put("USD", 1.1);
        expectedRates.put(FrankfurterUtils.EUR, 0.9);

        assertEquals(expectedRates, seriesRates.ratesFor(startDate),
                "Should return correct rates for the provided date.");
    }

    @Nested
    @DisplayName("Has Symbol For Tests")
    class HasSymbolForTests {
        @ParameterizedTest(name = "[{index}] ''{0}''")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   "})
        void hasSymbolForBlankSymbolWithDate(String input) {
            assertFalse(seriesRates.hasSymbolFor(startDate, input),
                    "Should return false for a blank symbol with valid LocalDate.");
        }


        @Test
        void hasSymbolForEmptyRatesMapWithDate() {
            assertFalse(emptySeriesRates.hasSymbolFor(LocalDate.of(2020, 1, 1), "USD"),
                    "Should return false when rates map is empty.");
        }


        @Test
        void hasSymbolForExistingSymbolAndDateWithDate() {
            assertTrue(seriesRates.hasSymbolFor(startDate, "USD"),
                    "Should return true for an existing symbol and valid LocalDate.");
        }


        @Test
        void hasSymbolForNonExistingDateWithDate() {
            assertFalse(seriesRates.hasSymbolFor(nonExistentDate, "USD"),
                    "Should return false for a non-existing LocalDate.");
        }

        @Test
        void hasSymbolForNonExistingSymbolWithDate() {
            assertFalse(seriesRates.hasSymbolFor(startDate, "XYZ"),
                    "Should return false for a non-existing symbol with valid LocalDate.");
        }

        @ParameterizedTest
        @NullSource
        void hasSymbolForNullDateWithDate(LocalDate input) {
            assertFalse(seriesRates.hasSymbolFor(input, "USD"),
                    "Should return false for a null LocalDate.");
        }

        @ParameterizedTest
        @NullSource
        void hasSymbolForNullSymbolWithDate(String input) {
            assertFalse(seriesRates.hasSymbolFor(startDate, input),
                    "Should return false for a null symbol with valid LocalDate.");
        }
    }

    @Nested
    @DisplayName("End/Start Local Date Tests")
    class LocalDateTests {
        @Test
        void endLocalDate() {
            assertEquals(endDate, seriesRates.endLocalDate());
        }

        @Test
        void endLocalDateWithInvalidDateString() {
            assertThrows(DateTimeParseException.class, () -> dataWithInvalidDateStrings.endLocalDate());
        }

        @Test
        void startLocalDate() {
            assertEquals(startDate, seriesRates.startLocalDate());
        }

        @Test
        void startLocalDateWithInvalidDateString() {
            assertThrows(DateTimeParseException.class, () -> dataWithInvalidDateStrings.startLocalDate());
        }
    }

    @Nested
    @DisplayName("Rate For Tests")
    class RateForTests {
        @Test
        void rateForBaseCurrencyExplicitRequest() {
            assertEquals(0.9, seriesRates.rateFor(startDate, "EUR"),
                    "Should return a correct rate when explicitly asking for the base currency.");
        }

        @ParameterizedTest(name = "[{index}] ''{0}''")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   "})
        void rateForBlankCurrencySymbol(String input) {
            assertNull(seriesRates.rateFor(startDate, input),
                    "Should return null for blank currency symbol.");
        }

        @Test
        void rateForEmptyRatesMapScenario() {
            assertNull(emptySeriesRates.rateFor(startDate, "USD"),
                    "Should return null when attempting to fetch any rate from an empty rates map.");
        }

        @Test
        void rateForInvalidCurrencySymbol() {
            assertThrows(IllegalArgumentException.class, () -> seriesRates.rateFor(startDate, "INVALID"),
                    "Should throw IllegalArgumentException for invalid currency symbol");
        }

        @Test
        void rateForNonExistentCurrency() {
            assertNull(seriesRates.rateFor(startDate, "XYZ"),
                    "Should return null for a currency that doesn't exist on a valid date.");
        }

        @Test
        void rateForNonExistingDate() {
            assertNull(seriesRates.rateFor(nonExistentDate, "USD"),
                    "Should return null for a date not present in the rates map.");
        }

        @ParameterizedTest
        @NullSource
        void rateForNullDate(LocalDate input) {
            assertNull(seriesRates.rateFor(input, "USD"),
                    "Should return null when date is null.");
        }

        @Test
        void rateForNullDateAndCurrency() {
            assertNull(seriesRates.rateFor(null, null),
                    "Should return null when both date and currencySymbol are null.");
        }

        @Test
        void rateForSpecialCharacterCurrency() {
            assertThrows(IllegalArgumentException.class, () -> seriesRates.rateFor(startDate, "@#%"),
                    "Should throw IllegalArgumentException for special character symbols.");
        }

        @Test
        void rateForValidDateAndCurrency() {
            assertEquals(1.1, seriesRates.rateFor(startDate, "USD"),
                    "Should return the correct rate for a valid date and currency symbol.");
        }
    }

    @Nested
    @DisplayName("Rates For Tests")
    class RatesForTests {
        @Test
        void ratesFor() {
            var expectedRates = new HashMap<>();
            expectedRates.put("USD", 1.1);
            expectedRates.put(FrankfurterUtils.EUR, 0.9);
            assertEquals(expectedRates, seriesRates.ratesFor(startDate));
        }

        @Test
        void ratesForWithDateKeyMapsToEmptyRateMap() {
            assertTrue(seriesRates.ratesFor(fifth).isEmpty());
        }

        @Test
        void ratesForWithDateWithEmptyRatesMap() {
            var actualRates = seriesRates.ratesFor(third);
            assertNotNull(actualRates);
            assertTrue(actualRates.isEmpty());
        }

        @Test
        void ratesForWithNonExistingDate() {
            assertTrue(seriesRates.ratesFor(nonExistentDate).isEmpty());
        }

        @Test
        void ratesForWithOnEmptyTimeSeriesData() {
            assertTrue(emptySeriesRates.ratesFor(LocalDate.of(2020, 1, 1)).isEmpty());
        }
    }
}
