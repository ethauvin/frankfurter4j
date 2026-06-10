/*
 * ExchangeRatesConfigTest.java
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

import net.thauvin.erik.frankfurter.config.RatesConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExchangeRatesConfigTest {

    @Nested
    @DisplayName("build()")
    class BuildTests {

        @Test
        @DisplayName("builds valid config")
        void buildsValid() {
            var cfg = new RatesConfig.Builder()
                    .base("EUR")
                    .quotes("USD")
                    .date(LocalDate.parse("2020-01-01"))
                    .build();

            assertNotNull(cfg);
        }
    }

    @Nested
    @DisplayName("currency validation")
    class CurrencyValidation {

        @ParameterizedTest
        @EmptySource
        @ValueSource(strings = {" ", "  ", "US", "USDD"})
        @DisplayName("rejects invalid ISO codes")
        void rejectsInvalidIso(String code) {
            var b = new RatesConfig.Builder();
            assertThrows(IllegalArgumentException.class, () -> b.base(code));
        }
    }

    @Nested
    @DisplayName("date validation")
    class DateValidation {

        @Test
        @DisplayName("rejects invalid dates")
        void rejectsInvalidDates() {
            var b = new RatesConfig.Builder();
            assertThrows(IllegalArgumentException.class, () -> b.date(LocalDate.of(1976, 12, 31)));
        }

        @Test
        @DisplayName("rejects mismatched start/end dates")
        void rejectsMismatchedRange() {
            var b = new RatesConfig.Builder()
                    .base("EUR")
                    .quotes("USD")
                    .from(LocalDate.parse("2020-01-01"))
                    .to(LocalDate.parse("2019-12-31"));

            assertThrows(IllegalArgumentException.class, b::build);
        }
    }
}
