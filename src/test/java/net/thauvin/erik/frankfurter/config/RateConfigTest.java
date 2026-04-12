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
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RateConfigTest {


    @Nested
    @DisplayName("applyTo()")
    class ApplyToTests {

        @Test
        @DisplayName("constructs correct URI")
        void constructsUri() {
            var cfg = new RateConfig.Builder()
                    .base("USD")
                    .quote("EUR")
                    .date(LocalDate.parse("2024-01-01"))
                    .providers("ECB", "BAM")
                    .build();

            var uri = cfg.applyTo(URI.create("https://api.example.com/"));

            assertTrue(uri.toString().contains("/rate/USD/EUR"));
            assertTrue(uri.toString().contains("date=2024-01-01"));
            assertTrue(uri.toString().contains("providers=ECB,BAM"));
        }
    }

    @Nested
    @DisplayName("build()")
    class BuildTests {

        @Test
        @DisplayName("build with blank base")
        void buildWithBlankBase() {
            var b = new RateConfig.Builder();
            assertThrows(IllegalArgumentException.class, () -> b.base(" "));
        }

        @Test
        @DisplayName("build with empty providers list")
        void buildWithEmptyProviders() {
            var b = new RateConfig.Builder().providers(List.of("").toArray(new String[0]));
            assertThrows(IllegalArgumentException.class, b::build);
        }

        @Test
        @DisplayName("builds valid config")
        void buildsValid() {
            var cfg = new RateConfig.Builder()
                    .base("USD")
                    .quote("EUR")
                    .date(LocalDate.parse("2024-01-01"))
                    .providers("ECB")
                    .build();

            assertNotNull(cfg);
        }

        @Test
        @DisplayName("requires base and quote")
        void requiresBaseAndQuote() {
            var b = new RateConfig.Builder();
            assertThrows(IllegalArgumentException.class, b::build);

            b.base("USD");
            assertThrows(IllegalArgumentException.class, b::build);

            b.quote("EUR");
            assertDoesNotThrow(b::build);
        }
    }

    @Nested
    @DisplayName("currency validation")
    class CurrencyValidation {

        @ParameterizedTest
        @EmptySource
        @ValueSource(strings = {"  ", "US", "USDD"})
        @DisplayName("rejects invalid ISO codes")
        void rejectsInvalidIso(String code) {
            var b = new RateConfig.Builder();
            assertThrows(IllegalArgumentException.class, () -> b.base(code));
            assertThrows(IllegalArgumentException.class, () -> b.quote(code));
        }
    }

    @Nested
    @DisplayName("date validation")
    class DateValidation {

        @Test
        @DisplayName("rejects unsupported dates")
        void rejectsUnsupportedDates() {
            var b = new RateConfig.Builder();
            assertThrows(IllegalArgumentException.class,
                    () -> b.date(LocalDate.of(1970, 1, 1)));
        }
    }
}
