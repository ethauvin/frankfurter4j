/*
 * Frankfurter.java
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
import net.thauvin.erik.frankfurter.models.*;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Main entry point for interacting with the Frankfurter.dev API.
 *
 * <p>This client provides convenience methods for retrieving:</p>
 * <ul>
 *   <li>currency metadata</li>
 *   <li>provider metadata</li>
 *   <li>exchange rates for a single date or date range</li>
 *   <li>a single exchange rate via the {@code /rate/{base}/{quote}} endpoint</li>
 * </ul>
 *
 * <p>Instances of this class are thread-safe and may be reused.</p>
 */
@NullMarked
public final class Frankfurter {

    private static final URI DEFAULT_API = URI.create("https://api.frankfurter.dev/v2/");
    private final URI baseUri;
    private final HttpClient client;

    /**
     * Creates a new client using the default API endpoint and a default {@link HttpClient}.
     */
    public Frankfurter() {
        this(HttpClient.newHttpClient(), DEFAULT_API);
    }

    /**
     * Creates a new client using the given base URI and a default {@link HttpClient}.
     *
     * @param baseUri the base API URI
     */
    public Frankfurter(URI baseUri) {
        this(HttpClient.newHttpClient(), normalizeBase(baseUri));
    }

    /**
     * Creates a new client using the given HTTP client and base URI.
     *
     * @param client  the HTTP client to use
     * @param baseUri the base API URI
     */
    public Frankfurter(HttpClient client, URI baseUri) {
        this.client = client;
        this.baseUri = normalizeBase(baseUri);
    }

    private static URI normalizeBase(URI base) {
        return base.resolve("./");
    }

    /**
     * Returns the base API URI used by this client.
     *
     * @return the base URI
     */
    public URI getBaseUri() {
        return baseUri;
    }

    /**
     * Returns the underlying {@link HttpClient}.
     *
     * @return the HTTP client
     */
    public HttpClient getClient() {
        return client;
    }

    /**
     * Retrieves the list of supported currencies.
     *
     * @return the parsed {@link Currencies}
     * @throws IOException          if the request fails
     * @throws InterruptedException if the request is interrupted
     * @throws FrankfurterException if the API returns a non-200 status
     */
    public CurrenciesResult getCurrencies() throws IOException, InterruptedException {
        var uri = baseUri.resolve("currencies");
        var response = sendGet(uri);

        if (response.statusCode() != 200) {
            return FrankfurterEndpoints.parseError(response.body(), response.statusCode());
        }

        return FrankfurterEndpoints.parseCurrencies(response.body());
    }

    public CurrencyResult getCurrency(String code)
            throws IOException, InterruptedException {
        Validation.requireIsoCurrency(code, "code");
        var uri = DEFAULT_API.resolve("currency/").resolve(code);
        var response = sendGet(uri);

        if (response.statusCode() != 200) {
            return FrankfurterEndpoints.parseError(response.body(), response.statusCode());
        }

        return FrankfurterEndpoints.parseCurrency(response.body());
    }

    /**
     * Retrieves the list of available rate providers.
     *
     * @return the parsed {@link Providers}
     * @throws IOException          if the request fails
     * @throws InterruptedException if the request is interrupted
     * @throws FrankfurterException if the API returns a non-200 status
     */
    public ProvidersResult getProviders() throws IOException, InterruptedException {
        var uri = baseUri.resolve("providers");
        var response = sendGet(uri);

        if (response.statusCode() != 200) {
            return FrankfurterEndpoints.parseError(response.body(), response.statusCode());
        }

        return FrankfurterEndpoints.parseProviders(response.body());
    }

    public RateResult getRate(String base, String quote) throws IOException, InterruptedException {
        return getRate(new RateConfig.Builder().base(base).quote(quote).build());
    }

    /**
     * Retrieves a single exchange rate using the {@code /rate/{base}/{quote}} endpoint.
     *
     * <p>This method returns a single {@link Rate} rather than a collection.
     * It supports optional historical dates and provider filtering.</p>
     *
     * @param config the configuration describing the request
     * @return the parsed {@link Rate}
     * @throws IOException          if the request fails
     * @throws InterruptedException if the request is interrupted
     * @throws FrankfurterException if the API returns a non-200 status
     */
    public RateResult getRate(RateConfig config)
            throws IOException, InterruptedException {

        var uri = config.applyTo(baseUri);
        var response = sendGet(uri);

        if (response.statusCode() != 200) {
            return FrankfurterEndpoints.parseError(response.body(), response.statusCode());
        }

        return FrankfurterEndpoints.parseSingleRate(response.body());
    }

    public RatesResult getRates() throws IOException, InterruptedException {
        return getRates(new RatesConfig.Builder().build());
    }

    /**
     * Retrieves exchange rates using the {@code /rates} endpoint.
     *
     * <p>The returned value is either an {@link ExchangeRates} instance or an
     * {@link ErrorResponse} if the API returns a non-200 status.</p>
     *
     * @param config the configuration describing the request
     * @return the parsed result
     * @throws IOException          if the request fails
     * @throws InterruptedException if the request is interrupted
     */
    public RatesResult getRates(RatesConfig config)
            throws IOException, InterruptedException {

        var uri = config.applyTo(baseUri.resolve("rates"));
        var response = sendGet(uri);

        if (response.statusCode() != 200) {
            return FrankfurterEndpoints.parseError(response.body(), response.statusCode());
        }

        return FrankfurterEndpoints.parseRates(response.body());
    }

    /**
     * Sends a GET request to the given URI and returns the response.
     *
     * <p>This helper centralizes request construction and ensures consistent
     * headers and response handling across all API methods.</p>
     *
     * @param uri the target URI
     * @return the HTTP response
     * @throws IOException          if the request fails
     * @throws InterruptedException if the request is interrupted
     */
    private HttpResponse<String> sendGet(URI uri)
            throws IOException, InterruptedException {

        var request = HttpRequest.newBuilder(uri)
                .GET()
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json")
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}