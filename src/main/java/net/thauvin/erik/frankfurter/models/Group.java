/*
 * Group.java
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

import com.google.gson.annotations.SerializedName;
import edu.umd.cs.findbugs.annotations.NonNull;
import net.thauvin.erik.frankfurter.internal.Validation;

import java.util.Objects;

/**
 * Represents the time period by which exchange rates can be downsampled.
 *
 * <p>Used as an optional query parameter when requesting rates over a date
 * range. When specified, rates are aggregated over the given time period
 * rather than returned as daily values.</p>
 *
 * <p>The API accepts the lowercase string values {@code "week"} and
 * {@code "month"} for this parameter.</p>
 *
 * @author Erik C. Thauvin
 * @since 1.0
 */
public enum Group {

    /**
     * Downsample rates to one value per week.
     */
    @SerializedName("week")
    WEEK("week"),

    /**
     * Downsample rates to one value per month.
     */
    @SerializedName("month")
    MONTH("month");

    @NonNull
    private final String value;

    /**
     * Creates a new grouping value.
     *
     * <p>The constructor enforces that the internal string representation is
     * never {@code null}, ensuring that {@link #value()} always returns a
     * valid API parameter.</p>
     *
     * @param value the lowercase API value for this grouping period
     * @throws NullPointerException if {@code value} is {@code null}
     */
    Group(@NonNull String value) {
        this.value = Objects.requireNonNull(value, Validation.formatNullMessage("value"));
    }

    /**
     * Returns the API query parameter value for this group (e.g.
     * {@code "week"}, {@code "month"}).
     *
     * @return the lowercase string value used in query strings
     */
    @NonNull
    public String value() {
        return value;
    }
}