/*
 * CurrencyFormatterTests.java
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class CurrencyFormatterTests {
    @Test
    void formatCurrencyRoundedWithInvalidSymbol() {
        assertThrows(IllegalArgumentException.class,
                () -> CurrencyFormatter.format("INVALID", 100.0, true),
                "Invalid currency code should throw an IllegalArgumentException");
    }

    @ParameterizedTest
    @NullSource
    void formatCurrencyRoundedWithNullCode(String input) {
        assertThrows(IllegalArgumentException.class,
                () -> CurrencyFormatter.format(input, 100.0, true),
                "Null currency code should throw an IllegalArgumentException");
    }

    @Test
    void formatCurrencyRoundedWithUnknownSymbol() {
        var exception = assertThrows(IllegalArgumentException.class,
                () -> CurrencyFormatter.format("FOO", 100.0, true),
                "Invalid currency code should throw an IllegalArgumentException");
        assertTrue(exception.getMessage().contains("Unknown currency"),
                "Exception message should mention unknown currency: " + exception.getMessage());
    }

    @Test
    void formatCurrencyWithInvalidSymbol() {
        assertThrows(IllegalArgumentException.class,
                () -> CurrencyFormatter.format("INVALID", 100.0),
                "Invalid currency code should throw an IllegalArgumentException");
    }

    @ParameterizedTest
    @NullSource
    void formatCurrencyWithNullCode(String input) {
        assertThrows(IllegalArgumentException.class,
                () -> CurrencyFormatter.format(input, 100.0),
                "Null currency code should throw an IllegalArgumentException");
    }

    @Test
    void formatCurrencyWithUnknownSymbol() {
        var exception = assertThrows(IllegalArgumentException.class,
                () -> CurrencyFormatter.format("FOO", 100.0),
                "Invalid currency code should throw an IllegalArgumentException");
        assertTrue(exception.getMessage().contains("Unknown currency"),
                "Exception message should mention unknown currency: " + exception.getMessage());
    }

    @Test
    void formatRoundedWithNegativeAmount() {
        assertEquals(FormatCurrencyUtils.toDollar(-1234.57),
                CurrencyFormatter.format("USD", -1234.567, true),
                "Negative amounts should be formatted correctly with a negative symbol");
    }

    @ParameterizedTest
    @NullSource
    void formatRoundedWithNullAmount(Double input) {
        assertThrows(IllegalArgumentException.class,
                () -> CurrencyFormatter.format("USD", input, true),
                "Null amount should throw a NullPointerException");
    }

    @Test
    void formatRoundedWithValidEUR() {
        assertEquals(FormatCurrencyUtils.toEur(1234.57),
                CurrencyFormatter.format("EUR", 1234.567, true),
                "EUR amount should be formatted correctly in the German locale");
    }

    @Test
    void formatRoundedWithValidGBP() {
        assertEquals(FormatCurrencyUtils.toPound(1234.57),
                CurrencyFormatter.format("GBP", 1234.567, true),
                "GBP amount should be formatted correctly");
    }

    @Test
    void formatRoundedWithValidJPY() {
        assertEquals(FormatCurrencyUtils.toYen(1235.00),
                CurrencyFormatter.format("JPY", 1234.56, true),
                "JPY amount should be formatted as a whole number with proper symbol");
    }

    @Test
    void formatRoundedWithValidUSD() {
        assertEquals(FormatCurrencyUtils.toDollar(1234.57),
                CurrencyFormatter.format("USD", 1234.567, true),
                "USD amount should be formatted correctly");
    }

    @Test
    void formatRoundedWithZeroAmount() {
        assertEquals(FormatCurrencyUtils.toDollar(0.0),
                CurrencyFormatter.format("USD", 0.0, true),
                "Zero amount should be formatted correctly");
    }

    @Test
    @SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
    void formatWithAllSymbols() {
        var registry = CurrencyRegistry.getInstance();
        registry.getAllCurrencies().forEach(currency ->
                assertDoesNotThrow(() -> CurrencyFormatter.format(currency.symbol(), 10.0))
        );
    }

    @Test
    void formatWithNegativeAmount() {
        assertEquals(FormatCurrencyUtils.toDollar(-1234.567),
                CurrencyFormatter.format("USD", -1234.567),
                "Negative amounts should be formatted correctly with a negative symbol");
    }

    @ParameterizedTest
    @NullSource
    void formatWithNullAmount(Double input) {
        assertThrows(IllegalArgumentException.class,
                () -> CurrencyFormatter.format("USD", input),
                "Null amount should throw a NullPointerException");
    }

    @Test
    void formatWithValidEUR() {
        assertEquals(FormatCurrencyUtils.toEur(1234.567),
                CurrencyFormatter.format("EUR", 1234.567),
                "EUR amount should be formatted correctly in the German locale");
    }

    @Test
    void formatWithValidJPY() {
        assertEquals(FormatCurrencyUtils.toYen(1234.567),
                CurrencyFormatter.format("JPY", 1234.567),
                "JPY amount should be formatted as a whole number with proper symbol");
    }

    @Test
    void formatWithValidUSD() {
        assertEquals(FormatCurrencyUtils.toDollar(1234.567),
                CurrencyFormatter.format("USD", 1234.567),
                "USD amount should be formatted correctly");
    }

    @Test
    void formatWithZeroAmount() {
        assertEquals(FormatCurrencyUtils.toDollar(0.0),
                CurrencyFormatter.format("USD", 0.0),
                "Zero amount should be formatted correctly");
    }
}
