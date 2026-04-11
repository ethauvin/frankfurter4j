/*
 * LocalDateAdapter.java
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

package net.thauvin.erik.frankfurter.json;

import com.google.gson.*;
import org.jspecify.annotations.NullMarked;

import java.lang.reflect.Type;
import java.time.LocalDate;

/**
 * Gson adapter for serializing and deserializing {@link LocalDate} values.
 *
 * <p>This adapter reads and writes ISO‑8601 date strings such as
 * {@code "2026-04-08"}. It is used internally by the Frankfurter client to
 * ensure consistent date handling across all JSON payloads.</p>
 */
@NullMarked
public final class LocalDateAdapter implements JsonDeserializer<LocalDate>, JsonSerializer<LocalDate> {

    /**
     * Deserializes an ISO‑8601 date string into a {@link LocalDate}.
     *
     * @param json the JSON element containing the date string
     * @param type the target type (ignored)
     * @param ctx  the deserialization context (ignored)
     * @return the parsed {@link LocalDate}
     */
    @Override
    public LocalDate deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) {
        return LocalDate.parse(json.getAsString());
    }

    /**
     * Serializes a {@link LocalDate} into its ISO‑8601 string representation.
     *
     * @param date the date to serialize
     * @param type the target type (ignored)
     * @param ctx  the serialization context (ignored)
     * @return a JSON string containing the ISO‑8601 representation of the date
     */
    @Override
    public JsonElement serialize(LocalDate date, Type type, JsonSerializationContext ctx) {
        return new JsonPrimitive(date.toString());
    }
}
