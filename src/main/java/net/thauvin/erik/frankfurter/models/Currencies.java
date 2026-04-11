/*
 * Currencies.java
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

package net.thauvin.erik.frankfurter.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.thauvin.erik.frankfurter.json.LocalDateAdapter;
import org.jspecify.annotations.NullMarked;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

/**
 * Represents the set of currencies returned by the Frankfurter API.
 *
 * <p>The API returns a JSON array of currency entries. This type normalizes
 * the structure into a list and provides simple lookup helpers.</p>
 */
@NullMarked
@SuppressWarnings("ClassCanBeRecord")
public final class Currencies implements CurrenciesResult {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(java.time.LocalDate.class, new LocalDateAdapter())
            .create();

    private final List<Currency> list;

    /**
     * Creates a new immutable container for the given list of currencies.
     *
     * @param list the list of currency entries
     */
    public Currencies(Collection<Currency> list) {
        this.list = List.copyOf(list);
    }

    /**
     * Parses a JSON array of currency entries into a {@link Currencies} instance.
     *
     * @param json the JSON response
     * @return the parsed currencies
     */
    public static Currencies fromJson(String json) {
        Type type = new TypeToken<List<Currency>>() {
        }.getType();
        List<Currency> list = GSON.fromJson(json, type);
        return new Currencies(list);
    }

    /**
     * Finds a currency by its ISO code.
     *
     * @param iso the ISO 4217 currency code
     * @return an optional containing the matching currency
     */
    public java.util.Optional<Currency> find(String iso) {
        return list.stream()
                .filter(c -> c.isoCode().equalsIgnoreCase(iso))
                .findFirst();
    }

    /**
     * Returns {@code true} if the list is empty.
     *
     * @return {@code true} if the list is empty
     */
    public boolean isEmpty() {
        return list.isEmpty();
    }

    /**
     * Returns all currency entries.
     *
     * @return the list of currencies
     */
    public List<Currency> list() {
        return list;
    }

    /**
     * Searches currencies by name substring.
     *
     * @param name the substring to match
     * @return the list of matching currencies
     */
    public List<Currency> searchByName(String name) {
        var n = name.toLowerCase();
        return list.stream()
                .filter(c -> c.name().toLowerCase().contains(n))
                .toList();
    }

    /**
     * Returns the number of currencies.
     *
     * @return the number of currencies
     */
    public int size() {
        return list.size();
    }
}
