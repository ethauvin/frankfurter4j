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

@SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
class RatesConfigTest {

    @Nested
    @DisplayName("Date Validation")
    class DateValidation {

        @Test
        @DisplayName("date and from shouldn't be combined")
        void rejectsCombinedDateAnFrom() {
            var cfg = new RatesConfig.Builder()
                    .date(LocalDate.now())
                    .from(LocalDate.now());
            assertThrows(IllegalArgumentException.class, cfg::build);
        }

        @Test
        @DisplayName("date and to shouldn't be combined")
        void rejectsCombinedDateAndTo() {
            var cfg = new RatesConfig.Builder()
                    .date(LocalDate.now())
                    .to(LocalDate.now());
            assertThrows(IllegalArgumentException.class, cfg::build);
        }

        @Test
        @DisplayName("dates shouldn't be combined")
        void rejectsCombinedDates() {
            var cfg = new RatesConfig.Builder()
                    .date(LocalDate.now())
                    .from(LocalDate.now())
                    .to(LocalDate.now());
            assertThrows(IllegalArgumentException.class, cfg::build);
        }
    }

    @Nested
    @DisplayName("equals() and hashCode()")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("config with only base vs empty not equal")
        void configWithOnlyBaseVsEmptyNotEqual() {
            var cfg1 = new RatesConfig.Builder().base("USD").build();
            var cfg2 = new RatesConfig.Builder().build();
            assertNotEquals(cfg1, cfg2);
        }

        @Test
        @DisplayName("different base not equal")
        void differentBaseNotEqual() {
            var cfg1 = new RatesConfig.Builder().base("USD").build();
            var cfg2 = new RatesConfig.Builder().base("GBP").build();
            assertNotEquals(cfg1, cfg2);
        }

        @Test
        @DisplayName("different date not equal")
        void differentDateNotEqual() {
            var cfg1 = new RatesConfig.Builder()
                    .date(LocalDate.of(2024, 1, 1))
                    .build();
            var cfg2 = new RatesConfig.Builder()
                    .date(LocalDate.of(2024, 1, 2))
                    .build();
            assertNotEquals(cfg1, cfg2);
        }

        @Test
        @DisplayName("different from/to not equal")
        void differentFromToNotEqual() {
            var cfg1 = new RatesConfig.Builder()
                    .from(LocalDate.of(2024, 1, 1))
                    .to(LocalDate.of(2024, 1, 31))
                    .build();
            var cfg2 = new RatesConfig.Builder()
                    .from(LocalDate.of(2024, 2, 1))
                    .to(LocalDate.of(2024, 2, 28))
                    .build();
            assertNotEquals(cfg1, cfg2);
        }

        @Test
        @DisplayName("different group not equal")
        void differentGroupNotEqual() {
            var cfg1 = new RatesConfig.Builder()
                    .date(LocalDate.of(2024, 1, 1))
                    .group(Group.WEEK)
                    .build();
            var cfg2 = new RatesConfig.Builder()
                    .date(LocalDate.of(2024, 1, 1))
                    .group(Group.MONTH)
                    .build();
            assertNotEquals(cfg1, cfg2);
        }

        @Test
        @DisplayName("different providers not equal")
        void differentProvidersNotEqual() {
            var cfg1 = new RatesConfig.Builder().providers("ECB").build();
            var cfg2 = new RatesConfig.Builder().providers("BAM").build();
            assertNotEquals(cfg1, cfg2);
        }

        @Test
        @DisplayName("different quotes not equal")
        void differentQuotesNotEqual() {
            var cfg1 = new RatesConfig.Builder().quotes("EUR").build();
            var cfg2 = new RatesConfig.Builder().quotes("GBP").build();
            assertNotEquals(cfg1, cfg2);
        }

        @Test
        @DisplayName("does not equal different class")
        @SuppressWarnings("AssertBetweenInconvertibleTypes")
        void doesNotEqualDifferentClass() {
            var cfg = new RatesConfig.Builder().base("USD").build();
            assertNotEquals("RatesConfig", cfg);
        }

        @Test
        @DisplayName("does not equal null")
        void doesNotEqualNull() {
            var cfg = new RatesConfig.Builder().base("USD").build();
            assertNotEquals(null, cfg);
        }

        @Test
        @DisplayName("empty configs are equal")
        void emptyConfigsAreEqual() {
            var cfg1 = new RatesConfig.Builder().build();
            var cfg2 = new RatesConfig.Builder().build();
            assertEquals(cfg1, cfg2);
            assertEquals(cfg1.hashCode(), cfg2.hashCode());
        }

        @Test
        @DisplayName("equal objects have same hashCode")
        void equalObjectsHaveSameHashCode() {
            var cfg1 = new RatesConfig.Builder()
                    .base("USD")
                    .quotes("EUR", "GBP")
                    .date(LocalDate.of(2024, 1, 15))
                    .providers("ECB", "BANXICO")
                    .group(Group.WEEK)
                    .build();

            var cfg2 = new RatesConfig.Builder()
                    .base("USD")
                    .quotes("EUR", "GBP")
                    .date(LocalDate.of(2024, 1, 15))
                    .providers("ECB", "BANXICO")
                    .group(Group.WEEK)
                    .build();

            assertEquals(cfg1, cfg2);
            assertEquals(cfg1.hashCode(), cfg2.hashCode());
        }

        @Test
        @DisplayName("quotes order matters")
        void quotesOrderMatters() {
            var cfg1 = new RatesConfig.Builder().quotes("EUR", "GBP").build();
            var cfg2 = new RatesConfig.Builder().quotes("GBP", "EUR").build();
            assertNotEquals(cfg1, cfg2, "Map stores encoded string, order affects equality");
        }

        @Test
        @DisplayName("same instance equals itself")
        @SuppressWarnings("EqualsWithItself")
        void sameInstanceEqualsItself() {
            var cfg = new RatesConfig.Builder().base("USD").build();
            assertEquals(cfg, cfg);
        }
    }

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