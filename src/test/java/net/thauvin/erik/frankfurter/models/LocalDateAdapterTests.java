/*
 * LocalDateAdapterTests.java
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

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalDateAdapterTests {
    @Test
    void deserializeEmptyDateString() {
        var emptyDateJson = "\"\"";
        var jsonElement = JsonParser.parseString(emptyDateJson);
        var adapter = new LocalDateAdapter();

        assertThrows(DateTimeParseException.class, () ->
                        adapter.deserialize(jsonElement, LocalDate.class, null),
                "Expected to throw DateTimeParseException for empty date string."
        );
    }

    @Test
    void deserializeInvalidDateFormat() {
        var invalidDateJson = "\"15-10-2023\"";
        var jsonElement = JsonParser.parseString(invalidDateJson);
        var adapter = new LocalDateAdapter();

        assertThrows(DateTimeParseException.class, () ->
                        adapter.deserialize(jsonElement, LocalDate.class, null),
                "Expected to throw JsonParseException for invalid date format."
        );
    }

    @Test
    void deserializeNonPrimitiveJsonElement() {
        var nonPrimitiveJson = "{ \"date\": \"2023-10-15\" }";
        var jsonElement = JsonParser.parseString(nonPrimitiveJson);
        var adapter = new LocalDateAdapter();

        // Execution & Assertion
        assertThrows(IllegalStateException.class, () ->
                        adapter.deserialize(jsonElement, LocalDate.class, null),
                "Expected to throw IllegalStateException for non-primitive JSON element."
        );
    }

    @Test
    void deserializeValidDate() {
        var validDateJson = "\"2023-10-15\"";
        var jsonElement = JsonParser.parseString(validDateJson);
        var adapter = new LocalDateAdapter();

        var result = adapter.deserialize(jsonElement, LocalDate.class, null);

        assertEquals(LocalDate.of(2023, 10, 15), result,
                "The deserialized date is incorrect for a valid date.");
    }

    @Test
    void serializeValidDate() {
        var localDate = LocalDate.of(2023, 10, 15);
        var adapter = new LocalDateAdapter();

        var jsonElement = adapter.serialize(localDate, LocalDate.class, null);

        assertEquals("\"2023-10-15\"", jsonElement.toString(),
                "The serialized JSON string is incorrect for a valid LocalDate.");
    }
}