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
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"PMD.LinguisticNaming", "PMD.AvoidDuplicateLiterals"})
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
        assertEquals(1.0, seriesRates.getAmount(), "Amount should be 1.0");
        assertEquals(FrankfurterUtils.EUR, seriesRates.getBase(), "Base should be EUR");
        assertEquals(first, seriesRates.getStartDate(), "Start date should be 2023-01-01");
        assertEquals(fifth, seriesRates.getEndDate(), "End date should be 2023-01-05");

        assertEquals(3, seriesRates.getRates().size(), "All rates should have 3 entries");
        assertEquals(3, seriesRates.getDates().size(), "All dates should have 3 entries");

        assertEquals(0.9, seriesRates.getRateFor(first, "eur"),
                "Rate for EUR should be 0.9 on the 1st");
        assertEquals(1.1, seriesRates.getRateFor(first, "USD"),
                "Rate for USD should be 1.1 on the 1st");
        assertEquals(1.15, seriesRates.getRateFor(second, "usd"),
                "Rate for USD should be 1.15 on the 2nd");

        assertTrue(seriesRates.hasRatesFor(first), "Should have rates for 2023-01-01");
        assertFalse(seriesRates.getRatesFor(first).isEmpty(), "Should have empty rates for 2023-01-01");
        assertEquals(1.1, seriesRates.getRateFor(first, "usd"));

        assertTrue(seriesRates.hasRatesFor(second), "Should have rates for the 1st");
        assertFalse(seriesRates.getRatesFor(second).isEmpty(), "Should have empty rates for the 2nd");


        assertTrue(seriesRates.getRatesFor(third).isEmpty(), "Should have empty rates for the 3rd");

        assertFalse(seriesRates.hasRatesFor(fifth), "Should have no rates for the 5th");

        assertTrue(seriesRates.hasSymbolFor(first, "EUR"), "Should have EUR on the 1st");

        assertTrue(seriesRates.hasSymbolFor(second, "usd"), "Should have USD on the 2nd");
    }

    @Test
    void getRateForEmptyRatesMap() {
        assertTrue(emptySeriesRates.getRatesFor(startDate).isEmpty(),
                "Should return an empty map when rates map is empty.");
    }

    @ParameterizedTest
    @NullSource
    void getRateForNullDate(LocalDate input) {
        assertTrue(seriesRates.getRatesFor(input).isEmpty(),
                "Should return an empty map when a null date is provided.");
    }

    @Test
    void getRateForValidDate() {
        Map<String, Double> expectedRates = new ConcurrentHashMap<>();
        expectedRates.put("USD", 1.1);
        expectedRates.put(FrankfurterUtils.EUR, 0.9);

        assertEquals(expectedRates, seriesRates.getRatesFor(startDate),
                "Should return correct rates for the provided date.");
    }

    @Nested
    @DisplayName("Get End/Start Local Date Tests")
    class GetLocalDateTests {
        @Test
        void getEndLocalDate() {
            assertEquals(endDate, seriesRates.getEndDate());
        }

        @Test
        void getEndLocalDateWithInvalidDateString() {
            assertThrows(DateTimeParseException.class, () -> dataWithInvalidDateStrings.getEndDate());
        }

        @Test
        void getStartLocalDate() {
            assertEquals(startDate, seriesRates.getStartDate());
        }

        @Test
        void getStartLocalDateWithInvalidDateString() {
            assertThrows(DateTimeParseException.class, () -> dataWithInvalidDateStrings.getStartDate());
        }
    }

    @Nested
    @DisplayName("Get Rate For Tests")
    class GetRateForTests {
        @Test
        void getRateForBaseCurrencyExplicitRequest() {
            assertEquals(0.9, seriesRates.getRateFor(startDate, "EUR"),
                    "Should return a correct rate when explicitly asking for the base currency.");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   "})
        void getRateForBlankCurrencySymbol(String input) {
            assertNull(seriesRates.getRateFor(startDate, input),
                    "Should return null for blank currency symbol.");
        }

        @Test
        void getRateForEmptyRatesMapScenario() {
            assertNull(emptySeriesRates.getRateFor(startDate, "USD"),
                    "Should return null when attempting to fetch any rate from an empty rates map.");
        }

        @Test
        void getRateForInvalidCurrencySymbol() {
            assertThrows(IllegalArgumentException.class, () -> seriesRates.getRateFor(startDate, "INVALID"),
                    "Should throw IllegalArgumentException for invalid currency symbol");
        }

        @Test
        void getRateForNonExistentCurrency() {
            assertNull(seriesRates.getRateFor(startDate, "XYZ"),
                    "Should return null for a currency that doesn't exist on a valid date.");
        }

        @Test
        void getRateForNonExistingDate() {
            assertNull(seriesRates.getRateFor(nonExistentDate, "USD"),
                    "Should return null for a date not present in the rates map.");
        }

        @ParameterizedTest
        @NullSource
        void getRateForNullDate(LocalDate input) {
            assertNull(seriesRates.getRateFor(input, "USD"),
                    "Should return null when date is null.");
        }

        @Test
        void getRateForNullDateAndCurrency() {
            assertNull(seriesRates.getRateFor(null, null),
                    "Should return null when both date and currencySymbol are null.");
        }

        @Test
        void getRateForSpecialCharacterCurrency() {
            assertThrows(IllegalArgumentException.class, () -> seriesRates.getRateFor(startDate, "@#%"),
                    "Should throw IllegalArgumentException for special character symbols.");
        }

        @Test
        void getRateForValidDateAndCurrency() {
            assertEquals(1.1, seriesRates.getRateFor(startDate, "USD"),
                    "Should return the correct rate for a valid date and currency symbol.");
        }
    }

    @Nested
    @DisplayName("Get Rates For Tests")
    class GetRatesForTests {
        @Test
        void getRatesFor() {
            var expectedRates = new HashMap<>();
            expectedRates.put("USD", 1.1);
            expectedRates.put(FrankfurterUtils.EUR, 0.9);
            assertEquals(expectedRates, seriesRates.getRatesFor(startDate));
        }

        @Test
        void getRatesForWithDateKeyMapsToEmptyRateMap() {
            assertTrue(seriesRates.getRatesFor(fifth).isEmpty());
        }

        @Test
        void getRatesForWithDateWithEmptyRatesMap() {
            var actualRates = seriesRates.getRatesFor(third);
            assertNotNull(actualRates);
            assertTrue(actualRates.isEmpty());
        }

        @Test
        void getRatesForWithNonExistingDate() {
            assertTrue(seriesRates.getRatesFor(nonExistentDate).isEmpty());
        }

        @Test
        void getRatesForWithOnEmptyTimeSeriesData() {
            assertTrue(emptySeriesRates.getRatesFor(LocalDate.of(2020, 1, 1)).isEmpty());
        }
    }

    @Nested
    @DisplayName("Has Symbol For Tests")
    class HasSymbolForTests {
        @ParameterizedTest
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
    @DisplayName("toString Tests")
    class ToStringTests {
        @Test
        void toStringMatchesExpectedFormat() {
            var expected = Pattern.compile("SeriesRates\\{amount=1\\.0, base='EUR', " +
                    "startDate='2023-01-01', endDate='2023-01-05', rates=.*2023-01-02=\\{USD=1\\.15}.*");
            assertTrue(expected.matcher(seriesRates.toString()).matches(),
                    "toString() output should match the expected format: " + seriesRates.toString());
        }

        @Test
        void toStringWithEmptySeriesRates() {
            var expected = "SeriesRates{amount=1.5, base='USD', startDate='2020-01-01', endDate='2020-01-01', " +
                    "rates={}}";
            assertEquals(expected, emptySeriesRates.toString(),
                    "toString() should handle empty series rates.");
        }
    }
}