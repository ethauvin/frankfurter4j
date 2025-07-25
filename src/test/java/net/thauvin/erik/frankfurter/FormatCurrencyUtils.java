/*
 * FormatCurrencyUtils.java
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

import java.text.NumberFormat;
import java.util.Locale;

public final class FormatCurrencyUtils {
    private FormatCurrencyUtils() {
        // no-op
    }

    public static String toDollar(double amount) {
        var numberFormat = NumberFormat.getCurrencyInstance(Locale.US);
        numberFormat.setMaximumFractionDigits(99); // prevent rounding
        return numberFormat.format(amount);
    }

    public static String toEur(double amount) {
        var numberFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        numberFormat.setMaximumFractionDigits(99); // prevent rounding
        return numberFormat.format(amount);
    }

    public static String toPound(double amount) {
        var numberFormat = NumberFormat.getCurrencyInstance(Locale.UK);
        numberFormat.setMaximumFractionDigits(99); // prevent rounding
        return numberFormat.format(amount);
    }

    public static String toYen(double amount) {
        var numberFormat = NumberFormat.getCurrencyInstance(Locale.JAPAN);
        numberFormat.setMaximumFractionDigits(99); // prevent rounding
        return numberFormat.format(amount);
    }
}
