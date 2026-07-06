/*
 * ExchangeRatesTest.java
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

package net.thauvin.erik.frankfurter.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "DataFlowIssue"})
class ExchangeRatesTest {

    private static Rate rate(String quote, double value) {
        return new Rate(LocalDate.of(2024, 1, 1), "USD", quote, value);
    }

    @SuppressWarnings("SameParameterValue")
    private static Rate rate(LocalDate date, String quote, double value) {
        return new Rate(date, "USD", quote, value);
    }

    @Test
    void checkToString() {
        var r = rate("EUR", 1.1);
        var str = new ExchangeRates(List.of(r)).toString();
        assertTrue(str.contains("ExchangeRates"));
        assertTrue(str.contains("size=1"));
    }

    @Test
    @DisplayName("constructor throws on null")
    void constructorNull() {
        assertThrows(NullPointerException.class, () -> new ExchangeRates(null));
    }

    @Test
    @DisplayName("empty() returns empty instance")
    void empty() {
        var ex = ExchangeRates.empty();
        assertTrue(ex.isEmpty());
        assertEquals(0, ex.size());
    }

    @Test
    @DisplayName("equals and hashCode")
    void equalsHashCode() {
        var r1 = rate("EUR", 1.1);
        var r2 = rate("GBP", 0.9);

        var ex1 = new ExchangeRates(List.of(r1, r2));
        var ex2 = new ExchangeRates(List.of(r1, r2));
        var ex3 = new ExchangeRates(List.of(r1));

        assertEquals(ex1, ex2);
        assertEquals(ex1.hashCode(), ex2.hashCode());
        assertNotEquals(ex1, ex3);
    }

    @Test
    @DisplayName("isEmpty and size")
    void isEmptyAndSize() {
        var empty = ExchangeRates.empty();
        assertTrue(empty.isEmpty());
        assertEquals(0, empty.size());

        var ex = new ExchangeRates(List.of(rate("EUR", 1.1), rate("GBP", 0.9)));
        assertFalse(ex.isEmpty());
        assertEquals(2, ex.size());
    }

    @Nested
    @DisplayName("findAll()")
    class FindAllTests {

        @Test
        @DisplayName("findAll is case insensitive")
        void findAllCaseInsensitive() {
            var r1 = rate("EUR", 1.1);
            var r2 = rate("EUR", 1.2);
            var ex = new ExchangeRates(List.of(r1, r2));

            assertEquals(2, ex.findAll("eur").size());
            assertEquals(2, ex.findAll("EuR").size());
        }

        @Test
        @DisplayName("returns unmodifiable list")
        void findAllImmutable() {
            var r = rate("EUR", 1.1);
            var ex = new ExchangeRates(List.of(r));
            var list = ex.findAll("EUR");
            assertThrows(UnsupportedOperationException.class, () -> list.add(r));
        }

        @Test
        @DisplayName("returns empty list when not found")
        void findAllNotFound() {
            var ex = new ExchangeRates(List.of(rate("EUR", 1.1)));
            assertTrue(ex.findAll("GBP").isEmpty());
        }

        @Test
        @DisplayName("throws on null quote")
        void findAllNullQuote() {
            var ex = ExchangeRates.empty();
            assertThrows(NullPointerException.class, () -> ex.findAll((String) null));
        }

        @Test
        @DisplayName("returns all matching rates")
        void findAllRates() {
            var r1 = rate(LocalDate.of(2024, 1, 1), "EUR", 1.1);
            var r2 = rate(LocalDate.of(2024, 1, 2), "EUR", 1.2);
            var r3 = rate("GBP", 0.9);
            var ex = new ExchangeRates(List.of(r1, r2, r3));

            var all = ex.findAll(CurrencyCode.EUR);
            assertEquals(2, all.size());
            assertEquals(1.1, all.get(0).exchangeRate());
            assertEquals(1.2, all.get(1).exchangeRate());
        }
    }

    @Nested
    @DisplayName("find()")
    class FindTests {

        @Test
        @DisplayName("find is case insensitive")
        void findCaseInsensitive() {
            var r = rate("EUR", 1.1);
            var ex = new ExchangeRates(List.of(r));
            assertTrue(ex.find("eur").isPresent());
            assertTrue(ex.find("EuR").isPresent());
        }

        @Test
        @DisplayName("returns first match for time series")
        void findFirstForTimeSeries() {
            var r1 = rate(LocalDate.of(2024, 1, 1), "EUR", 1.1);
            var r2 = rate(LocalDate.of(2024, 1, 2), "EUR", 1.2);
            var ex = new ExchangeRates(List.of(r1, r2));

            var found = ex.find("EUR");
            assertTrue(found.isPresent());
            assertEquals(LocalDate.of(2024, 1, 1), found.get().date());
        }

        @Test
        @DisplayName("throws on null quote")
        void findNullQuote() {
            var ex = ExchangeRates.empty();
            assertThrows(NullPointerException.class, () -> ex.find((String) null));
        }

        @Test
        @DisplayName("finds matching rate")
        void findsRate() {
            var r = rate("EUR", 1.1);
            var ex = new ExchangeRates(List.of(r));
            assertTrue(ex.find("EUR").isPresent());
            assertEquals(1.1, ex.find("EUR").get().exchangeRate());
        }

        @Test
        @DisplayName("returns empty when not found")
        void notFound() {
            var ex = new ExchangeRates(List.of());
            assertTrue(ex.find("GBP").isEmpty());
        }
    }

    @Nested
    @DisplayName("knownQuotes()")
    class KnownQuotesTests {

        @Test
        @DisplayName("filters out codes not in CurrencyCode enum")
        void filtersOutUnknownCodes() {
            var r1 = rate("USD", 1.0);
            var r2 = rate("XYZ", 99.9);
            var r3 = rate("EUR", 0.92);
            var r4 = rate("ABC", 123.0);
            var ex = new ExchangeRates(List.of(r1, r2, r3, r4));

            var known = ex.knownQuotes();
            assertEquals(2, known.size(), "Should only include USD and EUR");
            assertTrue(known.contains(CurrencyCode.USD));
            assertTrue(known.contains(CurrencyCode.EUR));
            assertFalse(known.stream().anyMatch(c -> ("XYZ").equals(c.getCode())));
        }

        @Test
        @DisplayName("handles mixed case from API")
        void handlesMixedCaseFromApi() {
            var r1 = rate("usd", 1.0);
            var r2 = rate("Eur", 0.92);
            var r3 = rate("gBp", 0.79);
            var ex = new ExchangeRates(List.of(r1, r2, r3));

            var known = ex.knownQuotes();
            assertEquals(3, known.size());
            assertTrue(known.contains(CurrencyCode.USD));
            assertTrue(known.contains(CurrencyCode.EUR));
            assertTrue(known.contains(CurrencyCode.GBP));
        }

        @Test
        @DisplayName("handles time series with same quote multiple times")
        void handlesTimeSeries() {
            var r1 = rate(LocalDate.of(2024, 1, 1), "EUR", 1.1);
            var r2 = rate(LocalDate.of(2024, 1, 2), "EUR", 1.2);
            var r3 = rate(LocalDate.of(2024, 1, 3), "EUR", 1.15);
            var ex = new ExchangeRates(List.of(r1, r2, r3));

            var known = ex.knownQuotes();
            assertEquals(1, known.size(), "Should dedupe to single EUR");
            assertEquals(CurrencyCode.EUR, known.get(0));
        }

        @Test
        @DisplayName("removes duplicates but preserves first occurrence order")
        void removesDuplicatesPreservesOrder() {
            var r1 = rate(LocalDate.of(2024, 1, 1), "USD", 1.0);
            var r2 = rate(LocalDate.of(2024, 1, 2), "EUR", 0.92);
            var r3 = rate(LocalDate.of(2024, 1, 3), "USD", 1.01);
            var r4 = rate(LocalDate.of(2024, 1, 4), "GBP", 0.79);
            var r5 = rate(LocalDate.of(2024, 1, 5), "EUR", 0.93);
            var ex = new ExchangeRates(List.of(r1, r2, r3, r4, r5));

            var known = ex.knownQuotes();
            assertEquals(List.of(CurrencyCode.USD, CurrencyCode.EUR, CurrencyCode.GBP), known);
        }

        @Test
        @DisplayName("returns empty list when no rates")
        void returnsEmptyWhenNoRates() {
            var ex = ExchangeRates.empty();
            assertTrue(ex.knownQuotes().isEmpty());
        }

        @Test
        @DisplayName("returns only CurrencyCode enums for known codes")
        void returnsOnlyKnownEnums() {
            var r1 = rate("USD", 1.0);
            var r2 = rate("EUR", 0.92);
            var r3 = rate("GBP", 0.79);
            var ex = new ExchangeRates(List.of(r1, r2, r3));

            var known = ex.knownQuotes();
            assertEquals(3, known.size());
            assertEquals(List.of(CurrencyCode.USD, CurrencyCode.EUR, CurrencyCode.GBP), known);
        }

        @Test
        @DisplayName("returns unmodifiable list")
        void returnsUnmodifiableList() {
            var ex = new ExchangeRates(List.of(rate("USD", 1.0)));
            var known = ex.knownQuotes();
            assertThrows(UnsupportedOperationException.class, () -> known.add(CurrencyCode.EUR));
        }
    }

    @Nested
    @DisplayName("list()")
    class ListTests {

        @Test
        @DisplayName("returns defensive copy")
        void defensiveCopy() {
            var mutable = new java.util.ArrayList<Rate>();
            mutable.add(rate("EUR", 1.1));
            var ex = new ExchangeRates(mutable);

            mutable.add(rate("GBP", 0.9)); // mutate original
            assertEquals(1, ex.size()); // ExchangeRates unchanged
        }

        @Test
        @DisplayName("returns immutable list")
        void immutableList() {
            var r = rate("EUR", 1.1);
            var ex = new ExchangeRates(List.of(r));

            var list = ex.list();
            assertThrows(UnsupportedOperationException.class, () -> list.add(r));
        }
    }

    @Nested
    @DisplayName("quotes()")
    class QuotesTests {

        @Test
        @DisplayName("includes codes not in CurrencyCode enum")
        void includesUnknownCodes() {
            var r1 = rate("USD", 1.0);
            var r2 = rate("XYZ", 99.9);
            var r3 = rate("EUR", 0.92);
            var ex = new ExchangeRates(List.of(r1, r2, r3));

            var codes = ex.quotes();
            assertEquals(3, codes.size());
            assertTrue(codes.contains("USD"));
            assertTrue(codes.contains("XYZ"));
            assertTrue(codes.contains("EUR"));
        }

        @Test
        @DisplayName("removes duplicates but preserves first occurrence order")
        void removesDuplicatesPreservesOrder() {
            var r1 = rate(LocalDate.of(2024, 1, 1), "USD", 1.0);
            var r2 = rate(LocalDate.of(2024, 1, 2), "EUR", 0.92);
            var r3 = rate(LocalDate.of(2024, 1, 3), "USD", 1.01);
            var r4 = rate(LocalDate.of(2024, 1, 4), "GBP", 0.79);
            var ex = new ExchangeRates(List.of(r1, r2, r3, r4));

            var codes = ex.quotes();
            assertEquals(List.of("USD", "EUR", "GBP"), codes);
        }

        @Test
        @DisplayName("returns all distinct quote codes in order")
        void returnsAllDistinctCodes() {
            var r1 = rate("USD", 1.0);
            var r2 = rate("EUR", 0.92);
            var r3 = rate("GBP", 0.79);
            var ex = new ExchangeRates(List.of(r1, r2, r3));

            var codes = ex.quotes();
            assertEquals(List.of("USD", "EUR", "GBP"), codes);
        }

        @Test
        @DisplayName("returns empty list when no rates")
        void returnsEmptyWhenNoRates() {
            var ex = ExchangeRates.empty();
            assertTrue(ex.quotes().isEmpty());
        }

        @Test
        @DisplayName("returns unmodifiable list")
        void returnsUnmodifiableList() {
            var ex = new ExchangeRates(List.of(rate("USD", 1.0)));
            var codes = ex.quotes();
            assertThrows(UnsupportedOperationException.class, () -> codes.add("EUR"));
        }
    }
}
