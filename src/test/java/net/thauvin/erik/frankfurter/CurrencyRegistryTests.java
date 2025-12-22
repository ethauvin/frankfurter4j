/*
 * CurrencyRegistryTests.java
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

import net.thauvin.erik.frankfurter.models.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import rife.bld.extension.testing.RandomString;
import rife.bld.extension.testing.RandomStringResolver;
import rife.bld.extension.testing.TestingUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(RandomStringResolver.class)
@SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
class CurrencyRegistryTests {
    @Nested
    @DisplayName("Add Currency Tests")
    class AddCurrencyTests {
        @Test
        void addCurrency() {
            var registry = CurrencyRegistry.getInstance();
            registry.reset();

            var size = registry.size();

            registry.add(new Currency("FMD", "Fake Money Dollar", Locale.CANADA));
            assertEquals(size + 1, registry.size(), "size should increase by 1");

            var fmd = registry.findBySymbol("FMD").orElseThrow(() ->
                    new AssertionError("FMD not found"));
            assertEquals("FMD", fmd.symbol(), "symbol should be FMD");
            assertEquals("Fake Money Dollar", fmd.name(), "name should be Fake Money Dollar");
            assertEquals(Locale.CANADA, fmd.locale(), "locale should be Canada");
        }

        @Test
        void addCurrencyAlreadyExisting() {
            var registry = CurrencyRegistry.getInstance();
            var dollar = registry.findBySymbol("USD").orElseThrow(() ->
                    new AssertionError("USD should be present"));

            assertEquals("United States Dollar", dollar.name(), "name should be United States Dollar");

            registry.add(new Currency("USD", "US Dollar", Locale.US));
            assertEquals("US Dollar", registry.findBySymbol("USD").orElseThrow(() ->
                            new AssertionError("USD should be present")).name(),
                    "name should now be US Dollar");

            registry.add(dollar);
            assertEquals("United States Dollar", registry.findBySymbol("USD").orElseThrow(() ->
                            new AssertionError("USD should be present")).name(),
                    "name should be back to United State Dollar");
        }

        @Test
        void addCurrencyWithNullSymbol() {
            var registry = CurrencyRegistry.getInstance();
            assertThrows(IllegalArgumentException.class, () -> registry.add(null, null));
        }

        @Test
        void addCurrencyWithNulls() {
            var registry = CurrencyRegistry.getInstance();
            assertThrows(IllegalArgumentException.class, () -> registry.add(
                    new Currency(null, null, null)));
        }

        @Test
        void addCurrencyWithSymbolAndName() {
            var registry = CurrencyRegistry.getInstance();
            var size = registry.size();
            registry.add("FMD", "Fake Money Dollar");
            assertEquals(size + 1, registry.size(), "size should increase by 1");
            var fmd = registry.findBySymbol("FMD").orElseThrow(() ->
                    new AssertionError("FMD not found"));
            assertEquals("FMD", fmd.symbol(), "symbol should be FMD");
            assertEquals("Fake Money Dollar", fmd.name(), "name should be Fake Money Dollar");
            assertEquals(Locale.ROOT, fmd.locale(), "locale should be Locale.ROOT");
        }

        @Test
        void addNullCurrency() {
            var registry = CurrencyRegistry.getInstance();
            assertThrows(IllegalArgumentException.class, () -> registry.add(null));
        }
    }

    @Nested
    @DisplayName("Available Currencies Tests")
    class AvailableCurrenciesTests {
        @Test
        void availableCurrencies() {
            var currencies = assertDoesNotThrow(CurrencyRegistry::availableCurrencies);
            assertNotNull(currencies);
            assertTrue(currencies.containsKey("USD"));
        }

        @Test
        void refresh() {
            var registry = CurrencyRegistry.getInstance();
            var size = registry.size();
            assertDoesNotThrow(registry::refresh);
            assertEquals(size, CurrencyRegistry.getInstance().size());
        }
    }

    @Nested
    @DisplayName("Contains Tests")
    class ContainsTests {
        @Test
        void containsInvalidRegex() {
            var registry = CurrencyRegistry.getInstance();
            assertFalse(registry.contains("("));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"", " "})
        void containsNullOrEmptyName(String name) {
            var registry = CurrencyRegistry.getInstance();
            assertFalse(registry.contains(name));
        }

        @ParameterizedTest
        @NullAndEmptySource
        void containsNullOrEmptySymbol(String symbol) {
            var registry = CurrencyRegistry.getInstance();
            assertFalse(registry.contains(symbol));
        }

        @ParameterizedTest
        @ValueSource(strings = {"USD", "JPY", "CaD"})
        void containsValidSymbol(String symbol) {
            var registry = CurrencyRegistry.getInstance();
            assertTrue(registry.contains(symbol));
        }

        @ParameterizedTest
        @ValueSource(strings = {"US[A-D]", ".*PY", "C\\p{Upper}D"})
        void containsValidSymbolRegex(String symbol) {
            var registry = CurrencyRegistry.getInstance();
            assertTrue(registry.contains(symbol));
        }
    }

    @Nested
    @DisplayName("Currency Name Tests")
    class CurrencyNameTests {
        @ParameterizedTest
        @EmptySource
        @ValueSource(strings = {" ", "   "})
        void findNameWithEmptyOrBlankSymbol(String input) {
            var registry = CurrencyRegistry.getInstance();
            assertTrue(registry.findBySymbol(input).isEmpty());
        }

        @Test
        void findNameWithExistingSymbol() {
            var registry = CurrencyRegistry.getInstance();
            assertEquals("United States Dollar", registry.findBySymbol("USD").orElseThrow(() ->
                            new AssertionError("USD should be present")).name(),
                    "Should return the full name for an existing currency symbol.");
        }

        @Test
        void findNameWithInvalidRegex() {
            var registry = CurrencyRegistry.getInstance();
            assertTrue(registry.findBySymbol("(").isEmpty());
        }

        @RepeatedTest(3)
        @RandomString
        void findNameWithInvalidSymbol(String input) {
            var registry = CurrencyRegistry.getInstance();
            assertTrue(registry.findBySymbol(input).isEmpty(),
                    "Should return empty Optional for a non-existing currency symbol: " + input);
        }

        @Test
        void findNameWithLowercaseSymbol() {
            var registry = CurrencyRegistry.getInstance();
            assertEquals("Euro", registry.findBySymbol("eur").orElseThrow(() ->
                    new AssertionError("EUR should be present")).name());
        }

        @Test
        void findNameWithMixedCaseSymbol() {
            var registry = CurrencyRegistry.getInstance();
            assertEquals("Japanese Yen", registry.findBySymbol("jPy").orElseThrow(() ->
                    new AssertionError("JPY should be present")).name());
        }

        @Test
        void findNameWithNonExistingSymbol() {
            var registry = CurrencyRegistry.getInstance();
            // FIX: Use isEmpty not assertThrows
            assertTrue(registry.findBySymbol("XYZ").isEmpty(),
                    "Should return empty Optional for a non-existing currency symbol.");
        }

        @ParameterizedTest
        @NullAndEmptySource
        void findNameWithNullOrEmptySymbol(String input) {
            var registry = CurrencyRegistry.getInstance();
            assertTrue(registry.findBySymbol(input).isEmpty());
        }
    }

    @Nested
    @DisplayName("Currency Symbol Tests")
    class CurrencySymbolTests {
        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   "})
        void findSymbolWithBlankName(String input) {
            var registry = CurrencyRegistry.getInstance();
            assertTrue(registry.findBySymbol(input).isEmpty(),
                    "Should return empty Optional for blank name: " + input);
        }

        @RepeatedTest(3)
        @RandomString
        void findSymbolWithInvalidName(String input) {
            var registry = CurrencyRegistry.getInstance();
            // FIX: Use isEmpty not assertThrows
            assertTrue(registry.findByName(input).isEmpty(),
                    "Should return empty Optional for invalid name: " + input);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"(", " "})
        void findSymbolWithInvalidRegex(String pattern) {
            var registry = CurrencyRegistry.getInstance();
            assertTrue(registry.findByName(pattern).isEmpty());
        }

        @Test
        void findSymbolWithMixedCase() {
            var registry = CurrencyRegistry.getInstance();
            assertEquals("USD", registry.findByName("United STATES dollar").orElseThrow(() ->
                    new AssertionError("USD should be present")).symbol());
        }

        @Test
        void findSymbolWithNonExistingName() {
            var registry = CurrencyRegistry.getInstance();
            // FIX: Use isEmpty not assertThrows
            assertTrue(registry.findByName("NotARealCurrencyName").isEmpty(),
                    "Should return empty Optional for a non-existing currency name.");
        }

        @ParameterizedTest
        @NullAndEmptySource
        void findSymbolWithNullOrEmptyName(String pattern) {
            var registry = CurrencyRegistry.getInstance();
            assertTrue(registry.findByName(pattern).isEmpty());
        }

        @Test
        void findSymbolWithRegex() {
            var registry = CurrencyRegistry.getInstance();
            assertEquals("JPY", registry.findByName(".*Japan.*").orElseThrow(() ->
                    new AssertionError("JPY should be present")).symbol());
        }

        @Test
        void findSymbolWithValidName() {
            var registry = CurrencyRegistry.getInstance();
            assertEquals("USD", registry.findByName("United States Dollar").orElseThrow(() ->
                    new AssertionError("USD should be present")).symbol());
        }
    }

    @Nested
    @DisplayName("Get All Tests")
    class GetAllTests {
        @Test
        void allCurrencies() {
            var registry = CurrencyRegistry.getInstance();
            registry.reset();

            var currencies = registry.getAllCurrencies();

            assertEquals(CurrencyRegistry.DEFAULT_CURRENCY_COUNT, currencies.size());
        }

        @Test
        void allSymbols() {
            var registry = CurrencyRegistry.getInstance();
            registry.reset();

            var symbols = registry.getAllSymbols();

            assertEquals(CurrencyRegistry.DEFAULT_CURRENCY_COUNT, symbols.size());
            assertTrue(symbols.contains("USD"));
        }
    }

    @Nested
    @DisplayName("Pattern Cache Tests")
    class PatternCacheTests {
        @Test
        void patternCacheClear() {
            var registry = CurrencyRegistry.getInstance();
            if (registry.patternCacheSize() == 0) {
                assertTrue(registry.contains("USD"), "USD should be present");
                assertTrue(registry.size() > 0, "registry size should be greater than 0");
            }
            registry.clearPatternCache();
            assertEquals(0, registry.patternCacheSize(), "cache size should be 0");
        }

        @Test
        void patternCacheSize() {
            var registry = CurrencyRegistry.getInstance();

            while (registry.patternCacheSize() < 50) {
                registry.contains(TestingUtils.generateRandomString(3));
            }

            assertEquals(50, registry.patternCacheSize(), "cache size should now be 50");

            registry.contains(TestingUtils.generateRandomString(3));

            assertEquals(50, registry.patternCacheSize(), "cache size should still be 50");

            registry.clearPatternCache();

            assertEquals(0, registry.patternCacheSize(), "cache size should be 0");
        }
    }

    @Nested
    @DisplayName("Search Tests")
    class SearchTests {
        static final List<String> DOLLAR_SYMBOLS = List.of("AUD", "CAD", "HKD", "NZD", "SGD", "USD");

        @Test
        void searchForName() {
            var registry = CurrencyRegistry.getInstance();
            registry.reset();

            var results = registry.search("Dollar");
            assertEquals(6, results.size());

            var symbols = results.stream().map(Currency::symbol).toList();
            assertEquals(new HashSet<>(DOLLAR_SYMBOLS), new HashSet<>(symbols));
        }

        @Test
        void searchForNameWithRegex() {
            var registry = CurrencyRegistry.getInstance();
            registry.reset();

            var results = registry.search("^.*Dollar$");
            assertEquals(6, results.size());

            var symbols = results.stream().map(Currency::symbol).toList();
            assertEquals(new HashSet<>(DOLLAR_SYMBOLS), new HashSet<>(symbols));
        }

        @Test
        void searchForSymbol() {
            var registry = CurrencyRegistry.getInstance();
            var results = registry.search("NZD");

            assertEquals(1, results.size());
            assertEquals("NZD", results.get(0).symbol());
        }

        @Test
        void searchForSymbolWithRegex() {
            var registry = CurrencyRegistry.getInstance();
            registry.reset();

            var results = registry.search("^[A-Z]{2}D$");

            var symbols = results.stream().map(Currency::symbol).toList();
            assertEquals(new HashSet<>(DOLLAR_SYMBOLS), new HashSet<>(symbols));
        }

        @Test
        void searchWithInvalidRegex() {
            var registry = CurrencyRegistry.getInstance();
            assertTrue(registry.search("(").isEmpty());
        }

        @ParameterizedTest
        @NullAndEmptySource
        void searchWithNullOrEmpty(String pattern) {
            var registry = CurrencyRegistry.getInstance();
            assertTrue(registry.search(pattern).isEmpty());
        }
    }
}