/*
 * TimeSeriesRatesTest.java
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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import rife.bld.extension.testing.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
@ExtendWith({LoggingExtension.class, RandomStringResolver.class, RandomRangeResolver.class})
class TimeSeriesRatesTest {
    @RegisterExtension
    @SuppressWarnings("unused")
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(FrankfurterUtils.LOGGER);
    private static final String VALID_BASE_CURRENCY = "USD";
    private static final List<LocalDate> VALID_DATES = FrankfurterUtils.workingDays(
            LocalDate.of(2024, 1, 2), LocalDate.of(2024, 1, 31));
    private static final LocalDate VALID_END_DATE = VALID_DATES.get(VALID_DATES.size() - 1);
    private static final LocalDate VALID_START_DATE = VALID_DATES.get(0);
    private static final List<String> VALID_SYMBOLS_LIST = Arrays.asList("EUR", "GBP");

    @Test
    void timeSeriesConstructorAndGetters() {
        var builder = new TimeSeries.Builder()
                .startDate(VALID_START_DATE)
                .endDate(VALID_END_DATE)
                .base(VALID_BASE_CURRENCY)
                .symbols(VALID_SYMBOLS_LIST);
        var timeSeries = builder.build();

        assertEquals(VALID_BASE_CURRENCY, timeSeries.base());
        assertEquals(VALID_END_DATE, timeSeries.endDate());
        assertEquals(VALID_START_DATE, timeSeries.startDate());
        assertEquals(VALID_SYMBOLS_LIST, timeSeries.symbols());
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {
        @Test
        void builderAllParameters() {
            var builder = new TimeSeries.Builder()
                    .amount(4.25)
                    .base("GBP")
                    .startDate(VALID_START_DATE)
                    .endDate(VALID_END_DATE)
                    .symbols(VALID_SYMBOLS_LIST);
            var timeSeries = builder.build();

            assertEquals(4.25, timeSeries.amount());
            assertEquals("GBP", timeSeries.base());
            assertEquals(VALID_START_DATE, timeSeries.startDate());
            assertEquals(VALID_END_DATE, timeSeries.endDate());
            assertTrue(timeSeries.symbols().containsAll(VALID_SYMBOLS_LIST));
        }

        @Test
        void builderBaseDefault() {
            var builder = new TimeSeries.Builder();
            var timeSeries = builder.build();
            assertEquals(FrankfurterUtils.EUR, timeSeries.base());
        }

        @Test
        void builderBaseInvalid() {
            var builder = new TimeSeries.Builder();

            var ex = assertThrows(IllegalArgumentException.class,
                    () -> builder.base("US"), "Symbol too short"
            );
            assertEquals("Invalid currency symbol: US", ex.getMessage());

            assertThrows(IllegalArgumentException.class, () -> builder.base("DOLLAR"), "Symbol too long");
            assertThrows(IllegalArgumentException.class, () -> builder.base("U$D"), "Symbol non-alphabetic");
            assertThrows(IllegalArgumentException.class, () -> builder.base(null), "Symbol is null");
        }

        @Test
        void builderBaseValid() {
            var builder = new TimeSeries.Builder();
            builder.base("usd"); // Test lowercase
            var timeSeries = builder.build();
            assertEquals("USD", timeSeries.base());
        }

        @Test
        void builderBuild() {
            var builder = new TimeSeries.Builder()
                    .startDate(VALID_START_DATE)
                    .endDate(VALID_END_DATE)
                    .base(VALID_BASE_CURRENCY)
                    .symbols(VALID_SYMBOLS_LIST);
            var timeSeries = builder.build();

            assertNotNull(timeSeries);
            assertEquals(VALID_START_DATE, timeSeries.startDate());
            assertEquals(VALID_END_DATE, timeSeries.endDate());
            assertEquals(VALID_BASE_CURRENCY, timeSeries.base());
            assertEquals(VALID_SYMBOLS_LIST, timeSeries.symbols());
        }

        @Test
        void builderEndDate() {
            var builder = new TimeSeries.Builder();
            builder.endDate(VALID_END_DATE);
            var timeSeries = builder.build();
            assertEquals(VALID_END_DATE, timeSeries.endDate());
        }

        @RepeatedTest(3)
        @RandomRange(min = 1, max = 10)
        void builderEndDateIsBeforeMinDate(int dayToSubtract) {
            var builder = new TimeSeries.Builder();
            var invalidDate = FrankfurterUtils.MIN_DATE.minusDays(dayToSubtract);
            assertThrows(IllegalArgumentException.class, () -> builder.endDate(invalidDate),
                    "The end date (" + invalidDate + ") must be before " + FrankfurterUtils.MIN_DATE);
        }

        @Test
        void builderEndDateIsLocalDate() {
            var builder = new TimeSeries.Builder();
            builder.endDate(VALID_END_DATE);
            var timeSeries = builder.startDate(VALID_START_DATE).build();
            assertEquals(VALID_END_DATE, timeSeries.endDate());
        }

        @Test
        void builderEndDateIsNull() {
            var builder = new TimeSeries.Builder();
            assertThrows(IllegalArgumentException.class, () -> builder.endDate(null));
        }

        @Test
        void builderStartDate() {
            var builder = new TimeSeries.Builder();
            builder.startDate(VALID_START_DATE);
            var timeSeries = builder.build();
            assertEquals(VALID_START_DATE, timeSeries.startDate());
        }

        @Test
        void builderStartDateIsBeforeMinDate() {
            var builder = new TimeSeries.Builder();
            var invalidDate = FrankfurterUtils.MIN_DATE.minusDays(1);
            assertThrows(IllegalArgumentException.class, () -> builder.startDate(invalidDate));
        }

        @Test
        void builderStartDateIsLocalDate() {
            var builder = new TimeSeries.Builder();
            builder.startDate(VALID_START_DATE);
            var timeSeries = builder.build();
            assertEquals(VALID_START_DATE, timeSeries.startDate());
        }

        @Test
        void builderStartDateIsNull() {
            var builder = new TimeSeries.Builder();
            assertThrows(IllegalArgumentException.class, () -> builder.startDate(null));
            var timeSeries = builder.build();
            assertNull(timeSeries.startDate());
        }

        @Test
        void builderStartDateIsSameAsEndDate() {
            var builder = new TimeSeries.Builder();
            builder.startDate(VALID_START_DATE).endDate(VALID_START_DATE);
            var timeSeries = builder.build();
            assertEquals(VALID_START_DATE, timeSeries.startDate());
            assertEquals(VALID_START_DATE, timeSeries.endDate());
        }

        @Test
        void builderSymbolsListWithInvalidSymbol() {
            var builder = new TimeSeries.Builder();

            var ex = assertThrows(IllegalArgumentException.class,
                    () -> builder.symbols(List.of("US", "EUR"))
            );
            assertEquals("Invalid currency symbol: US", ex.getMessage());

            ex = assertThrows(IllegalArgumentException.class, () -> builder.symbols(List.of("USD", "EUROPE")));
            assertEquals("Invalid currency symbol: EUROPE", ex.getMessage());
        }

        @ParameterizedTest
        @NullAndEmptySource
        void builderSymbolsListWithNullAndEmpty(String input) {
            var builder = new TimeSeries.Builder();
            assertThrows(IllegalArgumentException.class, () -> builder.symbols("USD", input));
            assertThrows(IllegalArgumentException.class, () -> builder.symbols(Arrays.asList("USD", input))); // Collection with null
        }

        @Test
        void builderSymbolsWithCollection() {
            var builder = new TimeSeries.Builder();
            builder.symbols(Arrays.asList("jpy", "cad"));
            var timeSeries = builder.build();
            assertTrue(timeSeries.symbols().containsAll(Arrays.asList("JPY", "CAD")));
            assertEquals(2, timeSeries.symbols().size());
        }

        @Test
        void builderSymbolsWithEmptyCollection() {
            var builder = new TimeSeries.Builder();
            builder.symbols(Collections.emptyList()); // Empty collection
            var timeSeries = builder.build();
            assertTrue(timeSeries.symbols().isEmpty());
        }

        @Test
        void builderSymbolsWithEmptyVarargs() {
            var builder = new TimeSeries.Builder();
            builder = builder.symbols(); // Empty varargs
            var timeSeries = builder.build();
            assertTrue(timeSeries.symbols().isEmpty());
        }

        @RepeatedTest(3)
        @RandomString
        void builderSymbolsWithInvalidRandomSymbol(String input) {
            var builder = new TimeSeries.Builder();
            assertThrows(IllegalArgumentException.class, () -> builder.symbols(input),
                    "Currency symbol should be invalid: " + input);
        }

        @ParameterizedTest
        @ValueSource(strings = {"EU", "EUROS", "E$S"})
        void builderSymbolsWithInvalidSymbol(String input) {
            var builder = new TimeSeries.Builder();
            assertThrows(IllegalArgumentException.class, () -> builder.symbols(input));
        }

        @ParameterizedTest
        @NullSource
        void builderSymbolsWithNullVarargs(String input) {
            var builder = new TimeSeries.Builder();
            assertThrows(IllegalArgumentException.class, () -> builder.symbols(input)); // Varargs with null

        }

        @Test
        void builderSymbolsWithVarargs() {
            var builder = new TimeSeries.Builder();
            builder.symbols("eur", "gbp");
            var timeSeries = builder.build();
            assertTrue(timeSeries.symbols().containsAll(VALID_SYMBOLS_LIST));
            assertEquals(2, timeSeries.symbols().size());
        }
    }

    @Nested
    @DisplayName("Periodic Rates Tests")
    class PeriodicRatesTests {
        @Test
        void periodicRatesAllParameters() throws IOException, URISyntaxException, InterruptedException {
            var timeSeries = new TimeSeries.Builder()
                    .amount(5.25)
                    .startDate(VALID_START_DATE)
                    .endDate(VALID_END_DATE)
                    .base(VALID_BASE_CURRENCY)
                    .symbols(VALID_SYMBOLS_LIST)
                    .build();

            var data = timeSeries.periodicRates();
            assertNotNull(data);
            assertEquals(timeSeries.amount(), data.amount());
            assertEquals(timeSeries.base(), data.base());
            assertEquals(timeSeries.startDate(), data.startLocalDate());
            assertEquals(timeSeries.endDate(), data.endLocalDate());
            assertEquals(timeSeries.startDate(), data.startLocalDate());
            VALID_DATES.forEach(date -> {
                assertTrue(data.ratesFor(date).get("EUR") > 0, "getPeriodicRates(date).get(EUR)");
                assertTrue(data.ratesFor(date).get("GBP") > 0, "getPeriodicRates(date).get(GBP)");
            });
        }

        @Test
        void periodicRatesCurrentMonth() throws IOException, URISyntaxException, InterruptedException {
            var now = LocalDate.now();
            var timeSeries = new TimeSeries.Builder()
                    .startDate(now.withDayOfMonth(1))
                    .endDate(now)
                    .build();

            var data = timeSeries.periodicRates();

            assertNotNull(data);
            assertTrue(data.endLocalDate().isBefore(now) || data.endLocalDate().isEqual(now));
            assertFalse(data.dates().isEmpty());
        }

        @Test
        void periodicRatesEndDateBeforeStartDate() {
            var timeSeries = new TimeSeries.Builder()
                    .startDate(VALID_END_DATE)
                    .endDate(VALID_START_DATE)
                    .build(); // startDate is null
            var exception = assertThrows(IllegalArgumentException.class, timeSeries::periodicRates);
            assertEquals("The end date must be on or after the start date.", exception.getMessage());
        }

        @Test
        void periodicRatesForUSD() throws IOException, URISyntaxException, InterruptedException {
            var startDate = LocalDate.of(2025, 1, 2);
            var endDate = LocalDate.of(2025, 1, 9);
            var timeSeries =
                    new TimeSeries.Builder()
                            .startDate(startDate)
                            .endDate(endDate)
                            .base("USD").build();
            assertEquals("USD", timeSeries.base(), "base()");
            assertEquals(startDate, timeSeries.startDate(), "startDate()");
            assertEquals(endDate, timeSeries.endDate(), "getEndDate()");
            assertEquals(Double.valueOf("0.9689"),
                    timeSeries.periodicRates().ratesFor(startDate).get("EUR"),
                    "getPeriodicRates(2025-01-02).get(EUR)");
        }

        @Test
        void periodicRatesOnlyStartDate() throws IOException, URISyntaxException, InterruptedException {
            var timeSeries = new TimeSeries.Builder()
                    .startDate(VALID_START_DATE) // Default base EUR, no end date, no symbols
                    .build();

            var data = timeSeries.periodicRates();

            assertNotNull(data);
            assertEquals(timeSeries.base(), data.base());
            assertEquals(timeSeries.startDate(), data.startLocalDate());
            assertNull(timeSeries.endDate());
        }

        @Test
        void periodicRatesStartDateNull() {
            var timeSeries = new TimeSeries.Builder().build(); // startDate is null
            var exception = assertThrows(IllegalArgumentException.class, timeSeries::periodicRates);
            assertEquals("The start date is required.", exception.getMessage());
        }

        @Test
        void periodicRatesWithIntAmount() throws IOException, URISyntaxException, InterruptedException {
            var timeSeries = new TimeSeries.Builder()
                    .startDate(VALID_START_DATE)
                    .endDate(VALID_START_DATE)
                    .amount(10)
                    .build();

            assertEquals(10.0, timeSeries.amount());

            var data = timeSeries.periodicRates();

            assertNotNull(data);
            assertEquals(timeSeries.amount(), data.amount());
        }

        @Test
        void periodicRatesWithNullAmount() throws IOException, URISyntaxException, InterruptedException {
            var timeSeries = new TimeSeries.Builder()
                    .amount(null) // not set
                    .startDate(VALID_START_DATE)
                    .endDate(VALID_START_DATE)
                    .build();

            assertNull(timeSeries.amount());

            var data = timeSeries.periodicRates();

            assertNotNull(data);
            assertEquals(1.0, data.amount());
        }
    }
}
