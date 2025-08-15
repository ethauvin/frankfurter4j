/*
 * HttpErrorExceptionTests.java
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

package net.thauvin.erik.frankfurter.exceptions;

import com.google.gson.JsonSyntaxException;
import net.thauvin.erik.frankfurter.FrankfurterUtils;
import net.thauvin.erik.frankfurter.LatestRates;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import rife.bld.extension.testing.LoggingExtension;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(LoggingExtension.class)
@SuppressWarnings("PMD.LinguisticNaming")
class HttpErrorExceptionTests {
    @RegisterExtension
    static final LoggingExtension extension = new LoggingExtension(FrankfurterUtils.LOGGER);
    @Test
    void fetchUriNoJson() {
        var uri = URI.create("https://www.google.com/404");
        var exception = assertThrows(HttpErrorException.class,
                () -> FrankfurterUtils.fetchUri(uri));
        assertEquals(404, exception.getStatusCode(), "HTTP status code should be 404");
        assertEquals("Unable to parse error message", exception.getMessage(),
                "HTTP error message should be present");
        assertEquals(uri, exception.getUri(), "HTTP error URI should be set");
        assertNotNull(exception.getCause(), "HTTP error cause should be set");
        assertInstanceOf(JsonSyntaxException.class, exception.getCause(),
                "HTTP error cause should be JsonSyntaxException");
    }

    @Test
    void fetchUriWithOutOfBoundsDate() {
        var uri = URI.create(FrankfurterUtils.API_BASE_URL + "1993-12-31");
        var exception = assertThrows(HttpErrorException.class, () -> FrankfurterUtils.fetchUri(uri));
        assertEquals(404, exception.getStatusCode(), "HTTP status code should be 404");
        assertEquals("not found", exception.getMessage(), "HTTP error message should be set");
        assertEquals(uri, exception.getUri(), "HTTP error URI should be set");
        assertNull(exception.getCause(), "HTTP error cause should be null");
    }

    @Test
    void getRatesWithSameBaseAndSymbol() {
        var latestRates = new LatestRates.Builder().base("USD").symbols("USD").build();
        var exception = assertThrows(HttpErrorException.class, latestRates::getExchangeRates);
        assertEquals(422, exception.getStatusCode(), "HTTP status code should be 422");
        assertEquals("bad currency pair", exception.getMessage(), "HTTP error message should be set");
        assertEquals("https://api.frankfurter.dev/v1/latest?base=USD&symbols=USD",
                exception.getUri().toString(), "HTTP error URI should be set");
        assertNull(exception.getCause(), "HTTP error cause should be null");
    }
}
