/*
 * FrankfurterTest.java
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

import net.thauvin.erik.frankfurter.config.RateConfig;
import net.thauvin.erik.frankfurter.config.RatesConfig;
import net.thauvin.erik.frankfurter.models.ErrorResponse;
import net.thauvin.erik.frankfurter.models.ExchangeRates;
import net.thauvin.erik.frankfurter.models.Rate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.CloseResource"})
class FrankfurterTest {

    @Nested
    @DisplayName("HTTP Client")
    class HttpClientTests {

        @Test
        @DisplayName("returns the underlying HttpClient")
        void returnsHttpClient() {
            var client = HttpClient.newHttpClient();
            var api = new Frankfurter(client, URI.create("https://example.com/"));
            assertSame(client, api.getClient());
        }
    }

    @Nested
    @DisplayName("HTTP error handling")
    class HttpErrorTest {

        @Test
        @DisplayName("getRate(base, quote) delegates to config-based method")
        void delegatesToConfig() throws Exception {
            var json = """
                    { "date":"2024-01-01", "base":"USD", "quote":"EUR", "rate":1.1 }
                    """;

            var mockClient = mock(HttpClient.class);
            var mockResponse = mock(HttpResponse.class);

            when(mockResponse.statusCode()).thenReturn(200);
            when(mockResponse.body()).thenReturn(json);

            //noinspection unchecked
            when(mockClient.send(any(), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(mockResponse);

            var api = new Frankfurter(mockClient, URI.create("https://example.com/"));
            var result = api.getRate("USD", "EUR");

            assertInstanceOf(Rate.class, result);
            assertEquals("USD", ((Rate) result).base());
        }

        private HttpClient mockClient(int status, String body) throws IOException, InterruptedException {
            var client = mock(HttpClient.class);
            var response = mock(HttpResponse.class);

            when(response.statusCode()).thenReturn(status);
            when(response.body()).thenReturn(body);

            //noinspection unchecked
            when(client.send(any(), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(response);

            return client;
        }

        @Nested
        @DisplayName("getCurrencies()")
        class GetCurrenciesTests {

            @Test
            @DisplayName("Currencies not found")
            void currenciesNotFound() throws Exception {
                var client = mockClient(500, "boom");
                var api = new Frankfurter(client, URI.create("https://example.com/"));

                var result = api.getCurrencies();
                assertInstanceOf(ErrorResponse.class, result);
                assertEquals(500, ((ErrorResponse) result).status());
            }
        }

        @Nested
        @DisplayName("getCurrency()")
        class GetCurrencyTests {

            @Test
            @DisplayName("returns ErrorResponse on non-200")
            void returnsErrorResponse() throws Exception {
                var client = mockClient(400, "{\"status\":400,\"message\":\"Bad\"}");
                var api = new Frankfurter(client, URI.create("https://example.com/"));

                var result = api.getCurrency("USD");
                assertInstanceOf(ErrorResponse.class, result);
                assertEquals(400, ((ErrorResponse) result).status());
            }
        }

        @Nested
        @DisplayName("getProviders()")
        class GetProvidersTests {

            @Test
            @DisplayName("Providers not found")
            void providersNotFound() throws Exception {
                var client = mockClient(404, "nope");
                var api = new Frankfurter(client, URI.create("https://example.com/"));

                var result = api.getProviders();
                assertInstanceOf(ErrorResponse.class, result);
                assertEquals(404, ((ErrorResponse) result).status());
            }
        }

        @Nested
        @DisplayName("getRate(RateConfig)")
        class GetRateConfigTests {

            @Test
            @DisplayName("returns ErrorResponse on non-200")
            void returnsErrorResponse() throws Exception {
                var client = mockClient(418, "{\"status\":418,\"message\":\"I'm a teapot\"}");
                var api = new Frankfurter(client, URI.create("https://example.com/"));

                var cfg = new RateConfig.Builder()
                        .base("USD")
                        .quote("EUR")
                        .build();

                var result = api.getRate(cfg);
                assertInstanceOf(ErrorResponse.class, result);
            }
        }

        @Nested
        @DisplayName("getRate(base, quote)")
        class GetRateOverloadTests {

            @Test
            @DisplayName("returns ErrorResponse on non-200")
            void returnsErrorResponse() throws Exception {
                var client = mockClient(503, "{\"status\":503,\"message\":\"Unavailable\"}");
                var api = new Frankfurter(client, URI.create("https://example.com/"));

                var result = api.getRate("USD", "EUR");
                assertInstanceOf(ErrorResponse.class, result);
            }
        }

        @Nested
        @DisplayName("getRates(RatesConfig)")
        class GetRatesConfigTests {

            @Test
            @DisplayName("returns ErrorResponse on non-200")
            void returnsErrorResponse() throws Exception {
                var client = mockClient(401, "{\"status\":401,\"message\":\"Unauthorized\"}");
                var api = new Frankfurter(client, URI.create("https://example.com/"));

                var cfg = new RatesConfig.Builder()
                        .base("USD")
                        .quotes("EUR")
                        .build();

                var result = api.getRates(cfg);
                assertInstanceOf(ErrorResponse.class, result);
            }
        }

        @Nested
        @DisplayName("getRates() default overload")
        class GetRatesDefaultTests {

            @Test
            @DisplayName("returns ErrorResponse on non-200")
            void returnsErrorResponse() throws Exception {
                var client = mockClient(500, "{\"status\":500,\"message\":\"Oops\"}");
                var api = new Frankfurter(client, URI.create("https://example.com/"));

                var result = api.getRates();
                assertInstanceOf(ErrorResponse.class, result);
            }
        }
    }

    @Nested
    @Tag("integration")
    @DisplayName("real HTTP integration tests")
    class IntegrationTests {

        @Test
        @DisplayName("fetches real currencies from API")
        void fetchesCurrencies() throws Exception {
            var api = new Frankfurter();
            var currencies = api.getCurrencies();

            if (currencies instanceof net.thauvin.erik.frankfurter.models.Currencies c) {

                assertTrue(c.list().size() > 50);
                assertTrue(c.find("USD").isPresent());
            }
        }

        @Test
        @DisplayName("fetches real rates from API")
        void fetchesRates() throws Exception {
            var api = new Frankfurter();

            var cfg = new RatesConfig.Builder()
                    .base("EUR")
                    .quotes("USD")
                    .build();

            var result = api.getRates(cfg);

            assertInstanceOf(ExchangeRates.class, result);
        }
    }

    @Nested
    @DisplayName("mocked HttpClient")
    class MockedTests {

        @Test
        @DisplayName("getCurrencies() parses response")
        void parsesCurrencies() throws Exception {
            var json = """
                    [ { "iso_code":"USD", "iso_numeric":"840", "name":"US Dollar" } ]
                    """;

            var mockClient = mock(HttpClient.class);
            var mockResponse = mock(HttpResponse.class);

            when(mockResponse.statusCode()).thenReturn(200);
            when(mockResponse.body()).thenReturn(json);

            //noinspection unchecked
            when(mockClient.send(any(), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(mockResponse);

            var api = new Frankfurter(mockClient, URI.create("https://example.com/"));
            var currencies = api.getCurrencies();

            if (currencies instanceof net.thauvin.erik.frankfurter.models.Currencies c) {
                assertEquals(1, c.list().size());
                assertEquals("USD", c.list().get(0).isoCode());
            }
        }

        @Test
        @DisplayName("getRates() returns ErrorResponse on non-200")
        void returnsErrorResponse() throws Exception {
            var mockClient = mock(HttpClient.class);
            var mockResponse = mock(HttpResponse.class);

            when(mockResponse.statusCode()).thenReturn(400);
            when(mockResponse.body()).thenReturn("{\"status\":400,\"message\":\"Bad\"}");

            //noinspection unchecked
            when(mockClient.send(any(), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(mockResponse);

            var api = new Frankfurter(mockClient, URI.create("https://example.com/"));

            var cfg = new RatesConfig.Builder()
                    .base("EUR")
                    .quotes("USD")
                    .date(LocalDate.parse("2020-01-01"))
                    .build();

            var result = api.getRates(cfg);
            assertInstanceOf(ErrorResponse.class, result);
        }
    }

    @Nested
    @DisplayName("Timeout Configuration")
    class TimeoutTests {

        @Test
        @DisplayName("Frankfurter accepts HttpClient with valid connect timeout")
        void acceptsValidConnectTimeout() {
            var client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(1))
                    .build();

            assertDoesNotThrow(() -> new Frankfurter(client, URI.create("https://example.com/")));

            var api = new Frankfurter(client, URI.create("https://example.com/"));
            assertTrue(api.getConnectTimeout().isPresent());
            assertEquals(Duration.ofSeconds(1), api.getConnectTimeout().get());
        }

        @Test
        @DisplayName("constructor with both timeouts sets them correctly")
        void customBothTimeouts() {
            var connectTimeout = Duration.ofSeconds(3);
            var requestTimeout = Duration.ofSeconds(15);
            var api = new Frankfurter(connectTimeout, requestTimeout);

            assertTrue(api.getConnectTimeout().isPresent());
            assertEquals(connectTimeout, api.getConnectTimeout().get());
            assertEquals(requestTimeout, api.getRequestTimeout());
        }

        @Test
        @DisplayName("constructor with requestTimeout uses default 5s connect timeout")
        void customRequestTimeoutOnly() {
            var customRequest = Duration.ofSeconds(20);
            var api = new Frankfurter(customRequest);

            assertEquals(customRequest, api.getRequestTimeout());
            assertTrue(api.getConnectTimeout().isPresent());
            assertEquals(Duration.ofSeconds(5), api.getConnectTimeout().get());
        }

        @Test
        @DisplayName("default constructor uses 5s connect and 10s request timeout")
        void defaultTimeouts() {
            var api = new Frankfurter();

            assertEquals(Duration.ofSeconds(10), api.getRequestTimeout());
            assertTrue(api.getConnectTimeout().isPresent());
            assertEquals(Duration.ofSeconds(5), api.getConnectTimeout().get());
        }

        @Test
        @DisplayName("constructor with HttpClient uses its connect timeout")
        void httpClientConnectTimeout() {
            var connectTimeout = Duration.ofSeconds(2);
            var client = HttpClient.newBuilder()
                    .connectTimeout(connectTimeout)
                    .build();

            var api = new Frankfurter(client, URI.create("https://example.com/"));

            assertTrue(api.getConnectTimeout().isPresent());
            assertEquals(connectTimeout, api.getConnectTimeout().get());
            assertEquals(Duration.ofSeconds(10), api.getRequestTimeout());
        }

        @Test
        @DisplayName("HttpClient.Builder itself rejects ZERO connect timeout before Frankfurter sees it")
        void jdkRejectsZeroConnectTimeout() {
            var builder = HttpClient.newBuilder();

            var ex = assertThrows(IllegalArgumentException.class,
                    () -> builder.connectTimeout(Duration.ZERO));

            assertEquals("Invalid duration: PT0S", ex.getMessage());
        }

        @Test
        @DisplayName("getConnectTimeout returns empty when HttpClient has no connect timeout")
        void noConnectTimeoutReturnsEmpty() {
            var client = HttpClient.newHttpClient(); // JDK default has no connect timeout
            var api = new Frankfurter(client, URI.create("https://example.com/"));

            assertTrue(api.getConnectTimeout().isEmpty());
        }

        @Test
        @DisplayName("constructor rejects negative connectTimeout")
        void rejectsNegativeConnectTimeout() {
            var ex = assertThrows(IllegalArgumentException.class,
                    () -> new Frankfurter(Duration.ofSeconds(-5), Duration.ofSeconds(10)));
            assertTrue(ex.getMessage().contains("connectTimeout must be positive"));
        }

        @Test
        @DisplayName("constructor rejects negative requestTimeout")
        void rejectsNegativeRequestTimeout() {
            var ex = assertThrows(IllegalArgumentException.class,
                    () -> new Frankfurter(Duration.ofSeconds(-1)));
            assertTrue(ex.getMessage().contains("requestTimeout must be positive"));
        }

        @Test
        @DisplayName("constructor rejects ZERO connectTimeout")
        void rejectsZeroConnectTimeout() {
            var ex = assertThrows(IllegalArgumentException.class,
                    () -> new Frankfurter(Duration.ZERO, Duration.ofSeconds(10)));
            assertTrue(ex.getMessage().contains("connectTimeout must be positive"));
        }

        @Test
        @DisplayName("constructor rejects ZERO requestTimeout")
        void rejectsZeroRequestTimeout() {
            var ex = assertThrows(IllegalArgumentException.class,
                    () -> new Frankfurter(Duration.ZERO));
            assertTrue(ex.getMessage().contains("requestTimeout must be positive"));
        }

        @Test
        @DisplayName("request timeout is actually applied to HttpRequest")
        @SuppressWarnings("unchecked")
        void requestTimeoutApplied() throws Exception {
            var mockClient = mock(HttpClient.class);
            var mockResponse = mock(HttpResponse.class);
            var timeout = Duration.ofSeconds(7);

            when(mockResponse.statusCode()).thenReturn(200);
            when(mockResponse.body()).thenReturn("[]");

            // Capture the request to verify timeout
            //noinspection unchecked
            when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenAnswer(inv -> {
                        HttpRequest req = inv.getArgument(0);
                        assertTrue(req.timeout().isPresent());
                        assertEquals(timeout, req.timeout().get());
                        return mockResponse;
                    });

            var api = new Frankfurter(mockClient, URI.create("https://example.com/"), timeout);
            api.getCurrencies();

            verify(mockClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
        }
    }

    @Nested
    @DisplayName("URI Handling")
    class URIHandlingTests {

        @Test
        @DisplayName("normalizes base URI")
        void normalizesBaseUri() {
            var api = new Frankfurter(URI.create("https://example.com/v2"));
            assertEquals("https://example.com/v2/", api.getBaseUri().toString());
        }

        @Test
        @DisplayName("normalizes base URI with path")
        void normalizesBaseUriWithPath() {
            var api = new Frankfurter(URI.create("https://example.com/v2/"));
            assertEquals("https://example.com/v2/", api.getBaseUri().toString());
        }
    }

}
