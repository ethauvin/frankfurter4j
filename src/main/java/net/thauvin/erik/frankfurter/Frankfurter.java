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
import net.thauvin.erik.frankfurter.internal.GeneratedVersion;
import net.thauvin.erik.frankfurter.internal.JsonParsers;
import net.thauvin.erik.frankfurter.internal.Validation;
import net.thauvin.erik.frankfurter.models.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;

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
 * <p><b>Threading note:</b> All methods in this class block the calling thread until the HTTP
 * request completes. Do not call from reactive/event-loop threads. In servlet containers,
 * consider using a separate executor to avoid exhausting the request thread pool.</p>
 *
 * <p>Instances of this class are thread-safe and may be reused.</p>
 */
public final class Frankfurter {

    private static final URI DEFAULT_API = URI.create("https://api.frankfurter.dev/v2/");
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final String USER_AGENT =
            GeneratedVersion.PROJECT + '/' + GeneratedVersion.VERSION + " (+https://github.com/ethauvin/frankfurter4j)";

    @NonNull
    private final URI baseUri;

    @NonNull
    private final HttpClient client;

    @NonNull
    private final Duration timeout;

    /**
     * Creates a new client using the default API endpoint, default {@link HttpClient}, and 10s timeout.
     */
    public Frankfurter() {
        this(HttpClient.newHttpClient(), DEFAULT_API, DEFAULT_TIMEOUT);
    }

    /**
     * Creates a new client using the given base URI and a default {@link HttpClient}.
     *
     * @param baseUri the base API URI (must not be null)
     */
    public Frankfurter(@NonNull URI baseUri) {
        this(HttpClient.newHttpClient(), baseUri, DEFAULT_TIMEOUT);
    }

    /**
     * Creates a new client using the given HTTP client, base URI, and timeout.
     *
     * @param client  the HTTP client to use (must not be null)
     * @param baseUri the base API URI (must not be null)
     * @param timeout the request timeout (must not be null)
     */
    public Frankfurter(@NonNull HttpClient client, @NonNull URI baseUri, @NonNull Duration timeout) {
        this.client = Objects.requireNonNull(client, Validation.formatNullMessage("client"));
        this.baseUri = normalizeBase(baseUri);
        this.timeout = Objects.requireNonNull(timeout, Validation.formatNullMessage("timeout"));
    }

    /**
     * Creates a new client using the given HTTP client and base URI with default timeout.
     *
     * @param client  the HTTP client to use (must not be null)
     * @param baseUri the base API URI (must not be null)
     */
    public Frankfurter(@NonNull HttpClient client, @NonNull URI baseUri) {
        this(client, baseUri, DEFAULT_TIMEOUT);
    }

    @NonNull
    private static URI normalizeBase(@NonNull URI base) {
        Objects.requireNonNull(base, Validation.formatNullMessage("base"));
        String baseStr = base.toString();
        if (!baseStr.endsWith("/")) {
            baseStr += "/";
        }
        return URI.create(baseStr);
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
     *
     * @throws IOException if a network error occurs
     */
    @NonNull
    public CurrenciesResult getCurrencies() throws IOException {
        return execute(baseUri.resolve("currencies"), JsonParsers::parseCurrencies);
    }

    /**
     * Retrieves metadata for a single currency.
     *
     * @param code the ISO currency code (must not be null or blank)
     * @throws IOException if a network error occurs
     */
    @NonNull
    public CurrencyResult getCurrency(@NonNull String code) throws IOException {
        Validation.requireIsoCurrency(code, "code");
        return execute(baseUri.resolve("currency/").resolve(code), JsonParsers::parseCurrency);
    }

    /**
     * Retrieves the list of available rate providers.
     *
     * @throws IOException if a network error occurs
     */
    @NonNull
    public ProvidersResult getProviders() throws IOException {
        return execute(baseUri.resolve("providers"), JsonParsers::parseProviders);
    }

    /**
     * Retrieves a single exchange rate for the given currency pair.
     *
     * @param base  the base ISO currency code (must not be null or blank)
     * @param quote the quote ISO currency code (must not be null or blank)
     * @throws IOException if a network error occurs
     */
    @NonNull
    public RateResult getRate(@NonNull String base, @NonNull String quote) throws IOException {
        Validation.requireIsoCurrency(base, "base");
        Validation.requireIsoCurrency(quote, "quote");
        return getRate(new RateConfig.Builder().base(base).quote(quote).build());
    }

    /**
     * Retrieves a single exchange rate using the {@code /rate/{base}/{quote}} endpoint.
     *
     * @param config the rate configuration (must not be null)
     * @throws IOException if a network error occurs
     */
    @NonNull
    public RateResult getRate(@NonNull RateConfig config) throws IOException {
        Objects.requireNonNull(config, Validation.formatNullMessage("config"));
        var uri = config.applyTo(baseUri);
        return execute(uri, JsonParsers::parseSingleRate);
    }

    /**
     * Retrieves exchange rates using default configuration.
     *
     * @throws IOException if a network error occurs
     */
    @NonNull
    public RatesResult getRates() throws IOException {
        return getRates(new RatesConfig.Builder().build());
    }

    /**
     * Retrieves exchange rates using the {@code /rates} endpoint.
     *
     * @param config the rates configuration (must not be null)
     * @throws IOException if a network error occurs
     */
    @NonNull
    public RatesResult getRates(@NonNull RatesConfig config) throws IOException {
        Objects.requireNonNull(config, Validation.formatNullMessage("config"));
        var uri = config.applyTo(baseUri.resolve("rates"));
        return execute(uri, JsonParsers::parseRates);
    }

    /**
     * Executes a GET request and parses the response.
     *
     * @param uri           the URI to request (must not be null)
     * @param successParser function to parse successful responses
     * @param <T>           the result type
     * @return the parsed result
     * @throws IOException          if a network error occurs
     * @throws FrankfurterException if the request is interrupted
     */
    @SuppressWarnings("unchecked")
    @NonNull
    private <T> T execute(@NonNull URI uri, @NonNull Function<String, T> successParser) throws IOException {
        Objects.requireNonNull(uri, Validation.formatNullMessage("uri"));
        Objects.requireNonNull(successParser, Validation.formatNullMessage("successParser"));

        var request = HttpRequest.newBuilder(uri)
                .GET()
                .timeout(timeout)
                .header("Accept", "application/json")
                .header("User-Agent", USER_AGENT)
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            // Do not re-interrupt the thread. This library doesn't own the thread.
            // In servlet containers, interrupting the request thread can break the container.
            throw new FrankfurterException("Request interrupted for " + uri, e);
        }

        if (response.statusCode() == 200) {
            return successParser.apply(response.body());
        }

        return (T) JsonParsers.parseError(response.body(), response.statusCode());
    }
}