/*
 * FrankfurterEndpoints.java
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import edu.umd.cs.findbugs.annotations.NonNull;
import net.thauvin.erik.frankfurter.json.LocalDateAdapter;
import net.thauvin.erik.frankfurter.models.*;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Provides JSON parsing helpers for Frankfurter API responses.
 *
 * <p>These methods convert raw JSON strings into strongly typed model objects.
 * They are used internally by {@link Frankfurter}.</p>
 */
public final class FrankfurterEndpoints {

    public static final String JSON_MUST_NOT_BE_NULL = Validation.formatNullMessage("json");
    @NonNull
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    /**
     * You can't call the constructor.
     */
    private FrankfurterEndpoints() {
    }

    /**
     * Parses a JSON object of currency entries.
     *
     * @param json the JSON response (must not be null)
     * @return the parsed currencies (never null)
     */
    @NonNull
    public static Currencies parseCurrencies(@NonNull String json) {
        Objects.requireNonNull(json, JSON_MUST_NOT_BE_NULL);
        return Currencies.fromJson(json);
    }

    /**
     * Parses a single currency entry.
     *
     * @param json the JSON response (must not be null)
     * @return the parsed currency (never null)
     */
    @NonNull
    public static Currency parseCurrency(@NonNull String json) {
        Objects.requireNonNull(json, JSON_MUST_NOT_BE_NULL);
        return GSON.fromJson(json, Currency.class);
    }

    /**
     * Parses an error response.
     *
     * @param json   the raw JSON or fallback text (must not be null)
     * @param status the HTTP status code
     * @return the parsed error response (never null)
     */
    @NonNull
    public static ErrorResponse parseError(@NonNull String json, int status) {
        Objects.requireNonNull(json, JSON_MUST_NOT_BE_NULL);

        try {
            return GSON.fromJson(json, ErrorResponse.class);
        } catch (com.google.gson.JsonParseException e) {
            return new ErrorResponse(status, json);
        }
    }

    /**
     * Parses a JSON object of provider entries.
     *
     * @param json the JSON response (must not be null)
     * @return the parsed providers (never null)
     */
    @NonNull
    public static Providers parseProviders(@NonNull String json) {
        Objects.requireNonNull(json, JSON_MUST_NOT_BE_NULL);
        return Providers.fromJson(json);
    }

    /**
     * Parses a JSON array of rate entries.
     *
     * @param json the JSON response (must not be null)
     * @return the parsed exchange rates (never null)
     */
    @NonNull
    public static ExchangeRates parseRates(@NonNull String json) {
        Objects.requireNonNull(json, JSON_MUST_NOT_BE_NULL);

        Type type = new TypeToken<List<Rate>>() {
        }.getType();
        List<Rate> list = GSON.fromJson(json, type);

        return new ExchangeRates(list);
    }

    /**
     * Parses a single rate entry.
     *
     * @param json the JSON response (must not be null)
     * @return the parsed rate (never null)
     */
    @NonNull
    public static Rate parseSingleRate(@NonNull String json) {
        Objects.requireNonNull(json, JSON_MUST_NOT_BE_NULL);
        return GSON.fromJson(json, Rate.class);
    }
}