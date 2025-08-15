/*
 * AvailableCurrencies.java
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
import net.thauvin.erik.frankfurter.models.Currencies;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * Utility class that provides methods to retrieve information about available currencies.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public final class AvailableCurrencies {
    private AvailableCurrencies() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Fetches the collection of available currencies and their corresponding full names.
     *
     * @return a {@link Currencies} object containing a map of available currency symbols to their full names
     * @throws IOException         if an input or output exception occurs during the aPI request
     * @throws JsonSyntaxException if the JSON response from the aPI does not match the expected format
     */
    public static Currencies getCurrencies() throws IOException, JsonSyntaxException, InterruptedException {
        var gson = new GsonBuilder().setPrettyPrinting().create();
        var currencies = new Currencies();
        var mapType = new TypeToken<Map<String, String>>() {
        }.getType();

        var json = FrankfurterUtils.fetchUri(URI.create("https://api.frankfurter.dev/v1/currencies"));
        Map<String, String> currencyMap = gson.fromJson(json, mapType);
        currencies.putAll(currencyMap);

        return currencies;
    }
}
