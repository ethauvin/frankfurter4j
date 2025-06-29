/*
 * FrankfurterUtilsTests.java
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

import net.thauvin.erik.frankfurter.exceptions.HttpErrorException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@ExtendWith(BeforeAllTests.class)
class FrankfurterUtilsTests {
    @Test
    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    void privateConstructor() throws Exception {
        var constructor = FrankfurterUtils.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()), "Constructor is not private");
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
        } catch (InvocationTargetException e) {
            assertInstanceOf(IllegalStateException.class, e.getCause(), e.getMessage());
        }
    }

    @Nested
    @DisplayName("Closing Days Tests")
    class ClosingDaysTests {
        @Test
        void closingDaysForLeapYear() {
            var closingDays = FrankfurterUtils.closingDays(2024);

            assertTrue(closingDays.contains(LocalDate.of(2024, 1, 1)),
                    "Should include New Year's Day");
            assertTrue(closingDays.contains(LocalDate.of(2024, 12, 25)),
                    "Should include Christmas Day");
            assertTrue(closingDays.contains(LocalDate.of(2024, 12, 26)),
                    "Should include Boxing Day");
        }

        @Test
        void closingDaysVerifyDefaultHolidays() {
            var closingDays = FrankfurterUtils.closingDays(2025);

            assertTrue(closingDays.contains(LocalDate.of(2025, 1, 1)),
                    "Should include New Year's Day");
            assertTrue(closingDays.contains(LocalDate.of(2025, 12, 25)),
                    "Should include Christmas Day");
            assertTrue(closingDays.contains(LocalDate.of(2025, 12, 26)),
                    "Should include Boxing Day");

            // Validate calculated Easter-related holidays
            assertEquals(LocalDate.of(2025, 4, 18),
                    FrankfurterUtils.calculateGoodFriday(2025), "Good Friday should be 2025-04-18");
            assertEquals(LocalDate.of(2025, 4, 20),
                    FrankfurterUtils.calculateEasterSunday(2025), "Easter Sunday should be 2025-04-20");
            assertEquals(LocalDate.of(2025, 4, 21),
                    FrankfurterUtils.calculateEasterMonday(2025), "Easter Monday should be 2025-04-21");
        }

        @Test
        void closingDaysWithNegativeYear() {
            var closingDays = FrankfurterUtils.closingDays(-44);

            assertTrue(closingDays.contains(LocalDate.of(-44, 1, 1)),
                    "Should include New Year's Day");
            assertTrue(closingDays.contains(FrankfurterUtils.calculateEasterMonday(-44)),
                    "Should calculate Easter Monday for negative year");
            assertTrue(closingDays.contains(FrankfurterUtils.calculateGoodFriday(-44)),
                    "Should calculate Good Friday for negative year");
        }
    }

    @Nested
    @DisplayName("Date Validation Tests")
    class DateValidationTests {
        @Test
        void validateDateWithExactBoundaryDate() {
            assertDoesNotThrow(() -> FrankfurterUtils.validateDate(LocalDate.of(1994, 1, 4)));
        }

        @Test
        void validateDateWithInvalidEarlyDate() {
            try {
                FrankfurterUtils.validateDate(LocalDate.of(1993, 12, 31));
            } catch (IllegalArgumentException e) {
                assertEquals("Dates prior to 1994-01-04 are not supported: 1993-12-31", e.getMessage(),
                        "Dates before 1994-01-04 should throw an IllegalArgumentException");
            }
        }

        @Test
        void validateDateWithNullDate() {
            assertThrows(IllegalArgumentException.class, () -> FrankfurterUtils.validateDate(null));
        }

        @Test
        void validateDateWithValidDate() {
            assertDoesNotThrow(() -> FrankfurterUtils.validateDate(LocalDate.of(2000, 1, 1)));
        }
    }

    @Nested
    @DisplayName("Easter Sunday Tests")
    class EasterSundayTests {
        @Test
        void calculateEasterSundayFor2026() {
            var easterSunday = FrankfurterUtils.calculateEasterSunday(2026);
            assertEquals(LocalDate.of(2026, 4, 5), easterSunday,
                    "Easter Sunday for 2026 should be 2025-04-05");
        }

        @Test
        void calculateEasterSundayFor2027() {
            var easterSunday = FrankfurterUtils.calculateEasterSunday(2027);
            assertEquals(LocalDate.of(2027, 3, 28), easterSunday,
                    "Easter Sunday for 2027 should be 2027-03-28");
        }

        @Test
        void calculateEasterSundayForHistoricalYear() {
            var easterSunday = FrankfurterUtils.calculateEasterSunday(1900);
            assertEquals(LocalDate.of(1900, 4, 15), easterSunday,
                    "Easter Sunday for 1900 should be 1900-04-15");
        }

        @Test
        void calculateEasterSundayForLeapYear() {
            var easterSunday = FrankfurterUtils.calculateEasterSunday(2024);
            assertEquals(LocalDate.of(2024, 3, 31), easterSunday,
                    "Easter Sunday for 2024 should be 2024-03-31");
        }

        @Test
        void calculateEasterSundayForNonLeapYear() {
            var easterSunday = FrankfurterUtils.calculateEasterSunday(2025);
            assertEquals(LocalDate.of(2025, 4, 20), easterSunday,
                    "Easter Sunday for 2025 should be 2025-04-20");
        }
    }

    @Nested
    @DisplayName("Fetch URI Tests")
    class FetchUriTests {
        @Test
        void fetchUri() throws IOException {
            var uri = URI.create(FrankfurterUtils.API_BASE_URL + "2025-01-02?symbols=USD");
            var response = FrankfurterUtils.fetchUri(uri);

            assertEquals("{\"amount\":1.0,\"base\":\"EUR\",\"date\":\"2025-01-02\",\"rates\":{\"USD\":1.0321}}",
                    response);
        }

        @Test
        void fetchUriWithMalformedUrl() {
            var uri = URI.create("htt://invalid-url");
            try {
                FrankfurterUtils.fetchUri(uri);
            } catch (IOException e) {
                assertEquals("java.net.MalformedURLException", e.getClass().getName());
            }
        }

        @Test
        void fetchUriWithNoErrorStream() {
            var uri = URI.create("https://apichallenges.herokuapp.com/secret/note");

            assertThrows(HttpErrorException.class, () -> FrankfurterUtils.fetchUri(uri));
        }

        @Test
        void fetchUrlWithEmptyUrl() {
            var uri = URI.create("");
            try {
                FrankfurterUtils.fetchUri(uri);
            } catch (IllegalArgumentException | IOException e) {
                assertEquals("java.lang.IllegalArgumentException", e.getClass().getName());
            }
        }

        @Test
        void fetchUrlWithUnknownHost() {
            var uri = URI.create("https://fake.unknown.host");
            try {
                FrankfurterUtils.fetchUri(uri);
            } catch (IOException e) {
                assertEquals("java.net.UnknownHostException", e.getClass().getName(),
                        "Expected UnknownHostException for unknown host");
            }
        }
    }

    @Nested
    @DisplayName("Format Currency Tests")
    class FormatCurrencyTests {
        @Test
        void formatCurrencyRoundedWithInvalidCurrencySymbol() {
            assertThrows(IllegalArgumentException.class,
                    () -> FrankfurterUtils.formatCurrency("INVALID", 100.0, true),
                    "Invalid currency code should throw an IllegalArgumentException");
        }

        @Test
        void formatCurrencyRoundedWithNegativeAmount() {
            assertEquals("-$1,234.57",
                    FrankfurterUtils.formatCurrency("USD", -1234.567, true),
                    "Negative amounts should be formatted correctly with a negative symbol");
        }

        @Test
        void formatCurrencyRoundedWithNullAmount() {
            assertThrows(IllegalArgumentException.class,
                    () -> FrankfurterUtils.formatCurrency("USD", null, true),
                    "Null amount should throw a NullPointerException");
        }

        @Test
        void formatCurrencyRoundedWithNullCurrencyCode() {
            assertThrows(IllegalArgumentException.class,
                    () -> FrankfurterUtils.formatCurrency(null, 100.0, true),
                    "Null currency code should throw an IllegalArgumentException");
        }

        @Test
        void formatCurrencyRoundedWithUnknownCurrencySymbol() {
            var exception = assertThrows(IllegalArgumentException.class,
                    () -> FrankfurterUtils.formatCurrency("FOO", 100.0, true),
                    "Invalid currency code should throw an IllegalArgumentException");
            assertTrue(exception.getMessage().contains("Unknown currency"),
                    "Exception message should mention unknown currency: " + exception.getMessage());
        }

        @Test
        void formatCurrencyRoundedWithValidEUR() {
            assertEquals("1.234,57 €",
                    FrankfurterUtils.formatCurrency("EUR", 1234.567, true),
                    "EUR amount should be formatted correctly in the German locale");
        }

        @Test
        void formatCurrencyRoundedWithValidGBP() {
            assertEquals("£1,234.57",
                    FrankfurterUtils.formatCurrency("GBP", 1234.567, true),
                    "GBP amount should be formatted correctly");
        }

        @Test
        void formatCurrencyRoundedWithValidJPY() {
            assertEquals("￥1,235", FrankfurterUtils.formatCurrency("JPY", 1234.567, true),
                    "JPY amount should be formatted as a whole number with proper symbol");
        }

        @Test
        void formatCurrencyRoundedWithValidUSD() {
            assertEquals("$1,234.57",
                    FrankfurterUtils.formatCurrency("USD", 1234.567, true),
                    "USD amount should be formatted correctly");
        }

        @Test
        void formatCurrencyRoundedWithZeroAmount() {
            assertEquals("$0.00", FrankfurterUtils.formatCurrency("USD", 0.0, true),
                    "Zero amount should be formatted correctly");
        }

        @Test
        void formatCurrencyWithAllSymbols() throws IOException {
            var currencies = AvailableCurrencies.getCurrencies();
            for (var key : Collections.list(currencies.keys())) {
                assertDoesNotThrow(() -> FrankfurterUtils.formatCurrency(key, 10.0), key);
            }
        }

        @Test
        void formatCurrencyWithInvalidCurrencySymbol() {
            assertThrows(IllegalArgumentException.class,
                    () -> FrankfurterUtils.formatCurrency("INVALID", 100.0),
                    "Invalid currency code should throw an IllegalArgumentException");
        }

        @Test
        void formatCurrencyWithNegativeAmount() {
            assertEquals("-$1,234.567", FrankfurterUtils.formatCurrency("USD", -1234.567),
                    "Negative amounts should be formatted correctly with a negative symbol");
        }

        @Test
        void formatCurrencyWithNullAmount() {
            assertThrows(IllegalArgumentException.class,
                    () -> FrankfurterUtils.formatCurrency("USD", null),
                    "Null amount should throw a NullPointerException");
        }

        @Test
        void formatCurrencyWithNullCurrencyCode() {
            assertThrows(IllegalArgumentException.class,
                    () -> FrankfurterUtils.formatCurrency(null, 100.0),
                    "Null currency code should throw an IllegalArgumentException");
        }

        @Test
        void formatCurrencyWithUnknownCurrencySymbol() {
            var exception = assertThrows(IllegalArgumentException.class,
                    () -> FrankfurterUtils.formatCurrency("FOO", 100.0),
                    "Invalid currency code should throw an IllegalArgumentException");
            assertTrue(exception.getMessage().contains("Unknown currency"),
                    "Exception message should mention unknown currency: " + exception.getMessage());
        }

        @Test
        void formatCurrencyWithValidEUR() {
            assertEquals("1.234,567 €", FrankfurterUtils.formatCurrency("EUR", 1234.567),
                    "EUR amount should be formatted correctly in the German locale");
        }

        @Test
        void formatCurrencyWithValidJPY() {
            assertEquals("￥1,234.567", FrankfurterUtils.formatCurrency("JPY", 1234.567),
                    "JPY amount should be formatted as a whole number with proper symbol");
        }

        @Test
        void formatCurrencyWithValidUSD() {
            assertEquals("$1,234.567", FrankfurterUtils.formatCurrency("USD", 1234.567),
                    "USD amount should be formatted correctly");
        }

        @Test
        void formatCurrencyWithZeroAmount() {
            assertEquals("$0.00", FrankfurterUtils.formatCurrency("USD", 0.0),
                    "Zero amount should be formatted correctly");
        }
    }

    @Nested
    @DisplayName("Good Friday Tests")
    class GoodFridayTests {
        @Test
        void calculateGoodFridayForHistoricalYear() {
            var goodFriday = FrankfurterUtils.calculateGoodFriday(1900);
            assertEquals(LocalDate.of(1900, 4, 13), goodFriday,
                    "Good Friday for 1900 should be 1900-04-13");
        }

        @Test
        void calculateGoodFridayForLeapYear() {
            var goodFriday = FrankfurterUtils.calculateGoodFriday(2024);
            assertEquals(LocalDate.of(2024, 3, 29), goodFriday,
                    "Good Friday for 2024 should be 2024-03-29");
        }

        @Test
        void calculateGoodFridayForNonLeapYear() {
            var goodFriday = FrankfurterUtils.calculateGoodFriday(2025);
            assertEquals(LocalDate.of(2025, 4, 18), goodFriday,
                    "Good Friday for 2025 should be 2025-04-18");
        }
    }

    @Nested
    @DisplayName("Is Weekend Tests")
    class IsWeekendTests {
        @Test
        void isWeekendOnFriday() {
            var date = LocalDate.of(2025, 5, 30); // Friday
            assertFalse(FrankfurterUtils.isWeekend(date), "Friday should not be identified as a weekend.");
        }

        @Test
        void isWeekendOnMonday() {
            var date = LocalDate.of(2025, 5, 26); // Monday
            assertFalse(FrankfurterUtils.isWeekend(date), "Monday should not be identified as a weekend.");
        }

        @Test
        void isWeekendOnSaturday() {
            var date = LocalDate.of(2025, 5, 24); // Saturday
            assertTrue(FrankfurterUtils.isWeekend(date), "Saturday should be identified as a weekend.");
        }

        @Test
        void isWeekendOnSunday() {
            var date = LocalDate.of(2025, 5, 25); // Sunday
            assertTrue(FrankfurterUtils.isWeekend(date), "Sunday should be identified as a weekend.");
        }

        @Test
        void isWeekendOnWednesday() {
            var date = LocalDate.of(2025, 5, 28); // Wednesday
            assertFalse(FrankfurterUtils.isWeekend(date), "Wednesday should not be identified as a weekend.");
        }
    }

    @Nested
    @DisplayName("Normalize Symbol Tests")
    class NormalizeSymbolTests {
        @Test
        void normalizeSymbolWithEmptyString() {
            try {
                FrankfurterUtils.normalizeSymbol("");
            } catch (IllegalArgumentException e) {
                assertEquals("Invalid currency symbol: ", e.getMessage(),
                        "Empty string should throw an IllegalArgumentException");
            }
        }

        @Test
        void normalizeSymbolWithInvalidNumeric() {
            try {
                FrankfurterUtils.normalizeSymbol("123");
            } catch (IllegalArgumentException e) {
                assertEquals("Invalid currency symbol: 123", e.getMessage(),
                        "Numeric symbol should throw an IllegalArgumentException");
            }
        }

        @Test
        void normalizeSymbolWithInvalidTooManyChars() {
            try {
                FrankfurterUtils.normalizeSymbol("DOLLAR");
            } catch (IllegalArgumentException e) {
                assertEquals("Invalid currency symbol: DOLLAR", e.getMessage(),
                        "Too many characters in symbol should throw an IllegalArgumentException");
            }
        }

        @Test
        void normalizeSymbolWithValidLowercase() {
            assertEquals("USD", FrankfurterUtils.normalizeSymbol("usd"),
                    "Valid lowercase currency symbol should be converted to uppercase");
        }

        @Test
        void normalizeSymbolWithValidUppercase() {
            assertEquals("GBP", FrankfurterUtils.normalizeSymbol("GBP"),
                    "Valid uppercase currency symbol should remain unchanged");
        }
    }

    @Nested
    @DisplayName("Symbol Validation Tests")
    class SymbolValidationTests {
        @Test
        void validateSymbolWithContainsNumber() {
            assertFalse(FrankfurterUtils.isValidSymbol("A1C"),
                    "Should be invalid if it contains numbers");
        }

        @Test
        void validateSymbolWithContainsSpace() {
            assertFalse(FrankfurterUtils.isValidSymbol("A C"),
                    "Should be invalid if it contains spaces");
        }

        @Test
        void validateSymbolWithContainsSpecialCharacter() {
            assertFalse(FrankfurterUtils.isValidSymbol("A!C"),
                    "Should be invalid if it contains special characters");
        }

        @Test
        void validateSymbolWithEmptyString() {
            assertFalse(FrankfurterUtils.isValidSymbol(""), "Should be invalid for an empty string");
        }

        @Test
        void validateSymbolWithLeadingSpace() {
            assertFalse(FrankfurterUtils.isValidSymbol(" ABC"),
                    "Should be invalid if it has leading spaces");
        }

        @Test
        void validateSymbolWithNull() {
            //noinspection ConstantValue
            assertFalse(FrankfurterUtils.isValidSymbol(null), "Should be invalid for null");
        }

        @Test
        void validateSymbolWithOnlyNumbers() {
            assertFalse(FrankfurterUtils.isValidSymbol("123"),
                    "Should be invalid for only numbers");
        }

        @Test
        void validateSymbolWithOnlySpecialChars() {
            assertFalse(FrankfurterUtils.isValidSymbol("!@#"),
                    "Should be invalid for only special characters");
        }

        @Test
        void validateSymbolWithTooLong() {
            assertFalse(FrankfurterUtils.isValidSymbol("ABCD"),
                    "Should be invalid for a string longer than 3 letters");
        }

        @Test
        void validateSymbolWithTooShort() {
            assertFalse(FrankfurterUtils.isValidSymbol("AB"),
                    "Should be invalid for a string shorter than 3 letters");
        }

        @Test
        void validateSymbolWithTrailingSpace() {
            assertFalse(FrankfurterUtils.isValidSymbol("ABC "),
                    "Should be invalid if it has trailing spaces");
        }

        @Test
        void validateSymbolWithValidLowerCase() {
            assertTrue(FrankfurterUtils.isValidSymbol("xyz"), "Should be valid for 3 lowercase letters");
        }

        @Test
        void validateSymbolWithValidMixedCase() {
            assertTrue(FrankfurterUtils.isValidSymbol("aBc"), "Should be valid for 3 mixed-case letters");
        }

        @Test
        void validateSymbolWithValidUpperCase() {
            assertTrue(FrankfurterUtils.isValidSymbol("ABC"), "Should be valid for 3 uppercase letters");
        }
    }

    @Nested
    @DisplayName("URI Builder Tests")
    class UriBuilderTests {
        @Test
        void uriBuilderWithEmptyPath() throws Exception {
            var query = Map.of("symbols", "USD");
            var expected = FrankfurterUtils.API_BASE_URL + "?symbols=USD";
            var result = FrankfurterUtils.uriBuilder("", query).toString();

            assertEquals(expected, result, "URI with empty path should only include the query.");
        }

        @Test
        void uriBuilderWithEmptyPathAndQuery() throws Exception {
            var result = FrankfurterUtils.uriBuilder("", Collections.emptyMap()).toString();
            assertEquals(FrankfurterUtils.API_BASE_URL, result,
                    "URI with null path and null query should return the base URL.");
        }

        @Test
        void uriBuilderWithEmptyQuery() throws Exception {
            var path = "2025-01-02";
            var expected = FrankfurterUtils.API_BASE_URL + "2025-01-02";
            var result = FrankfurterUtils.uriBuilder(path, Collections.emptyMap()).toString();

            assertEquals(expected, result, "URI with empty query should include only the path.");
        }

        @Test
        void uriBuilderWithNullPathAndQuery() throws Exception {
            var result = FrankfurterUtils.uriBuilder(null, null).toString();
            assertEquals(FrankfurterUtils.API_BASE_URL, result,
                    "URI with null path and null query should return the base URL.");
        }

        @Test
        void uriBuilderWithNullQuery() throws Exception {
            var path = "2025-01-02";
            var expected = FrankfurterUtils.API_BASE_URL + "2025-01-02";
            var result = FrankfurterUtils.uriBuilder(path, null).toString();

            assertEquals(expected, result, "URI with null query should include only the path.");
        }

        @Test
        void uriBuilderWithPathAndQuery() throws Exception {
            var path = "2025-01-02";
            var query = new TreeMap<String, String>();
            query.put("symbols", "USD");
            query.put("base", "EUR");
            var expected = FrankfurterUtils.API_BASE_URL + "2025-01-02?base=EUR&symbols=USD";
            var result = FrankfurterUtils.uriBuilder(path, query).toString();

            assertEquals(expected, result, "URI with path and query should match the expected format.");
        }
    }

    @Nested
    @DisplayName("Working Days Tests")
    class WorkingDaysTests {

        @Test
        void workingDaysEmptyWhenSameDateIsWeekendOrHoliday() {
            var startDate = LocalDate.of(2025, 12, 25); // Christmas holiday
            var endDate = LocalDate.of(2025, 12, 25);

            var workingDays = FrankfurterUtils.workingDays(startDate, endDate);

            assertTrue(workingDays.isEmpty(), "Should return an empty list when the single date is a holiday.");
        }

        @Test
        void workingDaysExcludingHolidays() {
            var startDate = LocalDate.of(2025, 12, 20); // Saturday
            var endDate = LocalDate.of(2026, 1, 2);     // Friday

            var workingDays = FrankfurterUtils.workingDays(startDate, endDate);

            assertEquals(List.of(
                    LocalDate.of(2025, 12, 22), // Monday
                    LocalDate.of(2025, 12, 23), // Tuesday
                    LocalDate.of(2025, 12, 24), // Wednesday
                    LocalDate.of(2025, 12, 29), // Monday
                    LocalDate.of(2025, 12, 30), // Tuesday
                    LocalDate.of(2025, 12, 31), // Wednesday
                    LocalDate.of(2026, 1, 2)    // Friday
            ), workingDays, "Should exclude holidays like Christmas and New Year's Day.");
        }

        @Test
        void workingDaysIncludesWeekdaysExcludesWeekendAndHolidays() {
            var startDate = LocalDate.of(2025, 12, 22); // Monday
            var endDate = LocalDate.of(2025, 12, 28);   // Sunday

            var workingDays = FrankfurterUtils.workingDays(startDate, endDate);

            assertEquals(List.of(
                            LocalDate.of(2025, 12, 22),
                            LocalDate.of(2025, 12, 23),
                            LocalDate.of(2025, 12, 24)
                    ), workingDays,
                    "Should include weekdays and exclude weekend and holidays like Christmas (25th and 26th).");
        }

        @Test
        void workingDaysOnlyWeekends() {
            var startDate = LocalDate.of(2025, 12, 27); // Saturday
            var endDate = LocalDate.of(2025, 12, 28);   // Sunday

            var workingDays = FrankfurterUtils.workingDays(startDate, endDate);

            assertTrue(workingDays.isEmpty(),
                    "Should return an empty list when only weekends are in the range.");
        }

        @Test
        void workingDaysWithStartAfterEndDate() {
            var startDate = LocalDate.of(2025, 12, 28); // Sunday
            var endDate = LocalDate.of(2025, 12, 22);   // Monday

            var workingDays = FrankfurterUtils.workingDays(startDate, endDate);

            assertEquals(List.of(
                    LocalDate.of(2025, 12, 22),
                    LocalDate.of(2025, 12, 23),
                    LocalDate.of(2025, 12, 24)
            ), workingDays, "Should handle reversed dates and return valid working days.");
        }

        @Nested
        @DisplayName("Is Working Day Tests")
        class IsWorkingDayTests {
            @Test
            void isWorkingDayOnHoliday() {
                var date = LocalDate.of(2025, 12, 25); // Christmas
                var closingDays = FrankfurterUtils.closingDays(2025);
                assertFalse(FrankfurterUtils.isWorkingDay(date, closingDays),
                        "A holiday should not be a working day.");
            }

            @Test
            void isWorkingDayOnHolidayThatFallsOnWeekend() {
                var date = LocalDate.of(2027, 12, 25); // Christmas falls on Saturday
                var closingDays = FrankfurterUtils.closingDays(2027);
                assertFalse(FrankfurterUtils.isWorkingDay(date, closingDays),
                        "A holiday that falls on the weekend should not be a working day.");
            }

            @Test
            void isWorkingDayOnOrdinaryDate() {
                var date = LocalDate.of(2025, 5, 26); // Monday, not a holiday
                var closingDays = FrankfurterUtils.closingDays(2025);
                assertTrue(FrankfurterUtils.isWorkingDay(date, closingDays),
                        "An ordinary weekday not on a holiday should be a working day.");
            }

            @Test
            void isWorkingDayOnWeekday() {
                var date = LocalDate.of(2025, 5, 27); // Tuesday
                var closingDays = FrankfurterUtils.closingDays(2025);
                assertTrue(FrankfurterUtils.isWorkingDay(date, closingDays),
                        "A regular weekday should be a working day.");
            }

            @Test
            void isWorkingDayOnWeekend() {
                var date = LocalDate.of(2025, 5, 24); // Saturday
                var closingDays = FrankfurterUtils.closingDays(2025);
                assertFalse(FrankfurterUtils.isWorkingDay(date, closingDays),
                        "A weekend date should not be a working day.");
            }
        }
    }

    @Nested
    @DisplayName("Years Between Tests")
    class YearsBetweenTests {
        @Test
        void yearsBetweenAscendingOrder() {
            var startDate = LocalDate.of(2000, 1, 1);
            var endDate = LocalDate.of(2003, 12, 31);

            var years = FrankfurterUtils.yearsBetween(startDate, endDate);

            assertEquals(List.of(2000, 2001, 2002, 2003), years,
                    "Should return all years between 2000 and 2003 inclusive in ascending order.");
        }

        @Test
        void yearsBetweenDescendingOrder() {
            var startDate = LocalDate.of(2025, 12, 31);
            var endDate = LocalDate.of(2020, 1, 1);

            var years = FrankfurterUtils.yearsBetween(startDate, endDate);

            assertEquals(List.of(2020, 2021, 2022, 2023, 2024, 2025), years,
                    "Should return all years between 2020 and 2025 inclusive, handling descending date order.");
        }

        @Test
        void yearsBetweenLargeRange() {
            var startDate = LocalDate.of(1900, 1, 1);
            var endDate = LocalDate.of(1905, 12, 31);

            var years = FrankfurterUtils.yearsBetween(startDate, endDate);

            assertEquals(List.of(1900, 1901, 1902, 1903, 1904, 1905), years,
                    "Should return all years between 1900 and 1905 inclusive over a large range.");
        }

        @Test
        void yearsBetweenSingleYear() {
            var startDate = LocalDate.of(2022, 1, 1);
            var endDate = LocalDate.of(2022, 12, 31);

            var years = FrankfurterUtils.yearsBetween(startDate, endDate);

            assertEquals(List.of(2022), years,
                    "Should return a single year when start and end dates are within the same year.");
        }
    }
}
