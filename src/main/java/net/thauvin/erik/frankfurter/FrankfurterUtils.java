/*
 * FrankfurterUtils.java
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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.uwyn.urlencoder.UrlEncoder;
import net.thauvin.erik.frankfurter.exceptions.HttpErrorException;
import net.thauvin.erik.frankfurter.models.Error;
import net.thauvin.erik.httpstatus.Reasons;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utility class for interacting with the Frankfurter API and performing related operations.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 0.9.0
 */
public final class FrankfurterUtils {
    /**
     * The base URL for the Frankfurter API.
     */
    public static final String API_BASE_URL = "https://api.frankfurter.dev/v1/";
    /**
     * The Euro currency code.
     */
    public static final String EUR = "EUR";
    /**
     * The logger for this class.
     */
    public static final Logger LOGGER = Logger.getLogger(FrankfurterUtils.class.getName());
    /**
     * The minimum date supported by the Frankfurter API.
     */
    public static final LocalDate MIN_DATE = LocalDate.of(1994, 1, 4);
    /**
     * Map currency codes to their respective locales for proper formatting
     */
    private static final Map<String, Locale> CURRENCY_LOCALES = new ConcurrentHashMap<>();
    /**
     * Gson instance for parsing JSON responses.
     */
    private static final Gson GSON = new Gson();
    /**
     * Client for executing HTTP requests.
     */
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    static {
        CURRENCY_LOCALES.put("AUD", new Locale("en", "AU"));
        CURRENCY_LOCALES.put("BGN", new Locale("bg", "BG"));
        CURRENCY_LOCALES.put("BRL", new Locale("pt", "BR"));
        CURRENCY_LOCALES.put("CAD", Locale.CANADA);
        CURRENCY_LOCALES.put("CHF", new Locale("de", "CH"));
        CURRENCY_LOCALES.put("CNY", Locale.CHINA);
        CURRENCY_LOCALES.put("CZK", new Locale("cs", "CZ"));
        CURRENCY_LOCALES.put("DKK", new Locale("da", "DK"));
        CURRENCY_LOCALES.put("EUR", Locale.GERMANY);
        CURRENCY_LOCALES.put("GBP", Locale.UK);
        CURRENCY_LOCALES.put("HKD", new Locale("zh", "HK"));
        CURRENCY_LOCALES.put("HUF", new Locale("hu", "HU"));
        CURRENCY_LOCALES.put("IDR", new Locale("id", "ID"));
        CURRENCY_LOCALES.put("ILS", new Locale("he", "IL"));
        CURRENCY_LOCALES.put("INR", new Locale("hi", "IN"));
        CURRENCY_LOCALES.put("ISK", new Locale("is", "IS"));
        CURRENCY_LOCALES.put("JPY", Locale.JAPAN);
        CURRENCY_LOCALES.put("KRW", Locale.KOREA);
        CURRENCY_LOCALES.put("MXN", new Locale("es", "MX"));
        CURRENCY_LOCALES.put("MYR", new Locale("ms", "MY"));
        CURRENCY_LOCALES.put("NOK", new Locale("no", "NO"));
        CURRENCY_LOCALES.put("NZD", new Locale("en", "NZ"));
        CURRENCY_LOCALES.put("PHP", new Locale("fil", "PH"));
        CURRENCY_LOCALES.put("PLN", new Locale("pl", "PL"));
        CURRENCY_LOCALES.put("RON", new Locale("ro", "RO"));
        CURRENCY_LOCALES.put("SEK", new Locale("sv", "SE"));
        CURRENCY_LOCALES.put("SGD", new Locale("en", "SG"));
        CURRENCY_LOCALES.put("THB", new Locale("th", "TH"));
        CURRENCY_LOCALES.put("TRY", new Locale("tr", "TR"));
        CURRENCY_LOCALES.put("USD", Locale.US);
        CURRENCY_LOCALES.put("ZAR", new Locale("en", "ZA"));
    }

    private FrankfurterUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Calculates Easter Monday for a given year.
     * <p>
     * Easter Monday is 1 day after Easter Sunday
     *
     * @param year the year for which to calculate Easter Monday
     * @return {@link LocalDate} representing Easter Monday
     * @see #calculateEasterSunday(int)
     */
    public static LocalDate calculateEasterMonday(int year) {
        var easter = calculateEasterSunday(year);
        return easter.plusDays(1);
    }

    /**
     * Calculates Easter Sunday for a given year using the Western (Gregorian) calendar.
     * <p>
     * Based on the algorithm by Jean Meeus.
     *
     * @param year the year for which to calculate Easter
     * @return {@link LocalDate} representing Easter Sunday
     */
    public static LocalDate calculateEasterSunday(int year) {
        var a = year % 19;
        var b = year / 100;
        var c = year % 100;
        var d = b / 4;
        var e = b % 4;
        var f = (b + 8) / 25;
        var g = (b - f + 1) / 3;
        var h = (19 * a + b - d - g + 15) % 30;
        var i = c / 4;
        var k = c % 4;
        var l = (32 + 2 * e + 2 * i - h - k) % 7;
        var m = (a + 11 * h + 22 * l) / 451;
        var o = h + l - 7 * m + 114;
        var month = o / 31;
        var day = (o % 31) + 1;

        return LocalDate.of(year, month, day);
    }

    /**
     * Calculates Good Friday for a given year.
     * <p>
     * Good Friday is 2 days before Easter Sunday
     *
     * @param year the year for which to calculate Good Friday
     * @return {@link LocalDate} representing Good Friday
     * @see #calculateEasterSunday(int)
     */
    public static LocalDate calculateGoodFriday(int year) {
        var easter = calculateEasterSunday(year);
        return easter.minusDays(2);
    }

    /**
     * Calculates a list of target closing days for a given year.
     * <p>
     * The target closing days are:
     * <ul>
     *     <li>New Year's Day (Jan 1)</li>
     *     <li>Good Friday (2 days before Easter)</li>
     *     <li>Easter Monday (1 day after Easter)</li>
     *     <li>Lobor Day (May 1)</li>
     *     <li>Christmas Day (Dec 25)</li>
     *     <li>Christmas Holiday (Dec 26)</li>
     * </ul>
     *
     * @param year the year for which to generate the list of closing days
     * @return a list of {@link LocalDate} objects representing the closing days in the specified year
     * @see #calculateEasterMonday(int)
     * @see #calculateGoodFriday(int)
     * @see <a href="https://www.ecb.europa.eu/ecb/contacts/working-hours/html/index.en.html">ECB Closing Days</a>
     */
    public static List<LocalDate> closingDays(int year) {
        var closingDays = new ArrayList<LocalDate>();

        // New Year's Day
        closingDays.add(LocalDate.of(year, 1, 1));
        // Good Friday
        closingDays.add(calculateGoodFriday(year));
        // Easter Monday
        closingDays.add(calculateEasterMonday(year));
        // Labor Day
        closingDays.add(LocalDate.of(year, 5, 1));
        // Christmas Day
        closingDays.add(LocalDate.of(year, 12, 25));
        // Christmas Holiday
        closingDays.add(LocalDate.of(year, 12, 26));

        return closingDays;
    }

    /**
     * Sends an HTTP GET request to the specified URI and retrieves the response body as a string.
     * <p>
     * The request is configured to accept JSON responses.
     *
     * @param uri the URI to which the GET request will be sent
     * @return the response body as a string
     * @throws HttpErrorException if the response status code is not 200
     * @throws IOException        if an I/O error occurs during the request
     */
    public static String fetchUri(URI uri) throws IOException, InterruptedException, IllegalArgumentException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(uri.toString());
        }

        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        var response =
                HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() != 200) {
            handleErrorResponse(response, uri);
        }

        return response.body();
    }

    /**
     * Formats a currency amount based on the provided ISO currency symbol and amount.
     *
     * @param symbol the 3-letter ISO currency symbol (e.g., "USD", "EUR")
     * @param amount the monetary amount to format
     * @return a formatted currency string
     * @throws IllegalArgumentException if the currency symbol is unknown or invalid
     */
    public static String formatCurrency(String symbol, Double amount) {
        return formatCurrency(symbol, amount, false);
    }

    /**
     * Formats a currency amount based on the provided ISO currency symbol, amount, and rounding preference.
     *
     * @param symbol  the 3-letter ISO currency symbol (e.g., "USD", "EUR")
     * @param amount  the monetary amount to format
     * @param rounded Whether to round the amount
     * @return a formatted currency string
     * @throws IllegalArgumentException if the currency symbol is unknown or invalid
     */
    public static String formatCurrency(String symbol, Double amount, boolean rounded) {
        var normalizedSymbol = normalizeSymbol(symbol);

        try {
            var locale = CURRENCY_LOCALES.getOrDefault(normalizedSymbol, Locale.getDefault());
            var currencyFormatter = NumberFormat.getCurrencyInstance(locale);
            if (!rounded) {
                currencyFormatter.setMaximumFractionDigits(99); // prevent rounding
            }

            var currency = Currency.getInstance(normalizedSymbol);
            currencyFormatter.setCurrency(currency);

            return currencyFormatter.format(amount);

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown currency symbol: " + normalizedSymbol, e);
        }
    }

    @SuppressWarnings("PMD.ExceptionAsFlowControl")
    private static void handleErrorResponse(HttpResponse<String> response, URI uri) throws IOException {
        try {
            var errorBody = response.body();
            if (errorBody != null && !errorBody.isEmpty()) {
                var error = GSON.fromJson(errorBody, Error.class);
                throw new HttpErrorException(response.statusCode(), error.message(), uri);
            } else {
                throw new HttpErrorException(response.statusCode(),
                        reasonPhrase(response.statusCode(), "No error message provided"), uri);
            }
        } catch (JsonSyntaxException e) {
            throw new HttpErrorException(response.statusCode(),
                    reasonPhrase(response.statusCode(), "Unable to parse error message"), uri, e);
        }
    }

    /**
     * Validates whether the provided symbol is a non-null string consisting of exactly three alphabetical characters
     * (either uppercase or lowercase).
     *
     * @param symbol the string to be validated
     * @return {@code true} if the symbol is non-null and matches the required format of three alphabetical characters,
     * {@code false} otherwise.
     */
    public static boolean isValidSymbol(String symbol) {
        return symbol != null && symbol.matches("[a-zA-Z]{3}");
    }

    /**
     * Determines whether the given date falls on a weekend (Saturday or Sunday).
     *
     * @param date the {@link LocalDate} to evaluate
     * @return {@code true} if the specified date is a Saturday or Sunday, {@code false} otherwise
     */
    public static boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    /**
     * Determines whether the specified date is a working day.
     * <p>
     * A working day is defined as a day that is not a weekend (Saturday or Sunday) and does not fall on a
     * {@link #closingDays(int) closing day}.
     *
     * @param date        the {@link LocalDate} to evaluate
     * @param closingDays a list of {@link LocalDate} objects representing non-working days
     * @return {@code true} if the specified date is a working day, {@code false} otherwise
     * @see #isWeekend(LocalDate)
     * @see #closingDays(int)
     */
    public static boolean isWorkingDay(LocalDate date, List<LocalDate> closingDays) {
        return !isWeekend(date) && !closingDays.contains(date);
    }

    /**
     * Formats a given currency symbol to uppercase if it matches the required format of three alphabetical characters.
     *
     * @param symbol the currency symbol to be formatted, which must consist of exactly three alphabetical characters
     * @return The normalized version of the currency symbol if it is valid
     * @throws IllegalArgumentException if the provided symbol is not exactly three alphabetical characters
     */
    public static String normalizeSymbol(String symbol) {
        if (isValidSymbol(symbol)) {
            return symbol.toUpperCase(Locale.US);
        } else {
            throw new IllegalArgumentException(String.format("Invalid currency symbol: %s", symbol));
        }
    }

    private static String reasonPhrase(int statusCode, String defaultReason) {
        return Reasons.getReasonPhrase(statusCode, defaultReason);
    }

    /**
     * Builds a URI by combining the {@code aPI_BASE_URL base url}, a specified path, and query parameters.
     * <p>
     * The path is appended to the base URL, and query parameters are appended as a query string.
     *
     * @param path  the path to be appended to the base URL; can be null
     * @param query a map of query parameter keys and values; can be null or empty
     * @return a URI constructed from the base URL, the path, and the query parameters
     * @throws URISyntaxException if the resulting URI is invalid
     */
    public static URI uriBuilder(String path, Map<String, String> query) throws URISyntaxException {
        var sb = new StringBuilder(API_BASE_URL);

        if (path != null && !path.isBlank()) {
            sb.append(path);
        }

        if (query != null && !query.isEmpty()) {
            sb.append('?').append(
                    query.entrySet()
                            .stream()
                            .map(entry ->
                                    UrlEncoder.encode(entry.getKey()) + '=' + UrlEncoder.encode(entry.getValue()))
                            .collect(Collectors.joining("&"))
            );
        }

        return new URI(sb.toString());
    }

    /**
     * Validates the provided date.
     * <p>
     * A valid date must be non-null and not earlier than {@code 1994-01-04}.
     *
     * @param date the date to be validated
     * @throws IllegalArgumentException if the date is null or earlier than {@code 1994-01-04}
     */
    public static void validateDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("A valid date is required.");
        }

        if (date.isBefore(MIN_DATE)) {
            throw new IllegalArgumentException(String.format("Dates prior to 1994-01-04 are not supported: %s", date));
        }
    }

    /**
     * Calculates a list of working days between two given dates, excluding weekends (Saturdays and Sundays) and
     * target closing days.
     *
     * @param startDate the starting date of the range (inclusive)
     * @param endDate   the ending date of the range (inclusive)
     * @return a list of {@link LocalDate} objects representing the working days
     * @see #closingDays(int)
     * @see <a href="https://www.ecb.europa.eu/stats/policy_and_exchange_rates/euro_reference_exchange_rates/html/index.en.html">ECB Working Days</a>
     */
    public static List<LocalDate> workingDays(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            return workingDays(endDate, startDate);
        }

        var closingDays = yearsBetween(startDate, endDate).stream()
                .flatMap(year -> closingDays(year).stream())
                .collect(Collectors.toList());

        return startDate.datesUntil(endDate.plusDays(1))
                .filter(date -> isWorkingDay(date, closingDays))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a list of all years in the range between the provided start date and end date, inclusive.
     *
     * @param startDate the starting date of the range (inclusive)
     * @param endDate   the ending date of the range (inclusive)
     * @return a list of integers representing all years in the specified date range
     */
    public static List<Integer> yearsBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            return yearsBetween(endDate, startDate);
        }

        var startYear = startDate.getYear();
        var endYear = endDate.getYear();

        // Add all years from start to end (inclusive)
        return IntStream.rangeClosed(startYear, endYear).boxed().collect(Collectors.toList());
    }
}