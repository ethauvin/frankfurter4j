/*
 * CurrencyTest.java
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class CurrencyTest {

    @Nested
    @DisplayName("constructor validation")
    class ConstructorValidation {

        @Test
        @DisplayName("accepts valid currency")
        void acceptsValid() {
            var c = new Currency("USD", "840", "US Dollar", "$",
                    LocalDate.parse("2000-01-01"), null);

            assertEquals("USD", c.isoCode());
            assertEquals("US Dollar", c.name());
        }

        @ParameterizedTest
        @EmptySource
        @ValueSource(strings = {"  "})
        @DisplayName("rejects blank isoCode")
        void rejectsBlankIso(String iso) {
            assertThrows(IllegalArgumentException.class,
                    () -> new Currency(iso, "123", "Name", "$", null, null));
        }

        @ParameterizedTest
        @EmptySource
        @ValueSource(strings = {"  "})
        @DisplayName("rejects blank name")
        void rejectsBlankName(String name) {
            assertThrows(IllegalArgumentException.class,
                    () -> new Currency("USD", "123", name, "$", null, null));
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("rejects null isoCode")
        void rejectsNullIso(String iso) {
            assertThrows(NullPointerException.class,
                    () -> new Currency(iso, "123", "Name", "$", null, null));
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("rejects null name")
        void rejectsNullName(String name) {
            assertThrows(NullPointerException.class,
                    () -> new Currency("USD", "123", name, "$", null, null));
        }
    }
}
