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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.thauvin.erik.frankfurter.config.RateConfig;
import net.thauvin.erik.frankfurter.config.RatesConfig;
import net.thauvin.erik.frankfurter.internal.GeneratedVersion;
import net.thauvin.erik.frankfurter.internal.JsonParsers;
import net.thauvin.erik.frankfurter.internal.Validation;
import net.thauvin.erik.frankfurter.models.*;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Client for the Frankfurter.dev exchange rates API.
 *
 * <p>Provides methods to fetch currency metadata, providers, and exchange rates.
 * All calls are blocking. Instances are thread-safe and reusable.</p>
 *
 * <p><b>Timeouts:</b> 5s connect timeout, 10s request timeout by default.
 * Both must be positive.</p>
 *
 * @see <a href="https://frankfurter.dev">Frankfurter.dev API</a>
 */
@NullMarked
@SuppressFBWarnings("EXS_EXCEPTION_SOFTENING_NO_CONSTRAINTS")
public final class Frankfurter {

    private static final URI DEFAULT_API = URI.create("https://api.frankfurter.dev/v2/");
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final String USER_AGENT =
            GeneratedVersion.PROJECT + '/' + GeneratedVersion.VERSION + " (+https://github.com/ethauvin/frankfurter4j)";

    private final URI baseUri;

    private final HttpClient client;

    private final Duration requestTimeout;

    /**
     * Creates a new client using the default API endpoint, default {@link HttpClient} with
     * default timeouts.
     */
    public Frankfurter() {
        this(buildDefaultClient(DEFAULT_CONNECT_TIMEOUT), DEFAULT_API, DEFAULT_REQUEST_TIMEOUT);
    }

    /**
     * Creates a new client using the default API endpoint and connect timeout, with a custom
     * request timeout.
     *
     * @param requestTimeout the request timeout (must be positive, not {@code null})
     * @throws IllegalArgumentException if {@code requestTimeout} is zero or negative
     */
    public Frankfurter(Duration requestTimeout) {
        this(buildDefaultClient(DEFAULT_CONNECT_TIMEOUT), DEFAULT_API, requestTimeout);
    }

    /**
     * Creates a new client using the default API endpoint with custom connect and request timeouts.
     *
     * @param connectTimeout the connect timeout (must be positive, not {@code null})
     * @param requestTimeout the request timeout (must be positive, not {@code null})
     * @throws IllegalArgumentException if either timeout is zero or negative
     */
    public Frankfurter(Duration connectTimeout, Duration requestTimeout) {
        this(buildDefaultClient(connectTimeout), DEFAULT_API, requestTimeout);
    }

    /**
     * Creates a new client using the given base URI, default {@link HttpClient} with default timeouts.
     *
     * @param baseUri the base API URI (must not be {@code null})
     */
    public Frankfurter(URI baseUri) {
        this(buildDefaultClient(DEFAULT_CONNECT_TIMEOUT), baseUri, DEFAULT_REQUEST_TIMEOUT);
    }

    /**
     * Creates a new client using the given HTTP client and base URI with default 10s request timeout.
     *
     * <p>Note: The connect timeout is determined by the provided {@link HttpClient}. If the
     * client was built without an explicit connect timeout, {@link #getConnectTimeout()} will
     * return an empty {@link Optional}.</p>
     *
     * @param client  the HTTP client to use (must not be {@code null})
     * @param baseUri the base API URI (must not be {@code null})
     */
    public Frankfurter(HttpClient client, URI baseUri) {
        this(client, baseUri, DEFAULT_REQUEST_TIMEOUT);
    }

    /**
     * Creates a new client using the given HTTP client, base URI, and request timeout.
     *
     * <p>Note: The connect timeout is determined by the provided {@link HttpClient}. If the
     * client was built without an explicit connect timeout, {@link #getConnectTimeout()} will
     * return an empty {@link Optional}.</p>
     *
     * @param client         the HTTP client to use (must not be {@code null})
     * @param baseUri        the base API URI (must not be {@code null})
     * @param requestTimeout the request timeout (must be positive, not {@code null})
     * @throws IllegalArgumentException if {@code requestTimeout} is zero or negative
     */
    public Frankfurter(HttpClient client, URI baseUri, Duration requestTimeout) {
        this.client = Objects.requireNonNull(client, Validation.formatNullMessage("client"));
        this.baseUri = normalizeBase(baseUri);
        this.requestTimeout = Objects.requireNonNull(requestTimeout, Validation.formatNullMessage("requestTimeout"));
        validateTimeouts();
    }

    private static HttpClient buildDefaultClient(Duration connectTimeout) {
        Objects.requireNonNull(connectTimeout, Validation.formatNullMessage("connectTimeout"));
        if (connectTimeout.isZero() || connectTimeout.isNegative()) {
            throw new IllegalArgumentException("connectTimeout must be positive, got: " + connectTimeout);
        }
        return HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();
    }

    private static URI normalizeBase(URI base) {
        Objects.requireNonNull(base, Validation.formatNullMessage("base"));

        String path = base.getPath();
        if (path == null || path.endsWith("/")) {
            return base;
        }

        try {
            // Rebuild with "/" appended to the path only
            return new URI(
                    base.getScheme(),
                    base.getAuthority(),
                    path + '/',
                    base.getQuery(),
                    base.getFragment()
            );
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Returns the base API URI used by this client.
     *
     * @return the base URI (never null)
     */
    public URI getBaseUri() {
        return baseUri;
    }

    /**
     * Returns the underlying {@link HttpClient}.
     *
     * @return the HTTP client (never null)
     */
    public HttpClient getClient() {
        return client;
    }

    /**
     * Returns the connect timeout configured on the underlying {@link HttpClient}.
     *
     * <p>Returns an empty {@link Optional} if the {@link HttpClient} was built without an
     * explicit connect timeout, which means connections may block indefinitely.</p>
     *
     * @return the connect timeout, or empty if not configured
     */
    public Optional<Duration> getConnectTimeout() {
        return client.connectTimeout();
    }

    /**
     * Retrieves the list of supported currencies.
     *
     * @return the currencies result
     * @throws IOException          if a network error occurs or the request times out
     * @throws InterruptedException if the request is interrupted
     */
    public CurrenciesResult getCurrencies() throws IOException, InterruptedException {
        return execute(baseUri.resolve("currencies"), JsonParsers::parseCurrencies);
    }

    /**
     * Retrieves metadata for a single currency.
     *
     * @param code the ISO currency code (must not be {@code null} or blank)
     * @return the currency result
     * @throws IOException          if a network error occurs or the request times out
     * @throws InterruptedException if the request is interrupted
     */
    public CurrencyResult getCurrency(String code) throws IOException, InterruptedException {
        Validation.requireIsoCurrency("code", code);
        return execute(baseUri.resolve("currency/").resolve(code), JsonParsers::parseCurrency);
    }

    /**
     * Retrieves metadata for a single currency.
     *
     * @param code the currency code (must not be {@code null})
     * @return the currency result
     * @throws IOException          if a network error occurs or the request times out
     * @throws InterruptedException if the request is interrupted
     */
    public CurrencyResult getCurrency(CurrencyCode code) throws IOException, InterruptedException {
        Objects.requireNonNull(code, Validation.formatNullMessage("code"));
        return getCurrency(code.getCode());
    }

    /**
     * Retrieves the list of available rate providers.
     *
     * @return the providers result
     * @throws IOException          if a network error occurs or the request times out
     * @throws InterruptedException if the request is interrupted
     */
    public ProvidersResult getProviders() throws IOException, InterruptedException {
        return execute(baseUri.resolve("providers"), JsonParsers::parseProviders);
    }

    /**
     * Retrieves a single exchange rate for the given currency pair.
     *
     * @param base  the base ISO currency code (must not be {@code null} or blank)
     * @param quote the quote ISO currency code (must not be {@code null} or blank)
     * @return the rate result
     * @throws IOException          if a network error occurs or the request times out
     * @throws InterruptedException if the request is interrupted
     */
    public RateResult getRate(String base, String quote) throws IOException, InterruptedException {
        Validation.requireIsoCurrency("base", base);
        Validation.requireIsoCurrency("quote", quote);
        return getRate(new RateConfig.Builder().base(base).quote(quote).build());
    }

    /**
     * Retrieves a single exchange rate for the given currency pair.
     *
     * @param base  the base currency (must not be {@code null})
     * @param quote the quote currency (must not be {@code null})
     * @return the rate result
     * @throws IOException          if a network error occurs or the request times out
     * @throws InterruptedException if the request is interrupted
     */
    public RateResult getRate(CurrencyCode base, CurrencyCode quote)
            throws IOException, InterruptedException {
        Objects.requireNonNull(base, Validation.formatNullMessage("base"));
        Objects.requireNonNull(quote, Validation.formatNullMessage("quote"));
        return getRate(new RateConfig.Builder().base(base).quote(quote).build());
    }

    /**
     * Retrieves a single exchange rate using the {@code /rate/{base}/{quote}} endpoint.
     *
     * @param config the rate configuration (must not be {@code null})
     * @return the rate result
     * @throws IOException          if a network error occurs or the request times out
     * @throws InterruptedException if the request is interrupted
     */
    public RateResult getRate(RateConfig config) throws IOException, InterruptedException {
        Objects.requireNonNull(config, Validation.formatNullMessage("config"));
        var uri = config.applyTo(baseUri);
        return execute(uri, JsonParsers::parseSingleRate);
    }

    /**
     * Retrieves exchange rates using default configuration.
     *
     * @return the rates result
     * @throws IOException          if a network error occurs or the request times out
     * @throws InterruptedException if the request is interrupted
     */
    public RatesResult getRates() throws IOException, InterruptedException {
        return getRates(new RatesConfig.Builder().build());
    }

    /**
     * Retrieves exchange rates using the {@code /rates} endpoint.
     *
     * @param config the rates configuration (must not be {@code null})
     * @return the rates result
     * @throws IOException          if a network error occurs or the request times out
     * @throws InterruptedException if the request is interrupted
     */
    public RatesResult getRates(RatesConfig config) throws IOException, InterruptedException {
        Objects.requireNonNull(config, Validation.formatNullMessage("config"));
        var uri = config.applyTo(baseUri.resolve("rates"));
        return execute(uri, JsonParsers::parseRates);
    }

    /**
     * Returns the request timeout configured for this client.
     *
     * @return the request timeout (never null)
     */
    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    /**
     * Executes a GET request and parses the response.
     *
     * @param uri           the URI to request (must not be {@code null})
     * @param successParser function to parse successful responses
     * @param <T>           the result type
     * @return the parsed result
     * @throws IOException          if a network error occurs or the request times out
     * @throws InterruptedException if the request is interrupted
     */
    @SuppressWarnings("unchecked")
    private <T> T execute(URI uri, Function<String, T> successParser)
            throws IOException, InterruptedException {
        Objects.requireNonNull(uri, Validation.formatNullMessage("uri"));
        Objects.requireNonNull(successParser, Validation.formatNullMessage("successParser"));

        var request = HttpRequest.newBuilder(uri)
                .GET()
                .timeout(requestTimeout)
                .header("Accept", "application/json")
                .header("User-Agent", USER_AGENT)
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() == 200) {
            return successParser.apply(response.body());
        }

        return (T) JsonParsers.parseError(response.body(), response.statusCode());
    }

    private void validateTimeouts() {
        if (requestTimeout.isZero() || requestTimeout.isNegative()) {
            throw new IllegalArgumentException("requestTimeout must be positive, got: " + requestTimeout);
        }
        client.connectTimeout().ifPresent(ct -> {
            if (ct.isZero() || ct.isNegative()) {
                throw new IllegalArgumentException("HttpClient connectTimeout must be positive, got: " + ct);
            }
        });
    }
}