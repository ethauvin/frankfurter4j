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

import edu.umd.cs.findbugs.annotations.NonNull;
import net.thauvin.erik.frankfurter.config.RateConfig;
import net.thauvin.erik.frankfurter.config.RatesConfig;
import net.thauvin.erik.frankfurter.models.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

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
public final class Frankfurter {

    private static final URI DEFAULT_API = URI.create("https://api.frankfurter.dev/v2/");

    @NonNull
    private final URI baseUri;

    @NonNull
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
     * @param baseUri the base API URI (must not be null)
     */
    public Frankfurter(@NonNull URI baseUri) {
        this(HttpClient.newHttpClient(), normalizeBase(baseUri));
    }

    /**
     * Creates a new client using the given HTTP client and base URI.
     *
     * @param client  the HTTP client to use (must not be null)
     * @param baseUri the base API URI (must not be null)
     */
    public Frankfurter(@NonNull HttpClient client, @NonNull URI baseUri) {
        this.client = Objects.requireNonNull(client, Validation.formatNullMessage("client"));
        this.baseUri = normalizeBase(baseUri);
    }

    @NonNull
    private static URI normalizeBase(@NonNull URI base) {
        Objects.requireNonNull(base, Validation.formatNullMessage("base"));
        return base.resolve("./");
    }

    /**
     * Returns the base API URI used by this client.
     *
     * @return the base URI (never null)
     */
    @NonNull
    public URI getBaseUri() {
        return baseUri;
    }

    /**
     * Returns the underlying {@link HttpClient}.
     *
     * @return the HTTP client (never null)
     */
    @NonNull
    public HttpClient getClient() {
        return client;
    }

    /**
     * Retrieves the list of supported currencies.
     */
    @NonNull
    public CurrenciesResult getCurrencies() throws IOException, InterruptedException {
        var uri = baseUri.resolve("currencies");
        var response = sendGet(uri);

        if (response.statusCode() != 200) {
            return FrankfurterEndpoints.parseError(response.body(), response.statusCode());
        }

        return FrankfurterEndpoints.parseCurrencies(response.body());
    }

    /**
     * Retrieves metadata for a single currency.
     *
     * @param code the ISO currency code (must not be null or blank)
     */
    @NonNull
    public CurrencyResult getCurrency(@NonNull String code)
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
     */
    @NonNull
    public ProvidersResult getProviders() throws IOException, InterruptedException {
        var uri = baseUri.resolve("providers");
        var response = sendGet(uri);

        if (response.statusCode() != 200) {
            return FrankfurterEndpoints.parseError(response.body(), response.statusCode());
        }

        return FrankfurterEndpoints.parseProviders(response.body());
    }

    @NonNull
    public RateResult getRate(@NonNull String base, @NonNull String quote)
            throws IOException, InterruptedException {

        return getRate(new RateConfig.Builder().base(base).quote(quote).build());
    }

    /**
     * Retrieves a single exchange rate using the {@code /rate/{base}/{quote}} endpoint.
     */
    @NonNull
    public RateResult getRate(@NonNull RateConfig config)
            throws IOException, InterruptedException {
        Objects.requireNonNull(config, Validation.formatNullMessage("config"));

        var uri = config.applyTo(baseUri);
        var response = sendGet(uri);

        if (response.statusCode() != 200) {
            return FrankfurterEndpoints.parseError(response.body(), response.statusCode());
        }

        return FrankfurterEndpoints.parseSingleRate(response.body());
    }

    @NonNull
    public RatesResult getRates() throws IOException, InterruptedException {
        return getRates(new RatesConfig.Builder().build());
    }

    /**
     * Retrieves exchange rates using the {@code /rates} endpoint.
     */
    @NonNull
    public RatesResult getRates(@NonNull RatesConfig config)
            throws IOException, InterruptedException {
        Objects.requireNonNull(config, Validation.formatNullMessage("config"));

        var uri = config.applyTo(baseUri.resolve("rates"));
        var response = sendGet(uri);

        if (response.statusCode() != 200) {
            return FrankfurterEndpoints.parseError(response.body(), response.statusCode());
        }

        return FrankfurterEndpoints.parseRates(response.body());
    }

    /**
     * Sends a GET request to the given URI and returns the response.
     */
    @NonNull
    private HttpResponse<String> sendGet(@NonNull URI uri)
            throws IOException, InterruptedException {
        Objects.requireNonNull(uri, Validation.formatNullMessage("uri"));

        var request = HttpRequest.newBuilder(uri)
                .GET()
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json")
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}