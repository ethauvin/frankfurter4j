/*
 * CurrencyFormatterTest.java
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.text.NumberFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurrencyFormatterTest {

    @Nested
    @DisplayName("format(amount, locale, rounded)")
    class FormatLocaleTests {

        @Test
        @DisplayName("respects rounding flag")
        @DisabledOnOs(OS.WINDOWS)
        void respectsRoundingFlag() {
            var locale = java.util.Locale.GERMANY;

            // rounded = true → default fraction digits (2)
            var rounded = CurrencyFormatter.format(1234.5678, locale, true);
            assertEquals("1.234,57 €", rounded);

            // rounded = false → unlimited fraction digits
            var unrounded = CurrencyFormatter.format(1234.5678, locale, false);
            assertEquals("1.234,5678 €", unrounded);
        }

        @Test
        @DisplayName("rounds and preserves numeric value")
        void roundsAndParsesBack() throws Exception {
            var locale = java.util.Locale.GERMANY;

            var rounded = CurrencyFormatter.format(1234.5678, locale, true);
            var unrounded = CurrencyFormatter.format(1234.5678, locale, false);

            var nf = NumberFormat.getCurrencyInstance(locale);

            var roundedNumber = nf.parse(rounded).doubleValue();
            var unroundedNumber = nf.parse(unrounded).doubleValue();

            // rounded: locale default fraction digits (usually 2)
            assertEquals(1234.57, roundedNumber, 0.0001);

            // unrounded: full precision preserved
            assertEquals(1234.5678, unroundedNumber, 0.0001);
        }
    }

    @Nested
    @DisplayName("format()")
    class FormatTests {

        @Test
        @DisplayName("formats using locale rules")
        @DisabledOnOs(OS.WINDOWS)
        void formatsLocaleAware() {
            var formatted = CurrencyFormatter.format(1234.56, "EUR");
            assertEquals("1.234,56 €", formatted);
        }

        @ParameterizedTest
        @ValueSource(strings = {"usd", "Usd", "USD"})
        @DisplayName("normalizes ISO code to uppercase")
        void normalizesIso(String code) {
            var result = CurrencyFormatter.format(123.45, code);
            assertEquals("$123.45", result);
        }

        @ParameterizedTest
        @EmptySource
        @ValueSource(strings = {" ", "  "})
        @DisplayName("rejects null ISO codes")
        void rejectsEmpty(String code) {
            assertThrows(IllegalArgumentException.class,
                    () -> CurrencyFormatter.format(10.0, code));
        }

        @ParameterizedTest
        @EmptySource
        @ValueSource(strings = {" ", "  ", "\t"})
        @DisplayName("rejects empty or blank ISO codes")
        void rejectsEmptyOrBlank(String code) {
            assertThrows(IllegalArgumentException.class,
                    () -> CurrencyFormatter.format(10.0, code));
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("rejects null ISO codes")
        void rejectsNull(String code) {
            assertThrows(NullPointerException.class,
                    () -> CurrencyFormatter.format(10.0, code));
        }

        @Test
        @DisplayName("rejects unknown ISO codes")
        void rejectsUnknown() {
            assertThrows(IllegalArgumentException.class,
                    () -> CurrencyFormatter.format(10.0, "ZZZ"));
        }
    }
}
