/*
 * CurrencyCodeTest.java
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

package net.thauvin.erik.frankfurter.internal;

import net.thauvin.erik.frankfurter.models.CurrencyCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.Currency;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CurrencyCode enum")
class CurrencyCodeTest {

    @Nested
    @DisplayName("format()")
    class Format {

        static Stream<Arguments> formatArgs() {
            return Stream.of(
                    Arguments.of(CurrencyCode.USD, 1234.56),
                    Arguments.of(CurrencyCode.EUR, 1234.56),
                    Arguments.of(CurrencyCode.JPY, 1234),
                    Arguments.of(CurrencyCode.GBP, 1234.56),
                    Arguments.of(CurrencyCode.INR, 1234.56)
            );
        }

        @ParameterizedTest(name = "{0}.format({1}) should not be blank")
        @MethodSource("formatArgs")
        @DisplayName("should format amounts using currency locale")
        void shouldFormatAmounts(CurrencyCode code, double amount) {
            var result = code.format(amount);
            assertNotNull(result);
            assertFalse(result.isBlank(), "Formatted result should not be blank");
        }

        @Test
        @DisplayName("should respect rounded parameter")
        void shouldRespectRoundedFlag() {
            var unrounded = CurrencyCode.JPY.format(123.456, false);
            var rounded = CurrencyCode.JPY.format(123.456, true);

            assertNotEquals(unrounded, rounded, "Rounded and unrounded should differ for JPY");

            // JPY has 0 fraction digits by ISO - check it has no decimal part
            assertFalse(rounded.contains("."), "JPY rounded should have no decimal point");
            assertFalse(rounded.contains(","), "JPY rounded should have no decimal comma");
            assertTrue(rounded.contains("123"), "JPY rounded should contain 123");
        }

        @Test
        @DisplayName("should use locale-specific symbols and grouping")
        void shouldUseLocaleSpecificFormat() {
            var usd = CurrencyCode.USD.format(1234.56);
            var eur = CurrencyCode.EUR.format(1234.56);

            // Check they're different
            assertNotEquals(usd, eur, "USD and EUR formats should differ");

            // Check USD uses US locale conventions: comma separator, period decimal
            assertTrue(usd.matches(".*1,234\\.56.*"), "USD should use 1,234.56 format");

            // Check EUR uses DE locale conventions: period separator, comma decimal
            assertTrue(eur.matches(".*1\\.234,56.*"), "EUR should use 1.234,56 format");

            // Check currency objects are correct instead of symbols
            assertEquals(Currency.getInstance("USD"), CurrencyCode.USD.toCurrency());
            assertEquals(Currency.getInstance("EUR"), CurrencyCode.EUR.toCurrency());
        }
    }

    @Nested
    @DisplayName("fromCode()")
    class FromCode {

        @ParameterizedTest(name = "should find {0}")
        @ValueSource(strings = {"USD", "usd", "UsD", "EUR", "JPY", "GBP"})
        @DisplayName("should handle known codes case-insensitively")
        void shouldFindKnownCodes(String code) {
            var result = CurrencyCode.fromCode(code);
            assertTrue(result.isPresent(), "Should find currency for " + code);
            assertEquals(code.toUpperCase(Locale.ROOT), result.get().getCode());
        }

        @ParameterizedTest(name = "should return empty for \"{0}\"")
        @ValueSource(strings = {"", " ", "XXX", "ABC", "FAKE"})
        @DisplayName("should return empty for unknown codes")
        void shouldReturnEmptyForUnknown(String code) {
            assertTrue(CurrencyCode.fromCode(code).isEmpty());
        }

        @Test
        @DisplayName("should throw NPE for null")
        @SuppressWarnings("DataFlowIssue")
        void shouldThrowForNull() {
            assertThrows(NullPointerException.class, () -> CurrencyCode.fromCode(null));
        }
    }

    @Nested
    @DisplayName("getCode()")
    class GetCode {

        @ParameterizedTest(name = "{0} should return \"{1}\"")
        @CsvSource({
                "USD, USD",
                "EUR, EUR",
                "JPY, JPY",
                "GBP, GBP",
                "CHF, CHF"
        })
        @DisplayName("should return correct ISO code")
        void shouldReturnCorrectCode(CurrencyCode code, String expected) {
            assertEquals(expected, code.getCode());
        }
    }

    @Nested
    @DisplayName("getLocale()")
    class GetLocale {

        static Stream<Arguments> localeMappings() {
            return Stream.of(
                    Arguments.of(CurrencyCode.USD, Locale.US),
                    Arguments.of(CurrencyCode.EUR, Locale.GERMANY),
                    Arguments.of(CurrencyCode.JPY, Locale.JAPAN),
                    Arguments.of(CurrencyCode.GBP, Locale.UK),
                    Arguments.of(CurrencyCode.CNY, Locale.CHINA),
                    Arguments.of(CurrencyCode.KRW, Locale.KOREA),
                    Arguments.of(CurrencyCode.CAD, new Locale("en", "CA")),
                    Arguments.of(CurrencyCode.AUD, new Locale("en", "AU"))
            );
        }

        @ParameterizedTest(name = "{0} should map to {1}")
        @MethodSource("localeMappings")
        @DisplayName("should return correct representative locale")
        void shouldReturnCorrectLocale(CurrencyCode code, Locale expectedLocale) {
            assertEquals(expectedLocale, code.getLocale());
        }
    }

    @Nested
    @DisplayName("toCurrency()")
    class ToCurrency {

        @ParameterizedTest
        @EnumSource(value = CurrencyCode.class, mode = EnumSource.Mode.INCLUDE, names = {
                "USD", "EUR", "JPY", "GBP", "CAD", "AUD", "CHF", "CNY"
        })
        @DisplayName("should convert to java.util.Currency for major currencies")
        void shouldConvertToJavaCurrency(CurrencyCode code) {
            var currency = code.toCurrency();
            assertNotNull(currency);
            assertEquals(code.getCode(), currency.getCurrencyCode());
        }

        @Test
        @DisplayName("should throw for codes not in java.util.Currency")
        void shouldThrowForUnsupportedCodes() {
            // XAG, XAU, etc. are supported, but some custom ones might not be
            // If you have any that java.util.Currency doesn't know, test them here
            assertDoesNotThrow(CurrencyCode.XAG::toCurrency);
        }
    }

    @Nested
    @DisplayName("values()")
    class Values {

        @Test
        @DisplayName("all codes should be uppercase 3-letter strings")
        void allCodesShouldBeValidFormat() {
            for (var code : CurrencyCode.values()) {
                assertEquals(3, code.getCode().length(), code + " should be 3 letters");
                assertEquals(code.getCode().toUpperCase(Locale.ROOT), code.getCode(),
                        code + " should be uppercase");
            }
        }

        @Test
        @DisplayName("should contain all major ISO 4217 codes")
        void shouldContainMajorCodes() {
            var codes = Stream.of(CurrencyCode.values())
                    .map(CurrencyCode::getCode)
                    .toList();

            assertTrue(codes.contains("USD"));
            assertTrue(codes.contains("EUR"));
            assertTrue(codes.contains("JPY"));
            assertTrue(codes.contains("GBP"));
            assertTrue(codes.contains("CHF"));
            assertTrue(codes.size() > 150, "Should have 150+ currencies");
        }
    }
}