/*
 * CurrenciesTests.java
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"PMD.LinguisticNaming", "PMD.AvoidDuplicateLiterals"})
class CurrenciesTests {
    private Currencies currencies;

    @BeforeEach
    void beforeEach() {
        currencies = new Currencies();
        currencies.put("USD", "United States Dollar");
        currencies.put("EUR", "Euro");
        currencies.put("JPY", "Japanese Yen");
    }

    @Nested
    @DisplayName("Currency Name Tests")
    class CurrencyNameTests {
        @Test
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        void getCurrencyNameWithEmptyCurrenciesData() {
            var emptyData = new Currencies();
            assertNull(emptyData.getFullNameFor("USD"),
                    "Should return null for any symbol in an empty map.");
            assertEquals(0, emptyData.size(), "Size of a new Currencies should be 0.");
        }

        @Test
        void getCurrencyNameWithEmptySymbol() {
            assertNull(currencies.getFullNameFor(""),
                    "Should return null for an empty string symbol if not explicitly added.");
            currencies.put("", "Empty Symbol Currency");
            assertNull(currencies.getFullNameFor(""),
                    "Should return null if an empty string symbol was explicitly added.");
        }

        @Test
        void getCurrencyNameWithExistingSymbol() {
            assertEquals("United States Dollar", currencies.getFullNameFor("USD"),
                    "Should return the full name for an existing currency symbol.");
        }

        @Test
        void getCurrencyNameWithLowercaseSymbol() {
            assertEquals("Euro", currencies.getFullNameFor("eur"),
                    "Should return the full name for another existing currency symbol.");
        }

        @Test
        void getCurrencyNameWithMixedCaseSymbol() {
            assertEquals("Japanese Yen", currencies.getFullNameFor("jPy"),
                    "Should return the full name for another existing currency symbol.");
        }

        @Test
        void getCurrencyNameWithNonExistingSymbol() {
            assertNull(currencies.getFullNameFor("XYZ"),
                    "Should return null for a non-existing currency symbol.");
        }

        @Test
        void getCurrencyNameWithNullSymbol() {
            assertNull(currencies.getFullNameFor(null),
                    "Should return null when the symbol is null (as per HashMap behavior).");
        }
    }

    @Nested
    @DisplayName("Currency Symbol Tests")
    class CurrencySymbolTests {
        @Test
        void getCurrencySymbolWithBlankName() {
            assertNull(currencies.getSymbolFor("   "));
        }

        @Test
        void getCurrencySymbolWithEmptyName() {
            assertNull(currencies.getSymbolFor(""));
        }

        @Test
        void getCurrencySymbolWithInvalidName() {
            assertNull(currencies.getSymbolFor("FOO"));
        }

        @Test
        void getCurrencySymbolWithInvalidRegex() {
            var regex = Pattern.compile(".*japan.*");
            assertNull(currencies.getSymbolFor(regex));
        }

        @Test
        void getCurrencySymbolWithMixedCase() {
            assertEquals("USD", currencies.getSymbolFor("United STATES dollar"));
        }

        @Test
        void getCurrencySymbolWithNull() {
            assertNull(currencies.getSymbolFor((String) null));
        }

        @Test
        void getCurrencySymbolWithNullRegex() {
            assertNull(currencies.getSymbolFor((Pattern) null));
        }

        @Test
        void getCurrencySymbolWithRegex() {
            var regex = Pattern.compile(".*Japan.*");
            assertEquals("JPY", currencies.getSymbolFor(regex));
        }

        @Test
        void getCurrencySymbolWithValidName() {
            assertEquals("USD", currencies.getSymbolFor("United States Dollar"));
        }
    }

    @Nested
    @DisplayName("AvailableCurrencies Data Tests")
    class HashMapBehaviorTests {
        @Test
        void currenciesDataWithContainsKey() {
            assertTrue(currencies.containsKey("USD"), "Should contain an existing key.");
            assertFalse(currencies.containsKey("AUD"), "Should not contain a non-existing key.");
        }

        @Test
        void currenciesDataWithContainsValue() {
            assertTrue(currencies.containsValue("Euro"), "Should contain an existing value.");
            assertFalse(currencies.containsValue("Australian Dollar"),
                    "Should not contain a non-existing value.");
        }

        @Test
        void currenciesDataWithPutAndGet() {
            currencies.put("CAD", "Canadian Dollar");
            assertEquals("Canadian Dollar", currencies.get("CAD"),
                    "Should allow putting and getting new entries like a standard HashMap.");
            assertEquals("Canadian Dollar", currencies.getFullNameFor("CAD"),
                    "getCurrencyName should also work for newly added entries.");
        }

        @Test
        void currenciesDataWithRemove() {
            assertEquals("Japanese Yen", currencies.remove("JPY"),
                    "Remove should return the value of the removed key.");
            assertNull(currencies.getFullNameFor("JPY"),
                    "getCurrencyName should return null for a removed symbol.");
            assertEquals(2, currencies.size(), "Size should decrement after removing an element.");
        }

        @Test
        void currenciesDataWithSize() {
            assertEquals(3, currencies.size(), "Initial size should be 3.");
            currencies.put("GBP", "British Pound");
            assertEquals(4, currencies.size(), "Size should increment after adding an element.");
        }
    }
}