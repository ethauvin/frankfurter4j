/*
 * CurrenciesTest.java
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "DataFlowIssue"})
class CurrenciesTest {

    @Nested
    @DisplayName("codes()")
    class CodesTests {

        @Test
        @DisplayName("includes codes not in CurrencyCode enum")
        void includesUnknownCodes() {
            var c1 = new Currency("USD", "840", "US Dollar", "$", null, null);
            var c2 = new Currency("XYZ", "999", "Future Currency", "X", null, null);
            var currencies = new Currencies(List.of(c1, c2));

            var codes = currencies.codes();
            assertEquals(2, codes.size());
            assertTrue(codes.contains("USD"));
            assertTrue(codes.contains("XYZ"));
        }

        @Test
        @DisplayName("returns all ISO codes in order")
        void returnsAllIsoCodes() {
            var c1 = new Currency("USD", "840", "US Dollar", "$", null, null);
            var c2 = new Currency("EUR", "978", "Euro", "€", null, null);
            var c3 = new Currency("JPY", "392", "Japanese Yen", "¥", null, null);
            var currencies = new Currencies(List.of(c1, c2, c3));

            var codes = currencies.codes();
            assertEquals(List.of("USD", "EUR", "JPY"), codes);
        }

        @Test
        @DisplayName("returns empty list when no currencies")
        void returnsEmptyWhenNoCurrencies() {
            var currencies = new Currencies(List.of());
            assertTrue(currencies.codes().isEmpty());
        }

        @Test
        @DisplayName("returns unmodifiable list")
        void returnsUnmodifiableList() {
            var c = new Currency("USD", "840", "US Dollar", "$", null, null);
            var currencies = new Currencies(List.of(c));
            var codes = currencies.codes();
            assertThrows(UnsupportedOperationException.class, () -> codes.add("EUR"));
        }
    }

    @Nested
    @DisplayName("constructor")
    class ConstructorTests {

        @Test
        @DisplayName("creates defensive copy")
        void defensiveCopy() {
            var mutable = new ArrayList<Currency>();
            var c = new Currency("USD", "840", "US Dollar", "$", null, null);
            mutable.add(c);
            var currencies = new Currencies(mutable);

            mutable.add(new Currency("EUR", "978", "Euro", "€", null, null));
            assertEquals(1, currencies.size(), "Should not be affected by external mutation");
        }

        @Test
        @DisplayName("throws on null element")
        void throwsOnNullElement() {
            var withNull = new ArrayList<Currency>();
            withNull.add(new Currency("USD", "840", "US Dollar", "$", null, null));
            withNull.add(null);

            assertThrows(NullPointerException.class, () -> new Currencies(withNull));
        }

        @Test
        @DisplayName("throws on null list")
        void throwsOnNullList() {
            NullPointerException ex = assertThrows(NullPointerException.class,
                    () -> new Currencies(null));
            assertTrue(ex.getMessage().contains("currencies"));
        }
    }

    @Nested
    @DisplayName("equals() and hashCode()")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("equals equivalent instances")
        void equalsEquivalent() {
            var c1 = new Currency("USD", "840", "US Dollar", "$", null, null);
            var currencies1 = new Currencies(List.of(c1));
            var currencies2 = new Currencies(List.of(c1));
            assertEquals(currencies1, currencies2);
            assertEquals(currencies1.hashCode(), currencies2.hashCode());
        }

        @Test
        @DisplayName("equals same instance")
        @SuppressWarnings("EqualsWithItself")
        void equalsSameInstance() {
            var c = new Currency("USD", "840", "US Dollar", "$", null, null);
            var currencies = new Currencies(List.of(c));
            assertEquals(currencies, currencies);
        }

        @Test
        @DisplayName("not equals for different order")
        void notEqualsDifferentOrder() {
            var c1 = new Currency("USD", "840", "US Dollar", "$", null, null);
            var c2 = new Currency("EUR", "978", "Euro", "€", null, null);
            var currencies1 = new Currencies(List.of(c1, c2));
            var currencies2 = new Currencies(List.of(c2, c1));
            assertNotEquals(currencies1, currencies2);
        }

        @Test
        @DisplayName("not equals null or other type")
        @SuppressWarnings("AssertBetweenInconvertibleTypes")
        void notEqualsNullOrOther() {
            var c = new Currency("USD", "840", "US Dollar", "$", null, null);
            var currencies = new Currencies(List.of(c));
            assertNotEquals(null, currencies);
            assertNotEquals("not currencies", currencies);
        }
    }

    @Nested
    @DisplayName("find()")
    class FindAdditionalTests {

        @Test
        @DisplayName("returns empty for empty string")
        void emptyString() {
            var c = new Currency("USD", "840", "US Dollar", "$", null, null);
            var currencies = new Currencies(List.of(c));
            assertTrue(currencies.find("").isEmpty());
        }

        @Test
        @DisplayName("throws on null ISO")
        void throwsOnNullIso() {
            var currencies = new Currencies(List.of());
            assertThrows(NullPointerException.class, () -> currencies.find((String) null));
        }
    }

    @Nested
    @DisplayName("find()")
    class FindTests {

        @Test
        @DisplayName("finds currency by ISO code (case-insensitive)")
        void findsCurrency() {
            var c = new Currency("USD", "840", "US Dollar", "$", null, null);
            var currencies = new Currencies(List.of(c));

            var result = currencies.find("usd");
            assertTrue(result.isPresent());
            assertEquals("USD", result.get().isoCode());
        }

        @Test
        @DisplayName("returns empty when not found")
        void notFound() {
            var currencies = new Currencies(List.of());
            assertTrue(currencies.find("EUR").isEmpty());
        }
    }

    @Nested
    @DisplayName("fromJson()")
    class FromJsonTests {

        @Test
        @DisplayName("handles empty array")
        void handlesEmptyArray() {
            var currencies = Currencies.fromJson("[]");
            assertTrue(currencies.isEmpty());
        }

        @Test
        @DisplayName("handles null literal")
        void handlesNullLiteral() {
            var currencies = Currencies.fromJson("null");
            assertTrue(currencies.isEmpty());
        }

        @Test
        @DisplayName("returns empty on blank string")
        void returnsEmptyOnBlank() {
            var currencies = Currencies.fromJson("   ");
            assertTrue(currencies.isEmpty());
        }

        @Test
        @DisplayName("throws on malformed JSON")
        void throwsOnMalformed() {
            var badJson = "[{\"isoCode\":\"USD\"";
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> Currencies.fromJson(badJson));
            assertTrue(ex.getMessage().startsWith("Invalid currencies JSON"));
            assertNotNull(ex.getCause());
        }

        @Test
        @DisplayName("throws on null JSON")
        void throwsOnNull() {
            assertThrows(NullPointerException.class, () -> Currencies.fromJson(null));
        }
    }

    @Nested
    @DisplayName("isEmpty()")
    class IsEmptyTests {

        @Test
        @DisplayName("returns true when empty")
        void empty() {
            var currencies = new Currencies(List.of());
            assertTrue(currencies.isEmpty());
        }

        @Test
        @DisplayName("returns false when not empty")
        void notEmpty() {
            var c = new Currency("USD", "840", "US Dollar", "$", null, null);
            var currencies = new Currencies(List.of(c));
            assertFalse(currencies.isEmpty());
        }
    }

    @Nested
    @DisplayName("knownCodes()")
    class KnownCodesTests {

        @Test
        @DisplayName("filters out codes not in CurrencyCode enum")
        void filtersOutUnknownCodes() {
            var c1 = new Currency("USD", "840", "US Dollar", "$", null, null);
            var c2 = new Currency("XYZ", "999", "Future Currency", "X", null, null);
            var c3 = new Currency("EUR", "978", "Euro", "€", null, null);
            var c4 = new Currency("ABC", "000", "Fake Currency", "A", null, null);
            var currencies = new Currencies(List.of(c1, c2, c3, c4));

            var known = currencies.knownCodes();
            assertEquals(2, known.size(), "Should only include USD and EUR");
            assertTrue(known.contains(CurrencyCode.USD));
            assertTrue(known.contains(CurrencyCode.EUR));
        }

        @Test
        @DisplayName("handles mixed case from API")
        void handlesMixedCaseFromApi() {
            var c1 = new Currency("usd", "840", "US Dollar", "$", null, null);
            var c2 = new Currency("Eur", "978", "Euro", "€", null, null);
            var currencies = new Currencies(List.of(c1, c2));

            var known = currencies.knownCodes();
            assertEquals(2, known.size());
            assertTrue(known.contains(CurrencyCode.USD));
            assertTrue(known.contains(CurrencyCode.EUR));
        }

        @Test
        @DisplayName("preserves order but removes duplicates")
        void preservesOrderRemovesDuplicates() {
            var c1 = new Currency("USD", "840", "US Dollar", "$", null, null);
            var c2 = new Currency("EUR", "978", "Euro", "€", null, null);
            var c3 = new Currency("USD", "840", "US Dollar", "$", null, null);
            var currencies = new Currencies(List.of(c1, c2, c3));

            var known = currencies.knownCodes();
            // CurrencyCode.fromCode is case-insensitive, but duplicates in source list stay
            // since we call .distinct() on the stream after mapping
            assertEquals(List.of(CurrencyCode.USD, CurrencyCode.EUR), known);
        }

        @Test
        @DisplayName("returns empty list when no currencies")
        void returnsEmptyWhenNoCurrencies() {
            var currencies = new Currencies(List.of());
            assertTrue(currencies.knownCodes().isEmpty());
        }

        @Test
        @DisplayName("returns only CurrencyCode enums for known codes")
        void returnsOnlyKnownEnums() {
            var c1 = new Currency("USD", "840", "US Dollar", "$", null, null);
            var c2 = new Currency("EUR", "978", "Euro", "€", null, null);
            var c3 = new Currency("GBP", "826", "British Pound", "£", null, null);
            var currencies = new Currencies(List.of(c1, c2, c3));

            var known = currencies.knownCodes();
            assertEquals(3, known.size());
            assertEquals(List.of(CurrencyCode.USD, CurrencyCode.EUR, CurrencyCode.GBP), known);
        }

        @Test
        @DisplayName("returns unmodifiable list")
        void returnsUnmodifiableList() {
            var c = new Currency("USD", "840", "US Dollar", "$", null, null);
            var currencies = new Currencies(List.of(c));
            var known = currencies.knownCodes();
            assertThrows(UnsupportedOperationException.class, () -> known.add(CurrencyCode.EUR));
        }
    }

    @Nested
    @DisplayName("list()")
    class ListAdditionalTests {

        @Test
        @DisplayName("preserves order")
        void preservesOrder() {
            var c1 = new Currency("JPY", "392", "Japanese Yen", "¥", null, null);
            var c2 = new Currency("USD", "840", "US Dollar", "$", null, null);
            var currencies = new Currencies(List.of(c1, c2));
            var result = currencies.list();
            assertEquals("JPY", result.get(0).isoCode());
            assertEquals("USD", result.get(1).isoCode());
        }
    }

    @Nested
    @DisplayName("list()")
    class ListTests {

        @Test
        @DisplayName("returns immutable list")
        void immutableList() {
            var c = new Currency("USD", "840", "US Dollar", "$", null, null);
            var currencies = new Currencies(List.of(c));

            var list = currencies.list();
            //noinspection DataFlowIssue
            assertThrows(UnsupportedOperationException.class, () -> list.add(c));
        }
    }

    @Nested
    @DisplayName("searchByName()")
    class SearchByNameAdditionalTests {

        @Test
        @DisplayName("returns all on empty string")
        void emptyStringReturnsAll() {
            var c1 = new Currency("USD", "840", "US Dollar", "$", null, null);
            var c2 = new Currency("EUR", "978", "Euro", "€", null, null);
            var currencies = new Currencies(List.of(c1, c2));
            assertEquals(2, currencies.searchByName("").size());
        }

        @Test
        @DisplayName("throws on null name")
        void throwsOnNullName() {
            var currencies = new Currencies(List.of());
            assertThrows(NullPointerException.class, () -> currencies.searchByName(null));
        }
    }

    @Nested
    @DisplayName("searchByName()")
    class SearchByNameTests {

        @Test
        @DisplayName("matches substring case-insensitively")
        void matchesSubstring() {
            var c = new Currency("USD", "840", "US Dollar", "$", null, null);
            var currencies = new Currencies(List.of(c));

            var results = currencies.searchByName("doll");
            assertEquals(1, results.size());
        }

        @Test
        @DisplayName("returns empty list when no match")
        void noMatch() {
            var c = new Currency("USD", "840", "US Dollar", "$", null, null);
            var currencies = new Currencies(List.of(c));

            var results = currencies.searchByName("yen");
            assertTrue(results.isEmpty());
        }
    }

    @Nested
    @DisplayName("size()")
    class SizeTests {

        @Test
        @DisplayName("returns correct size")
        void size() {
            var c1 = new Currency("USD", "840", "US Dollar", "$", null, null);
            var c2 = new Currency("EUR", "978", "Euro", "€", null, null);
            var currencies = new Currencies(List.of(c1, c2));

            assertEquals(2, currencies.size());
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringTests {

        @Test
        @DisplayName("contains list representation")
        void containsList() {
            var c = new Currency("USD", "840", "US Dollar", "$", null, null);
            var currencies = new Currencies(List.of(c));
            var str = currencies.toString();
            assertTrue(str.startsWith("Currencies["));
            assertTrue(str.contains("USD"));
        }

        @Test
        @DisplayName("handles empty list")
        void handlesEmpty() {
            var currencies = new Currencies(List.of());
            assertEquals("Currencies[]", currencies.toString());
        }
    }
}
