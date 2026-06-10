/*
 * RatesConfigTest.java
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

import net.thauvin.erik.frankfurter.models.Group;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RatesConfigTest {

    @Nested
    @DisplayName("Builder with Group")
    class GroupValidation {

        @Test
        @DisplayName("allows group with date")
        void allowsGroupWithDate() {
            assertDoesNotThrow(() -> new RatesConfig.Builder()
                    .group(Group.MONTH)
                    .date(LocalDate.of(2025, 6, 1))
                    .build());
        }

        @Test
        @DisplayName("allows group with from")
        void allowsGroupWithFrom() {
            assertDoesNotThrow(() -> new RatesConfig.Builder()
                    .group(Group.WEEK)
                    .from(LocalDate.of(2025, 1, 1))
                    .build());
        }

        @Test
        @DisplayName("allows group with from-to range")
        void allowsGroupWithFromToRange() {
            assertDoesNotThrow(() -> new RatesConfig.Builder()
                    .group(Group.MONTH)
                    .from(LocalDate.of(2025, 1, 1))
                    .to(LocalDate.of(2025, 12, 31))
                    .build());
        }

        @Test
        @DisplayName("allows group with to")
        void allowsGroupWithTo() {
            assertDoesNotThrow(() -> new RatesConfig.Builder()
                    .group(Group.MONTH)
                    .to(LocalDate.of(2025, 12, 31))
                    .build());
        }

        @Test
        @DisplayName("throws when group has no date or range")
        void throwsWhenGroupWithoutDate() {
            RatesConfig.Builder builder = new RatesConfig.Builder()
                    .group(Group.MONTH);

            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    builder::build,
                    "Expected group without date/range to throw"
            );

            assertEquals("group requires a date or date range", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToString {

        @Test
        @DisplayName("handles empty config")
        void handlesEmptyConfig() {
            RatesConfig config = new RatesConfig.Builder().build();
            assertEquals("RatesConfig{}", config.toString());
        }

        @Test
        @DisplayName("includes all params")
        void includesAllParams() {
            RatesConfig config = new RatesConfig.Builder()
                    .base("USD")
                    .quotes("EUR", "GBP")
                    .date(LocalDate.of(2025, 1, 15))
                    .providers("ECB", "BANXICO")
                    .group(Group.WEEK)
                    .build();

            String result = config.toString();

            assertAll(
                    () -> assertTrue(result.startsWith("RatesConfig{")),
                    () -> assertTrue(result.contains("base=USD")),
                    () -> assertTrue(result.contains("quotes=EUR,GBP")),
                    () -> assertTrue(result.contains("date=2025-01-15")),
                    () -> assertTrue(result.contains("providers=ECB,BANXICO")),
                    () -> assertTrue(result.contains("group=week"))
            );
        }
    }
}