/*
 * ProvidersTest.java
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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class ProvidersTest {

    private static Provider provider(String key, String name) {
        return new Provider(key, name, null, null, null, null, null, List.of());
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("handles null collection gracefully")
        void handlesNullCollection() {
            var providers = new Providers(null);
            assertTrue(providers.isEmpty());
            assertEquals(0, providers.size());
        }
    }

    @Nested
    @DisplayName("find()")
    class FindTests {

        @Test
        @DisplayName("finds provider by key, case-insensitive")
        void findsProvider() {
            var providers = new Providers(List.of(provider("ECB", "European Central Bank")));
            assertTrue(providers.find("ecb").isPresent());
            assertTrue(providers.find("ECB").isPresent());
            assertEquals("ECB", providers.find("ecb").get().key());
        }

        @Test
        @DisplayName("returns empty for blank key")
        void handlesBlankKey() {
            var providers = new Providers(List.of(provider("ECB", "European Central Bank")));
            assertTrue(providers.find("").isEmpty());
            assertTrue(providers.find("   ").isEmpty());
        }

        @Test
        @DisplayName("returns empty for null key")
        void handlesNullKey() {
            var providers = new Providers(List.of(provider("ECB", "European Central Bank")));
            assertTrue(providers.find(null).isEmpty());
        }

        @Test
        @DisplayName("returns empty when provider not found")
        void notFound() {
            var providers = new Providers(List.of());
            assertTrue(providers.find("XYZ").isEmpty());
        }
    }

    @Nested
    @DisplayName("fromJson()")
    class FromJsonTests {

        @Test
        @DisplayName("handles empty JSON array")
        void handlesEmptyArray() {
            var providers = Providers.fromJson("[]");
            assertTrue(providers.isEmpty());
            assertEquals(0, providers.size());
        }

        @Test
        @DisplayName("handles null JSON result as empty list")
        void handlesNullJsonResult() {
            var providers = Providers.fromJson("null");
            assertTrue(providers.isEmpty());
        }

        @Test
        @DisplayName("parses valid JSON array")
        void parsesValidJson() {
            var json = """
                    [
                      {"key":"ECB","name":"European Central Bank"},
                      {"key":"FED","name":"Federal Reserve"}
                    ]
                    """;
            var providers = Providers.fromJson(json);
            assertEquals(2, providers.size());
            assertTrue(providers.find("ECB").isPresent());
        }
    }

    @Nested
    @DisplayName("Iterable")
    class IterableTests {

        @Test
        @DisplayName("supports for-each loop")
        void supportsForEach() {
            var p1 = provider("ECB", "European Central Bank");
            var p2 = provider("FED", "Federal Reserve");
            var providers = new Providers(List.of(p1, p2));

            int count = 0;
            for (Provider p : providers) {
                assertNotNull(p);
                count++;
            }
            assertEquals(2, count);
        }
    }

    @Nested
    @DisplayName("list()")
    class ListTests {

        @Test
        @DisplayName("returns all providers")
        void returnsAllProviders() {
            var p1 = provider("ECB", "European Central Bank");
            var p2 = provider("FED", "Federal Reserve");
            var providers = new Providers(List.of(p1, p2));

            var list = providers.list();
            assertEquals(2, list.size());
            assertEquals("ECB", list.get(0).key());
            assertEquals("FED", list.get(1).key());
        }

        @Test
        @DisplayName("returns empty list when no providers")
        void returnsEmptyList() {
            var providers = new Providers(List.of());
            assertTrue(providers.list().isEmpty());
        }

        @Test
        @DisplayName("returns unmodifiable list")
        void returnsUnmodifiableList() {
            var providers = new Providers(List.of(provider("ECB", "European Central Bank")));
            var list = providers.list();

            assertThrows(UnsupportedOperationException.class, () -> list.add(
                    provider("BOE", "Bank of England")
            ));
            assertThrows(UnsupportedOperationException.class, list::clear);
        }
    }

    @Nested
    @DisplayName("searchByName()")
    class SearchTests {

        @Test
        @DisplayName("returns empty list for blank name")
        void handlesBlankName() {
            var providers = new Providers(List.of(provider("ECB", "European Central Bank")));
            assertTrue(providers.searchByName("").isEmpty());
            assertTrue(providers.searchByName("  ").isEmpty());
        }

        @Test
        @DisplayName("returns empty list for null name")
        void handlesNullName() {
            var providers = new Providers(List.of(provider("ECB", "European Central Bank")));
            assertTrue(providers.searchByName(null).isEmpty());
        }

        @Test
        @DisplayName("is locale-safe for Turkish i")
        void localeSafe() {
            var providers = new Providers(List.of(provider("TCMB", "Türkiye Cumhuriyet Merkez Bankası")));
            // "i".toUpperCase() in TR locale becomes "İ", which would break naive contains
            assertEquals(1, providers.searchByName("merkez").size());
        }

        @Test
        @DisplayName("matches substring case-insensitively")
        void matchesSubstring() {
            var providers = new Providers(List.of(provider("ECB", "European Central Bank")));
            var results = providers.searchByName("central");
            assertEquals(1, results.size());
            assertEquals("ECB", results.get(0).key());
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringTests {

        @Test
        @DisplayName("includes provider list in output")
        void includesList() {
            var providers = new Providers(List.of(provider("ECB", "European Central Bank")));
            var str = providers.toString();
            assertTrue(str.startsWith("Providers["));
            assertTrue(str.contains("ECB"));
        }
    }

    @Nested
    @DisplayName("isEmpty() and size()")
    class UtilityTests {

        @Test
        @DisplayName("reports empty state correctly")
        void emptyState() {
            var providers = new Providers(List.of());
            assertTrue(providers.isEmpty());
            assertEquals(0, providers.size());
        }

        @Test
        @DisplayName("reports size correctly")
        void sizeState() {
            var providers = new Providers(List.of(
                    provider("ECB", "European Central Bank"),
                    provider("FED", "Federal Reserve")
            ));
            assertFalse(providers.isEmpty());
            assertEquals(2, providers.size());
        }
    }
}