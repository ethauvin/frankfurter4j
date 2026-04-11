/*
 * CurrencyFormatTest.java
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

package net.thauvin.erik.frankfurter.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurrencyFormatterTest {

    @Nested
    @DisplayName("format()")
    class FormatTests {

        @Test
        @DisplayName("formats using locale rules")
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
        @NullSource
        @EmptySource
        @ValueSource(strings = {"  ", "\t"})
        @DisplayName("rejects null or blank ISO codes")
        void rejectsNullOrBlank(String code) {
            assertThrows(IllegalArgumentException.class,
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
