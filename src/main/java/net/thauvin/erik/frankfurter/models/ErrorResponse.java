/*
 * ErrorResponse.java
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

import org.jspecify.annotations.Nullable;

/**
 * Represents an error response returned by the Frankfurter API.
 *
 * <p>This type models the structure of error payloads returned by any endpoint.
 * It is used when the API responds with a non‑200 HTTP status code. Instances
 * may originate from parsed JSON or from fallback text when parsing fails.</p>
 *
 * <p>{@code ErrorResponse} participates in all result hierarchies
 * ({@link CurrenciesResult}, {@link CurrencyResult}, {@link ProvidersResult},
 * {@link RatesResult}, {@link RateResult}) so callers can uniformly handle
 * success and error outcomes.</p>
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
@SuppressWarnings("ClassCanBeRecord")
public final class ErrorResponse
        implements CurrenciesResult, CurrencyResult, ProvidersResult, RatesResult, RateResult {

    private final String message;
    private final int status;

    /**
     * Creates a new error response.
     *
     * @param status  the HTTP status code returned by the API
     * @param message the error message, or {@code null} if none was provided
     */
    public ErrorResponse(int status, @Nullable String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * Returns the error message provided by the API.
     *
     * @return the error message, or {@code null} if none was provided
     */
    @Nullable
    public String message() {
        return message;
    }

    /**
     * Returns the HTTP status code associated with this error.
     *
     * @return the HTTP status code
     */
    public int status() {
        return status;
    }
}