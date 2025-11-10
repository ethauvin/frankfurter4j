/*
 * CurrencyRegistry.java
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

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import net.thauvin.erik.frankfurter.models.Currency;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * The CurrencyRegistry class serves as a central repository for managing a collection of currencies.
 * <p>
 * Thread safety: All public methods are thread-safe.
 */
public final class CurrencyRegistry {
    /**
     * Default number of currencies available in the registry.
     */
    static final int DEFAULT_CURRENCY_COUNT = 31;

    private static final int PATTERN_CACHE_SIZE = 50;

    // Use ConcurrentHashMap for thread-safe O(1) lookups by symbol
    private final Map<String, Currency> currencyBySymbol;

    // LRU Pattern cache with access order and thread safety
    private final PatternCache patternCache = new PatternCache();

    /**
     * Constructs a CurrencyRegistry with default currencies.
     */
    private CurrencyRegistry() {
        this.currencyBySymbol = new ConcurrentHashMap<>(DEFAULT_CURRENCY_COUNT);
        initializeDefaultCurrencies();
    }

    /**
     * Fetches the collection of available currencies and their corresponding full names
     * from the Frankfurter API.
     *
     * @return A map where the keys are currency symbols (e.g., {@code USD}) and the values
     * are the corresponding currency names (e.g., {@code United States Dollar}).
     * @throws IOException          if an input or output exception occurs during the API request.
     * @throws JsonSyntaxException  if the JSON response from the API does not match the expected format.
     * @throws InterruptedException if the operation is interrupted.
     */
    public static Map<String, String> availableCurrencies()
            throws IOException, JsonSyntaxException, InterruptedException {
        var gson = new GsonBuilder().setPrettyPrinting().create();
        var mapType = new TypeToken<Map<String, String>>() {
        }.getType();

        var json = FrankfurterUtils.fetchUri(URI.create("https://api.frankfurter.dev/v1/currencies"));
        return gson.fromJson(json, mapType);
    }

    /**
     * Gets the default singleton instance of the currency registry.
     *
     * @return The default currency registry
     */
    public static CurrencyRegistry getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Adds a new currency to the registry.
     * <p>
     * If a currency with the same symbol exists, it will be replaced with the new entry.
     *
     * @param currency The currency to add
     * @throws IllegalArgumentException if the currency is {@code null} or has a {@code null} symbol
     */
    public void add(Currency currency) throws IllegalArgumentException {
        if (currency == null || currency.symbol() == null) {
            throw new IllegalArgumentException("Currency and currency symbol cannot be null");
        }

        var upperSymbol = currency.symbol().toUpperCase(Locale.ROOT);
        currencyBySymbol.put(upperSymbol, currency);
    }

    /**
     * Adds a new currency with symbol and name, using {@link Locale#ROOT} as the default locale.
     * <p>
     * Does <strong>not</strong> replace existing currencies and returns {@code false} if the symbol already exists.
     *
     * @param symbol The currency symbol (e.g., "USD")
     * @param name   The currency name (e.g., "United States Dollar")
     * @throws IllegalArgumentException if the symbol is {@code null} or empty
     */
    public void add(String symbol, String name) throws IllegalArgumentException {
        if (symbol == null) {
            throw new IllegalArgumentException("Currency symbol cannot be null");
        }

        var upperSymbol = symbol.toUpperCase(Locale.ROOT);

        // Use putIfAbsent for atomic check-and-add
        currencyBySymbol.putIfAbsent(upperSymbol, new Currency(symbol, name, Locale.ROOT));
    }

    /**
     * Clears the pattern cache to free memory.
     */
    public void clearPatternCache() {
        patternCache.clear();
    }

    /**
     * Checks if any currency matches the given symbol pattern using regular expression (case-insensitive).
     *
     * @param pattern The regular expression pattern to match against currency symbols
     * @return {@code true} if at least one currency matches, {@code false} otherwise or if the pattern is invalid
     */
    public boolean contains(String pattern) {
        return Optional.ofNullable(compilePattern(pattern))
                .map(regex -> currencyBySymbol.keySet().stream().anyMatch(symbol ->
                        regex.matcher(symbol).find()))
                .orElse(false);
    }

    /**
     * Finds a currency by its name using a regular expression pattern (case-insensitive).
     *
     * @param pattern The regular expression pattern to match against currency names
     * @return An Optional containing the first currency that matches, or empty if none found or pattern invalid
     */
    public Optional<Currency> findByName(String pattern) {
        var regex = compilePattern(pattern);
        if (regex == null) {
            return Optional.empty();
        }
        return currencyBySymbol.values().stream()
                .filter(c -> regex.matcher(c.name()).find())
                .findFirst();
    }

    /**
     * Finds a currency by its symbol using a regular expression pattern (case-insensitive).
     *
     * @param pattern The regular expression pattern to match against currency symbols
     * @return An Optional containing the first currency that matches, or empty if none found or pattern invalid
     */
    public Optional<Currency> findBySymbol(String pattern) {
        var regex = compilePattern(pattern);
        if (regex == null) {
            return Optional.empty();
        }
        return currencyBySymbol.keySet().stream()
                .filter(symbol -> regex.matcher(symbol).find())
                .map(currencyBySymbol::get)
                .findFirst();
    }

    /**
     * Returns all currencies in the registry as an unmodifiable list.
     *
     * @return Unmodifiable list of all currencies
     */
    public List<Currency> getAllCurrencies() {
        return List.copyOf(currencyBySymbol.values());
    }

    /**
     * Retrieves all currency symbols available in the registry.
     *
     * @return An unmodifiable list of all currency symbols.
     */
    public List<String> getAllSymbols() {
        return List.copyOf(currencyBySymbol.keySet());
    }

    /**
     * Returns the current size of the pattern cache.
     */
    public int patternCacheSize() {
        return patternCache.size();
    }

    /**
     * Fetches the collection of available currencies and their corresponding full names.
     *
     * @throws IOException         if an input or output exception occurs during the API request
     * @throws JsonSyntaxException if the JSON response from the API does not match the expected format
     */
    public void refresh() throws IOException, JsonSyntaxException, InterruptedException {
        var currencyMap = availableCurrencies();

        currencyMap.forEach(this::add);
    }

    /**
     * Resets the registry to its original default state.
     */
    public void reset() {
        currencyBySymbol.clear();
        clearPatternCache();
        initializeDefaultCurrencies();
    }

    /**
     * Searches for currencies by code or name using a regular expression pattern (case-insensitive).
     *
     * @param pattern The regular expression pattern to search with
     * @return List of matching currencies, or empty list if the pattern is invalid
     */
    public List<Currency> search(String pattern) {
        if (pattern == null || pattern.isBlank()) {
            return Collections.emptyList();
        }
        var regex = compilePattern(pattern);
        if (regex == null) {
            return Collections.emptyList();
        }
        return currencyBySymbol.values().stream()
                .filter(c -> regex.matcher(c.symbol()).find() || regex.matcher(c.name()).find())
                .toList();
    }

    /**
     * Returns the number of currencies in the registry.
     *
     * @return The count of currencies
     */
    public int size() {
        return currencyBySymbol.size();
    }

    /**
     * Compiles a regex pattern with a case-insensitive flag, with LRU caching.
     * Returns null if the pattern is invalid.
     *
     * @param pattern The pattern string
     * @return Compiled Pattern, or null if invalid
     */
    private Pattern compilePattern(String pattern) {
        return patternCache.getOrCompile(pattern);
    }

    private void initializeDefaultCurrencies() {
        add(new Currency("AUD", "Australian Dollar", new Locale("en", "AU")));
        add(new Currency("BGN", "Bulgarian Lev", new Locale("bg", "BG")));
        add(new Currency("BRL", "Brazilian Real", new Locale("pt", "BR")));
        add(new Currency("CAD", "Canadian Dollar", Locale.CANADA));
        add(new Currency("CHF", "Swiss Franc", new Locale("de", "CH")));
        add(new Currency("CNY", "Chinese Renminbi Yuan", Locale.CHINA));
        add(new Currency("CZK", "Czech Koruna", new Locale("cs", "CZ")));
        add(new Currency("DKK", "Danish Krone", new Locale("da", "DK")));
        add(new Currency("EUR", "Euro", Locale.GERMANY));
        add(new Currency("GBP", "British Pound", Locale.UK));
        add(new Currency("HKD", "Hong Kong Dollar", new Locale("zh", "HK")));
        add(new Currency("HUF", "Hungarian Forint", new Locale("hu", "HU")));
        add(new Currency("IDR", "Indonesian Rupiah", new Locale("id", "ID")));
        add(new Currency("ILS", "Israeli New Sheqel", new Locale("he", "IL")));
        add(new Currency("INR", "Indian Rupee", new Locale("hi", "IN")));
        add(new Currency("ISK", "Icelandic Króna", new Locale("is", "IS")));
        add(new Currency("JPY", "Japanese Yen", Locale.JAPAN));
        add(new Currency("KRW", "South Korean Won", Locale.KOREA));
        add(new Currency("MXN", "Mexican Peso", new Locale("es", "MX")));
        add(new Currency("MYR", "Malaysian Ringgit", new Locale("ms", "MY")));
        add(new Currency("NOK", "Norwegian Krone", new Locale("no", "NO")));
        add(new Currency("NZD", "New Zealand Dollar", new Locale("en", "NZ")));
        add(new Currency("PHP", "Philippine Peso", new Locale("fil", "PH")));
        add(new Currency("PLN", "Polish Złoty", new Locale("pl", "PL")));
        add(new Currency("RON", "Romanian Leu", new Locale("ro", "RO")));
        add(new Currency("SEK", "Swedish Krona", new Locale("sv", "SE")));
        add(new Currency("SGD", "Singapore Dollar", new Locale("en", "SG")));
        add(new Currency("THB", "Thai Baht", new Locale("th", "TH")));
        add(new Currency("TRY", "Turkish Lira", new Locale("tr", "TR")));
        add(new Currency("USD", "United States Dollar", Locale.US));
        add(new Currency("ZAR", "South African Rand", new Locale("en", "ZA")));
    }

    // Initialization-on-demand holder idiom for lazy singleton instantiation.
    private static final class Holder {
        static final CurrencyRegistry INSTANCE = new CurrencyRegistry();
    }

    /**
     * Private static PatternCache subclass for thread-safe LRU regex pattern caching.
     */
    private static class PatternCache {
        private final Map<String, Pattern> cache;
        private final ReentrantLock lock = new ReentrantLock();

        PatternCache() {
            this.cache = new LinkedHashMap<>(PATTERN_CACHE_SIZE, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Pattern> eldest) {
                    // Explicitly call the inherited size() method to avoid ambiguity
                    return super.size() > PATTERN_CACHE_SIZE;
                }
            };
        }

        void clear() {
            lock.lock();
            try {
                cache.clear();
            } finally {
                lock.unlock();
            }
        }

        Pattern getOrCompile(String pattern) {
            if (pattern == null || pattern.isBlank()) {
                return null;
            }
            lock.lock();
            try {
                var compiled = cache.get(pattern);
                if (compiled != null) {
                    return compiled;
                }
                try {
                    compiled = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                    cache.put(pattern, compiled);
                    return compiled;
                } catch (PatternSyntaxException e) {
                    return null;
                }
            } finally {
                lock.unlock();
            }
        }

        int size() {
            lock.lock();
            try {
                return cache.size();
            } finally {
                lock.unlock();
            }
        }
    }
}