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
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import edu.umd.cs.findbugs.annotations.NonNull;
import net.thauvin.erik.frankfurter.internal.LocalDateAdapter;
import net.thauvin.erik.frankfurter.internal.Validation;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the set of providers returned by the Frankfurter {@code /providers} endpoint.
 *
 * <p>The API returns a JSON array of provider entries. This type normalizes the
 * structure into a list and provides simple lookup helpers.</p>
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @apiNote This class is immutable and thread-safe.
 * @since 1.0
 */
@SuppressWarnings("ClassCanBeRecord")
public final class Providers implements ProvidersResult, Iterable<Provider> {

    @NonNull
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    private static final int TO_STRING_PREVIEW_LIMIT = 10;

    @NonNull
    private final List<Provider> list;

    /**
     * Creates a new immutable container for the given list of providers.
     *
     * @param providers the list of provider entries
     * @throws NullPointerException if {@code providers} is {@code null} or contains null elements
     */
    public Providers(@NonNull Collection<Provider> providers) {
        Validation.requireAllNonNull("providers", providers);
        this.list = List.copyOf(providers);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof Providers that && list.equals(that.list);
    }

    /**
     * Returns a string representation showing size and preview some entries for debugging.
     *
     * @return a string with class name, size, and preview of entries
     */
    @Override
    public String toString() {
        if (list.isEmpty()) {
            return "Providers{size=0}";
        }
        var preview = list.stream()
                .limit(TO_STRING_PREVIEW_LIMIT)
                .map(Provider::toString)
                .collect(Collectors.joining(", "));
        var suffix = list.size() > TO_STRING_PREVIEW_LIMIT ? ", ..." : "";
        return "Providers{size=" + list.size() + ", providers=[" + preview + suffix + "]}";
    }

    @Override
    @NonNull
    public Iterator<Provider> iterator() {
        return list.iterator();
    }

    /**
     * Parses a JSON array of provider entries into a {@link Providers} instance.
     *
     * @param json the JSON response
     * @return the parsed providers
     * @throws NullPointerException     if {@code json} is {@code null}
     * @throws IllegalArgumentException if {@code json} is malformed or contains null elements
     */
    @NonNull
    public static Providers fromJson(@NonNull String json) {
        Objects.requireNonNull(json, Validation.formatNullMessage("json"));
        try {
            Type type = new TypeToken<List<Provider>>() {
            }.getType();
            List<Provider> parsed = GSON.fromJson(json, type);
            if (parsed == null) {
                return new Providers(List.of());
            }
            if (parsed.stream().anyMatch(Objects::isNull)) {
                throw new IllegalArgumentException("Invalid providers JSON: contains null element");
            }
            return new Providers(parsed);
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Invalid providers JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Finds a provider by its key.
     *
     * @param key the provider key
     * @return an optional containing the matching provider
     * @throws NullPointerException if {@code key} is {@code null}
     */
    public Optional<Provider> find(@NonNull String key) {
        Objects.requireNonNull(key, Validation.formatNullMessage("key"));
        return list.stream()
                .filter(p -> p.key().equalsIgnoreCase(key))
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
    @NonNull
    public List<Provider> list() {
        return list;
    }

    /**
     * Searches providers by name substring.
     *
     * @param name the substring to match
     * @return the list of matching providers if any
     * @throws NullPointerException if {@code name} is {@code null}
     */
    @NonNull
    public List<Provider> searchByName(@NonNull String name) {
        Objects.requireNonNull(name, Validation.formatNullMessage("name"));
        if (name.isEmpty()) {
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
