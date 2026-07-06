/*
 * RateConfigTest.java
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

package net.thauvin.erik.frankfurter.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "DataFlowIssue"})
class RateConfigTest {

    private static final URI BASE_URI = URI.create("https://api.example.com/");

    @Nested
    @DisplayName("applyTo()")
    class ApplyToTests {

        @Test
        @DisplayName("constructs URI with base, date and providers")
        void constructsFullUri() {
            var cfg = new RateConfig.Builder()
                    .base("USD")
                    .quote("EUR")
                    .date(LocalDate.parse("2024-01-01"))
                    .providers("ECB", "BAM")
                    .build();

            var uri = cfg.applyTo(BASE_URI);

            assertEquals("https://api.example.com/rate/USD/EUR?date=2024-01-01&providers=ECB,BAM", uri.toString());
        }

        @Test
        @DisplayName("constructs URI with only quote")
        void constructsMinimalUri() {
            var cfg = new RateConfig.Builder()
                    .quote("EUR")
                    .build();

            var uri = cfg.applyTo(BASE_URI);

            assertEquals("https://api.example.com/rate/EUR", uri.toString());
        }

        @Test
        @DisplayName("preserves fragment from baseUri")
        void preservesFragment() {
            var cfg = new RateConfig.Builder().quote("EUR").build();
            var uri = cfg.applyTo(URI.create("https://api.example.com/#section"));

            assertEquals("https://api.example.com/rate/EUR#section", uri.toString());
        }

        @Test
        @DisplayName("throws NPE when baseUri is {@code null}")
        void throwsOnNullBaseUri() {
            var cfg = new RateConfig.Builder().quote("EUR").build();
            assertThrows(NullPointerException.class, () -> cfg.applyTo(null));
        }
    }

    @Nested
    @DisplayName("applyTo() basePath handling")
    class BasePathTests {

        @Test
        @DisplayName("handles empty path by treating as /")
        void handlesEmptyPath() {
            var cfg = new RateConfig.Builder().quote("EUR").build();
            // URI.create("https://host").getPath() == ""
            var uri = cfg.applyTo(URI.create("https://api.example.com"));

            assertEquals("https://api.example.com/rate/EUR", uri.toString());
        }

        @Test
        @DisplayName("handles path without trailing slash")
        void handlesNoTrailingSlash() {
            var cfg = new RateConfig.Builder().base("USD").quote("EUR").build();
            var uri = cfg.applyTo(URI.create("https://api.example.com/v1"));

            assertEquals("https://api.example.com/v1/rate/USD/EUR", uri.toString());
        }

        @Test
        @DisplayName("handles null path from URI constructor")
        void handlesNullPath() throws URISyntaxException {
            var cfg = new RateConfig.Builder().quote("EUR").build();
            // This actually gives getPath() == null
            var baseUri = new URI("https", "api.example.com", null, null, null);
            var uri = cfg.applyTo(baseUri);

            assertEquals("https://api.example.com/rate/EUR", uri.toString());
        }

        @Test
        @DisplayName("handles root path /")
        void handlesRootPath() {
            var cfg = new RateConfig.Builder().quote("EUR").build();
            var uri = cfg.applyTo(URI.create("https://api.example.com/"));

            assertEquals("https://api.example.com/rate/EUR", uri.toString());
        }

        @Test
        @DisplayName("handles path with trailing slash")
        void handlesTrailingSlash() {
            var cfg = new RateConfig.Builder().base("USD").quote("EUR").build();
            var uri = cfg.applyTo(URI.create("https://api.example.com/v1/"));

            assertEquals("https://api.example.com/v1/rate/USD/EUR", uri.toString());
            assertFalse(uri.toString().contains("v1//rate"), "Should not have double slash");
        }
    }

    @Nested
    @DisplayName("build()")
    class BuildTests {

        @Test
        @DisplayName("allows empty providers array")
        void allowsEmptyProviders() {
            var cfg = new RateConfig.Builder()
                    .quote("EUR")
                    .providers() // empty varargs
                    .build();

            assertNotNull(cfg);
            assertFalse(cfg.applyTo(BASE_URI).toString().contains("providers"));
        }

        @Test
        @DisplayName("builds valid config with all fields")
        void buildsValid() {
            var cfg = new RateConfig.Builder()
                    .base("USD")
                    .quote("EUR")
                    .date(LocalDate.parse("2024-01-01"))
                    .providers("ECB")
                    .build();

            assertNotNull(cfg);
            assertTrue(cfg.toString().contains("base=USD"));
            assertTrue(cfg.toString().contains("quote=EUR"));
        }

        @Test
        @DisplayName("creates defensive copy of providers")
        void defensiveCopyProviders() {
            var providers = new String[]{"ECB"};
            var cfg = new RateConfig.Builder()
                    .quote("EUR")
                    .providers(providers)
                    .build();

            providers[0] = "HACKED"; // mutate original

            var uri = cfg.applyTo(BASE_URI);
            assertTrue(uri.toString().contains("providers=ECB"));
            assertFalse(uri.toString().contains("HACKED"));
        }

        @Test
        @DisplayName("throws IllegalArgumentException when base equals quote")
        void rejectsBaseEqualsQuote() {
            var b = new RateConfig.Builder().base("USD").quote("USD");
            var ex = assertThrows(IllegalArgumentException.class, b::build);
            assertEquals("base and quote currencies must be different", ex.getMessage());
        }

        @Test
        @DisplayName("throws IllegalStateException when quote is missing")
        void requiresQuote() {
            var b = new RateConfig.Builder();
            var ex = assertThrows(IllegalStateException.class, b::build);
            assertEquals("quote currency is required", ex.getMessage());
        }

        @Test
        @DisplayName("throws IllegalStateException when only base is set")
        void requiresQuoteEvenWithBase() {
            var b = new RateConfig.Builder().base("USD");
            assertThrows(IllegalStateException.class, b::build);
        }
    }

    @Nested
    @DisplayName("Builder currency validation")
    class CurrencyValidation {

        @Test
        @DisplayName("accepts valid ISO codes")
        void acceptsValidIso() {
            assertDoesNotThrow(() -> new RateConfig.Builder()
                    .base("USD")
                    .quote("EUR")
                    .build());
        }

        @ParameterizedTest
        @EmptySource
        @ValueSource(strings = {" ", "US", "USDD", "12E"})
        @DisplayName("rejects invalid ISO codes for base")
        void rejectsInvalidBase(String code) {
            var b = new RateConfig.Builder();
            assertThrows(IllegalArgumentException.class, () -> b.base(code));
        }

        @ParameterizedTest
        @EmptySource
        @ValueSource(strings = {" ", "EU", "EURO"})
        @DisplayName("rejects invalid ISO codes for quote")
        void rejectsInvalidQuote(String code) {
            var b = new RateConfig.Builder();
            assertThrows(IllegalArgumentException.class, () -> b.quote(code));
        }
    }

    @Nested
    @DisplayName("Builder date validation")
    class DateValidation {

        @Test
        @DisplayName("accepts supported date")
        void acceptsSupportedDate() {
            assertDoesNotThrow(() -> new RateConfig.Builder()
                    .quote("EUR")
                    .date(LocalDate.of(2000, 1, 1))
                    .build());
        }

        @Test
        @DisplayName("rejects unsupported dates")
        void rejectsUnsupportedDates() {
            var b = new RateConfig.Builder().quote("EUR");
            assertThrows(IllegalArgumentException.class,
                    () -> b.date(LocalDate.of(1970, 1, 1)));
        }
    }

    @Nested
    @DisplayName("equals() and hashCode()")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("different base not equal")
        void differentBaseNotEqual() {
            var cfg1 = new RateConfig.Builder().base("USD").quote("EUR").build();
            var cfg2 = new RateConfig.Builder().base("GBP").quote("EUR").build();
            assertNotEquals(cfg1, cfg2);
        }

        @Test
        @DisplayName("different date not equal")
        void differentDateNotEqual() {
            var cfg1 = new RateConfig.Builder()
                    .quote("EUR")
                    .date(LocalDate.of(2024, 1, 1))
                    .build();
            var cfg2 = new RateConfig.Builder()
                    .quote("EUR")
                    .date(LocalDate.of(2024, 1, 2))
                    .build();
            assertNotEquals(cfg1, cfg2);
        }

        @Test
        @DisplayName("different providers not equal")
        void differentProvidersNotEqual() {
            var cfg1 = new RateConfig.Builder()
                    .quote("EUR")
                    .providers("ECB")
                    .build();
            var cfg2 = new RateConfig.Builder()
                    .quote("EUR")
                    .providers("BAM")
                    .build();
            assertNotEquals(cfg1, cfg2);
        }

        @Test
        @DisplayName("different quote not equal")
        void differentQuoteNotEqual() {
            var cfg1 = new RateConfig.Builder().quote("EUR").build();
            var cfg2 = new RateConfig.Builder().quote("USD").build();
            assertNotEquals(cfg1, cfg2);
        }

        @Test
        @DisplayName("does not equal different class")
        @SuppressWarnings("AssertBetweenInconvertibleTypes")
        void doesNotEqualDifferentClass() {
            var cfg = new RateConfig.Builder().quote("EUR").build();
            assertNotEquals("RateConfig", cfg);
        }

        @Test
        @DisplayName("does not equal null")
        void doesNotEqualNull() {
            var cfg = new RateConfig.Builder().quote("EUR").build();
            assertNotEquals(null, cfg);
        }

        @Test
        @DisplayName("empty providers equals empty providers")
        void emptyProvidersEqualsEmptyProviders() {
            var cfg1 = new RateConfig.Builder().quote("EUR").providers().build();
            var cfg2 = new RateConfig.Builder().quote("EUR").build();
            assertEquals(cfg1, cfg2);
            assertEquals(cfg1.hashCode(), cfg2.hashCode());
        }

        @Test
        @DisplayName("equal objects have same hashCode")
        void equalObjectsHaveSameHashCode() {
            var cfg1 = new RateConfig.Builder()
                    .base("USD")
                    .quote("EUR")
                    .date(LocalDate.of(2024, 1, 15))
                    .providers("ECB", "BAM")
                    .build();

            var cfg2 = new RateConfig.Builder()
                    .base("USD")
                    .quote("EUR")
                    .date(LocalDate.of(2024, 1, 15))
                    .providers("ECB", "BAM")
                    .build();

            assertEquals(cfg1, cfg2);
            assertEquals(cfg1.hashCode(), cfg2.hashCode());
        }

        @Test
        @DisplayName("null base equals null base")
        void nullBaseEqualsNullBase() {
            var cfg1 = new RateConfig.Builder().quote("EUR").build();
            var cfg2 = new RateConfig.Builder().quote("EUR").build();
            assertEquals(cfg1, cfg2);
        }

        @Test
        @DisplayName("null base not equal to non-null base")
        void nullBaseNotEqualToNonNullBase() {
            var cfg1 = new RateConfig.Builder().quote("EUR").build();
            var cfg2 = new RateConfig.Builder().base("USD").quote("EUR").build();
            assertNotEquals(cfg1, cfg2);
        }

        @Test
        @DisplayName("null date equals null date")
        void nullDateEqualsNullDate() {
            var cfg1 = new RateConfig.Builder().quote("EUR").build();
            var cfg2 = new RateConfig.Builder().quote("EUR").build();
            assertEquals(cfg1, cfg2);
        }

        @Test
        @DisplayName("providers order matters")
        void providersOrderMatters() {
            var cfg1 = new RateConfig.Builder()
                    .quote("EUR")
                    .providers("ECB", "BAM")
                    .build();
            var cfg2 = new RateConfig.Builder()
                    .quote("EUR")
                    .providers("BAM", "ECB")
                    .build();
            assertNotEquals(cfg1, cfg2, "Arrays.equals is order-sensitive");
        }

        @Test
        @DisplayName("same instance equals itself")
        @SuppressWarnings("EqualsWithItself")
        void sameInstanceEqualsItself() {
            var cfg = new RateConfig.Builder().quote("EUR").build();
            assertEquals(cfg, cfg);
        }
    }

    @Nested
    @DisplayName("Builder providers validation")
    class ProvidersValidation {

        @Test
        @DisplayName("rejects null providers array")
        void rejectsNullArray() {
            var b = new RateConfig.Builder().quote("EUR");
            assertThrows(NullPointerException.class, () -> b.providers((String[]) null));
        }

        @Test
        @DisplayName("rejects null provider element")
        void rejectsNullElement() {
            var b = new RateConfig.Builder().quote("EUR");
            assertThrows(NullPointerException.class, () -> b.providers("ECB", null));
        }

        @Test
        @DisplayName("removes duplicate providers")
        void removesDuplicates() {
            var cfg = new RateConfig.Builder()
                    .quote("EUR")
                    .providers("ECB", "ECB", "BAM", "ecb") // depends on Validation impl
                    .build();

            var uri = cfg.applyTo(BASE_URI);
            // Assuming Validation.requireNonBlankDistinct is case-sensitive
            assertTrue(uri.toString().contains("providers=ECB,BAM,ecb"));
            assertFalse(uri.toString().matches(".*ECB.*ECB.*"));
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringTests {

        @Test
        @DisplayName("includes all fields")
        void includesAllFields() {
            var cfg = new RateConfig.Builder()
                    .base("USD")
                    .quote("EUR")
                    .date(LocalDate.parse("2024-01-01"))
                    .providers("ECB")
                    .build();

            var str = cfg.toString();
            assertTrue(str.startsWith("RateConfig{"));
            assertTrue(str.contains("base=USD"));
            assertTrue(str.contains("quote=EUR"));
            assertTrue(str.contains("date=2024-01-01"));
            assertTrue(str.contains("providers=[ECB]"));
        }
    }
}