/*
 * Providers.java
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
import edu.umd.cs.findbugs.annotations.NonNull;
import net.thauvin.erik.frankfurter.internal.LocalDateAdapter;

import java.time.LocalDate;
import java.util.*;

/**
 * Represents the set of providers returned by the Frankfurter {@code /providers} endpoint.
 *
 * <p>The API returns a JSON array of provider entries. This type normalizes the
 * structure into a list and provides simple lookup helpers.</p>
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
@SuppressWarnings("ClassCanBeRecord")
public final class Providers implements ProvidersResult, Iterable<Provider> {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    private final List<Provider> list;

    /**
     * Creates a new immutable container for the given list of providers.
     *
     * @param list the list of provider entries
     */
    public Providers(Collection<Provider> list) {
        this.list = list == null ? List.of() : List.copyOf(list);
    }

    @Override
    @NonNull
    public Iterator<Provider> iterator() {
        return list.iterator();
    }

    @Override
    public String toString() {
        return "Providers" + list;
    }

    /**
     * Parses a JSON array of provider entries into a {@link Providers} instance.
     *
     * @param json the JSON response
     * @return the parsed providers
     */
    public static Providers fromJson(String json) {
        var type = new TypeToken<List<Provider>>() {
        }.getType();
        List<Provider> list = GSON.fromJson(json, type);
        return new Providers(list == null ? List.of() : list);
    }

    /**
     * Finds a provider by its key.
     *
     * @param key the provider key
     * @return an optional containing the matching provider
     */
    public Optional<Provider> find(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        return list.stream()
                .filter(p -> key.equalsIgnoreCase(p.key()))
                .findFirst();
    }

    /**
     * Returns {@code true} if there are no providers in the list.
     *
     * @return {@code true} if the list is empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        return list.isEmpty();
    }

    /**
     * Returns all provider entries.
     *
     * @return the list of providers
     */
    public List<Provider> list() {
        return list;
    }

    /**
     * Searches providers by name substring.
     *
     * @param name the substring to match
     * @return the list of matching providers
     */
    public List<Provider> searchByName(String name) {
        if (name == null || name.isBlank()) {
            return List.of();
        }
        var n = name.toLowerCase(Locale.ROOT);
        return list.stream()
                .filter(p -> p.name().toLowerCase(Locale.ROOT).contains(n))
                .toList();
    }

    /**
     * Returns the number of provider entries.
     *
     * @return the number of entries
     */
    public int size() {
        return list.size();
    }
}