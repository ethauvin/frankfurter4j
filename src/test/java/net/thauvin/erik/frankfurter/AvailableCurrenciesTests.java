/*
 * AvailableCurrenciesTests.java
 *
 * Copyright 2025 Erik C. Thauvin (erik@thauvin.net)
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

import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("PMD.LinguisticNaming")
@ExtendWith(BeforeAllTests.class)
class AvailableCurrenciesTests {
    @Test
    void getCurrencies() throws IOException {
        var currencies = AvailableCurrencies.getCurrencies();
        assertTrue(currencies.size() > 1);
        assertEquals("United States Dollar", currencies.get("USD"));

        // Ensure non-null keys and values
        assertTrue(currencies.entrySet().stream().allMatch(
                entry -> entry.getKey() != null && entry.getValue() != null));
    }

    @Test
    void getCurrenciesWithEmptyResponse() throws IOException {
        try (var mock = Mockito.mockStatic(FrankfurterUtils.class)) {
            mock.when(() -> FrankfurterUtils.fetchUri(URI.create("https://api.frankfurter.dev/v1/currencies")))
                    .thenReturn("{}");

            var currencies = AvailableCurrencies.getCurrencies();
            assertTrue(currencies.isEmpty(), "Currencies list should be empty");
        }
    }

    @Test
    void getCurrenciesWithInvalidJson() {
        try (var mock = Mockito.mockStatic(FrankfurterUtils.class)) {
            mock.when(() -> FrankfurterUtils.fetchUri(URI.create("https://api.frankfurter.dev/v1/currencies")))
                    .thenReturn("INVALID_JSON");

            assertThrows(JsonSyntaxException.class, AvailableCurrencies::getCurrencies,
                    "Expected JsonSyntaxException when invalid JSON is supplied");
        }
    }

    @Test
    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    void privateConstructor() throws Exception {
        var constructor = AvailableCurrencies.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()), "Constructor is not private");
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
        } catch (InvocationTargetException e) {
            assertInstanceOf(IllegalStateException.class, e.getCause(), e.getMessage());
        }
    }
}
