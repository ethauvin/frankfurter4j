/*
 * ExchangeRates.java
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

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a list of exchange rates returned by the Frankfurter API.
 *
 * <p>The API returns a JSON array of rate objects. This wrapper provides
 * convenience methods for searching and inspecting the returned rates.</p>
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public final class ExchangeRates implements RatesResult {

    private final List<Rate> rates;

    /**
     * Creates a new immutable container for the given list of rates.
     *
     * @param rates the list of rate entries
     */
    public ExchangeRates(Collection<Rate> rates) {
        this.rates = List.copyOf(rates);
    }

    @Override
    public String toString() {
        return "ExchangeRates{rates=" + rates + '}';
    }

    /**
     * Finds the first entry matching the given quote currency.
     *
     * @param quote the ISO 4217 quote currency
     * @return an optional containing the matching rate
     */
    public Optional<Rate> find(@NonNull String quote) {
        Objects.requireNonNull(quote, "quote must not be null");
        return rates.stream()
                .filter(r -> r.quote().equalsIgnoreCase(quote))
                .findFirst();
    }

    /**
     * Returns {@code true} if there are no rate entries.
     *
     * @return {@code true} if the list is empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        return rates.isEmpty();
    }

    /**
     * Returns all rate entries.
     *
     * @return the list of rates
     */
    public List<Rate> list() {
        return rates;
    }

    /**
     * Returns the number of rate entries.
     *
     * @return the number of entries
     */
    public int size() {
        return rates.size();
    }
}
