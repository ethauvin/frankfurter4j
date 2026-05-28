/*
 * ValidationTest.java
 *
 * Copyright (c) 2025-2026 Erik C. Thauvin (erik@thauvin.net)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of this project nor the names of its contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"NullableProblems", "DataFlowIssue", "ConstantValue", "PMD.AvoidDuplicateLiterals",
        "PMD.UnitTestShouldIncludeAssert"})
final class ValidationTest {

    @Nested
    @DisplayName("formatNullMessage")
    class FormatNullMessageTests {

        @Test
        @DisplayName("formats field name correctly")
        void formatsFieldName() {
            assertEquals("base currency must not be null", Validation.formatNullMessage("base currency"));
        }

        @Test
        @DisplayName("throws for null fieldName")
        void throwsForNullFieldName() {
            assertThrows(NullPointerException.class,
                    () -> Validation.formatNullMessage(null));
        }
    }

    @Nested
    @DisplayName("isNullOrBlank")
    class IsNullOrBlankTests {

        @Test
        @DisplayName("returns false for non-blank")
        void returnsFalseForNonBlank() {
            assertFalse(Validation.isNullOrBlank("USD"));
        }

        @Test
        @DisplayName("returns true for blank")
        void returnsTrueForBlank() {
            assertTrue(Validation.isNullOrBlank(" "));
        }

        @Test
        @DisplayName("returns true for null")
        void returnsTrueForNull() {
            assertTrue(Validation.isNullOrBlank(null));
        }
    }

    @Nested
    @DisplayName("requireAllNonNull(Collection)")
    class RequireAllNonNullCollectionTests {

        @Test
        @DisplayName("accepts a valid collection")
        void acceptsValidCollection() {
            Validation.requireAllNonNull(List.of("A", "B", "C"), "test");
        }

        @Test
        @DisplayName("exception message includes correct index")
        void exceptionMessageIncludesCorrectIndex() {
            var ex = assertThrows(NullPointerException.class,
                    () -> Validation.requireAllNonNull(Arrays.asList("A", null, "C"), "test"));

            assertEquals("test[1] must not be null", ex.getMessage());
        }

        @Test
        @DisplayName("throws for null collection")
        void throwsForNullCollection() {
            assertThrows(NullPointerException.class,
                    () -> Validation.requireAllNonNull(null, "test"));
        }

        @Test
        @DisplayName("throws for null element")
        void throwsForNullElement() {
            assertThrows(NullPointerException.class,
                    () -> Validation.requireAllNonNull(List.of("A", null, "C"), "test"));
        }
    }

    @Nested
    @DisplayName("requireAllNonNull(varargs)")
    class RequireAllNonNullVarargsTests {

        @Test
        @DisplayName("accepts valid varargs")
        void acceptsValidVarargs() {
            Validation.requireAllNonNull("test", "A", "B", "C");
        }

        @Test
        @DisplayName("throws for null array")
        void throwsForNullArray() {
            assertThrows(NullPointerException.class,
                    () -> Validation.requireAllNonNull("test", (String[]) null));
        }

        @Test
        @DisplayName("throws for null element")
        void throwsForNullElement() {
            assertThrows(NullPointerException.class,
                    () -> Validation.requireAllNonNull("test", "A", null, "C"));
        }
    }

    @Nested
    @DisplayName("requireIsoCurrency")
    class RequireIsoCurrencyTests {

        @ParameterizedTest
        @ValueSource(strings = {"USD", "usd", "EuR", "gbp"})
        @DisplayName("parameterized: accepts valid 3-letter codes and normalizes")
        void acceptsAndNormalizesValidCodes(String input) {
            String result = Validation.requireIsoCurrency(input, "base");
            assertEquals(input.toUpperCase(Locale.ROOT), result);
            assertEquals(3, result.length());
        }

        @Test
        @DisplayName("accepts valid ISO code")
        void acceptsValidIsoCode() {
            assertEquals("USD", Validation.requireIsoCurrency("USD", "base"));
        }

        @Test
        @DisplayName("normalizes lowercase to uppercase")
        void normalizesLowercase() {
            assertEquals("USD", Validation.requireIsoCurrency("usd", "base"));
            assertEquals("EUR", Validation.requireIsoCurrency("EuR", "base"));
            assertEquals("GBP", Validation.requireIsoCurrency("gbp", "target"));
        }

        @Test
        @DisplayName("throws for blank")
        void throwsForBlank() {
            assertThrows(IllegalArgumentException.class,
                    () -> Validation.requireIsoCurrency(" ", "base"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"US", "USDD", "US1", "U$D", "EU ", ""})
        @DisplayName("parameterized: throws for invalid codes")
        void throwsForInvalidCodes(String input) {
            assertThrows(IllegalArgumentException.class,
                    () -> Validation.requireIsoCurrency(input, "base"));
        }

        @Test
        @DisplayName("throws for non-letter characters")
        void throwsForNonLetters() {
            assertThrows(IllegalArgumentException.class,
                    () -> Validation.requireIsoCurrency("US1", "base"));
            assertThrows(IllegalArgumentException.class,
                    () -> Validation.requireIsoCurrency("U$D", "base"));
            assertThrows(IllegalArgumentException.class,
                    () -> Validation.requireIsoCurrency("EU ", "base"));
        }

        @Test
        @DisplayName("throws for null")
        void throwsForNull() {
            assertThrows(NullPointerException.class,
                    () -> Validation.requireIsoCurrency(null, "base"));
        }

        @Test
        @DisplayName("throws for wrong length")
        void throwsForWrongLength() {
            assertThrows(IllegalArgumentException.class,
                    () -> Validation.requireIsoCurrency("US", "base"));
            assertThrows(IllegalArgumentException.class,
                    () -> Validation.requireIsoCurrency("USDD", "base"));
        }
    }

    @Nested
    @DisplayName("requireSupportedDate")
    class RequireSupportedDateTests {

        @Test
        @DisplayName("accepts minimum supported date exactly")
        void acceptsMinSupportedDate() {
            var min = LocalDate.of(1994, 1, 4);
            assertEquals(min, Validation.requireSupportedDate(min, "date"));
        }

        @Test
        @DisplayName("accepts valid date")
        void acceptsValidDate() {
            var date = LocalDate.of(2000, 1, 1);
            assertEquals(date, Validation.requireSupportedDate(date, "date"));
        }

        @Test
        @DisplayName("throws for null")
        void throwsForNull() {
            assertThrows(NullPointerException.class,
                    () -> Validation.requireSupportedDate(null, "date"));
        }

        @Test
        @DisplayName("throws for too early date")
        void throwsForTooEarlyDate() {
            var tooEarly = LocalDate.of(1994, 1, 3);
            assertThrows(IllegalArgumentException.class,
                    () -> Validation.requireSupportedDate(tooEarly, "date"));
        }
    }
}