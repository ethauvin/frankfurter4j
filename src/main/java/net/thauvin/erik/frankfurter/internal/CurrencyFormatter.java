/*
 * CurrencyFormatter.java
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

package net.thauvin.erik.frankfurter.internal;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Provides locale‑aware formatting of monetary amounts using ISO 4217
 * currency codes. The formatter uses a static lookup table mapping each
 * currency to a representative locale. The locale determines grouping,
 * decimal separators, and symbol placement.
 *
 * <p>This class is thread‑safe. All state is immutable and formatting
 * operations create new {@link NumberFormat} instances, so no external
 * synchronization is required.</p>
 */
public final class CurrencyFormatter {

    /**
     * Maps ISO currency codes to representative locales.
     *
     * <p>This map is immutable and safe for concurrent access.</p>
     */
    private static final Map<String, Locale> LOCALES = Map.<String, Locale>ofEntries(
            Map.entry("AED", new Locale("ar", "AE")),
            Map.entry("AFN", new Locale("fa", "AF")),
            Map.entry("ALL", new Locale("sq", "AL")),
            Map.entry("AMD", new Locale("hy", "AM")),
            Map.entry("ANG", new Locale("nl", "CW")),
            Map.entry("AOA", new Locale("pt", "AO")),
            Map.entry("ARS", new Locale("es", "AR")),
            Map.entry("AUD", new Locale("en", "AU")),
            Map.entry("AWG", new Locale("nl", "AW")),
            Map.entry("AZN", new Locale("az", "AZ")),
            Map.entry("BAM", new Locale("bs", "BA")),
            Map.entry("BBD", new Locale("en", "BB")),
            Map.entry("BDT", new Locale("bn", "BD")),
            Map.entry("BGN", new Locale("bg", "BG")),
            Map.entry("BHD", new Locale("ar", "BH")),
            Map.entry("BIF", new Locale("fr", "BI")),
            Map.entry("BMD", new Locale("en", "BM")),
            Map.entry("BND", new Locale("ms", "BN")),
            Map.entry("BOB", new Locale("es", "BO")),
            Map.entry("BRL", new Locale("pt", "BR")),
            Map.entry("BSD", new Locale("en", "BS")),
            Map.entry("BTN", new Locale("dz", "BT")),
            Map.entry("BWP", new Locale("en", "BW")),
            Map.entry("BYN", new Locale("be", "BY")),
            Map.entry("BZD", new Locale("en", "BZ")),
            Map.entry("CAD", new Locale("en", "CA")),
            Map.entry("CDF", new Locale("fr", "CD")),
            Map.entry("CHF", new Locale("de", "CH")),
            Map.entry("CLP", new Locale("es", "CL")),
            Map.entry("CNY", Locale.CHINA),
            Map.entry("COP", new Locale("es", "CO")),
            Map.entry("CRC", new Locale("es", "CR")),
            Map.entry("CUP", new Locale("es", "CU")),
            Map.entry("CVE", new Locale("pt", "CV")),
            Map.entry("CZK", new Locale("cs", "CZ")),
            Map.entry("DJF", new Locale("fr", "DJ")),
            Map.entry("DKK", new Locale("da", "DK")),
            Map.entry("DOP", new Locale("es", "DO")),
            Map.entry("DZD", new Locale("ar", "DZ")),
            Map.entry("EGP", new Locale("ar", "EG")),
            Map.entry("ERN", new Locale("ti", "ER")),
            Map.entry("ETB", new Locale("am", "ET")),
            Map.entry("EUR", Locale.GERMANY),
            Map.entry("FJD", new Locale("en", "FJ")),
            Map.entry("FKP", new Locale("en", "FK")),
            Map.entry("GBP", Locale.UK),
            Map.entry("GEL", new Locale("ka", "GE")),
            Map.entry("GGP", new Locale("en", "GG")),
            Map.entry("GHS", new Locale("en", "GH")),
            Map.entry("GIP", new Locale("en", "GI")),
            Map.entry("GMD", new Locale("en", "GM")),
            Map.entry("GNF", new Locale("fr", "GN")),
            Map.entry("GTQ", new Locale("es", "GT")),
            Map.entry("GYD", new Locale("en", "GY")),
            Map.entry("HKD", new Locale("zh", "HK")),
            Map.entry("HNL", new Locale("es", "HN")),
            Map.entry("HTG", new Locale("ht", "HT")),
            Map.entry("HUF", new Locale("hu", "HU")),
            Map.entry("IDR", new Locale("id", "ID")),
            Map.entry("ILS", new Locale("he", "IL")),
            Map.entry("IMP", new Locale("en", "IM")),
            Map.entry("INR", new Locale("en", "IN")),
            Map.entry("IQD", new Locale("ar", "IQ")),
            Map.entry("IRR", new Locale("fa", "IR")),
            Map.entry("ISK", new Locale("is", "IS")),
            Map.entry("JEP", new Locale("en", "JE")),
            Map.entry("JMD", new Locale("en", "JM")),
            Map.entry("JOD", new Locale("ar", "JO")),
            Map.entry("JPY", Locale.JAPAN),
            Map.entry("KES", new Locale("en", "KE")),
            Map.entry("KGS", new Locale("ky", "KG")),
            Map.entry("KHR", new Locale("km", "KH")),
            Map.entry("KMF", new Locale("fr", "KM")),
            Map.entry("KRW", Locale.KOREA),
            Map.entry("KWD", new Locale("ar", "KW")),
            Map.entry("KYD", new Locale("en", "KY")),
            Map.entry("KZT", new Locale("kk", "KZ")),
            Map.entry("LAK", new Locale("lo", "LA")),
            Map.entry("LBP", new Locale("ar", "LB")),
            Map.entry("LKR", new Locale("si", "LK")),
            Map.entry("LRD", new Locale("en", "LR")),
            Map.entry("LSL", new Locale("en", "LS")),
            Map.entry("LYD", new Locale("ar", "LY")),
            Map.entry("MAD", new Locale("ar", "MA")),
            Map.entry("MDL", new Locale("ro", "MD")),
            Map.entry("MGA", new Locale("mg", "MG")),
            Map.entry("MKD", new Locale("mk", "MK")),
            Map.entry("MMK", new Locale("my", "MM")),
            Map.entry("MNT", new Locale("mn", "MN")),
            Map.entry("MOP", new Locale("zh", "MO")),
            Map.entry("MRU", new Locale("ar", "MR")),
            Map.entry("MUR", new Locale("en", "MU")),
            Map.entry("MVR", new Locale("dv", "MV")),
            Map.entry("MWK", new Locale("en", "MW")),
            Map.entry("MXN", new Locale("es", "MX")),
            Map.entry("MYR", new Locale("ms", "MY")),
            Map.entry("MZN", new Locale("pt", "MZ")),
            Map.entry("NAD", new Locale("en", "NA")),
            Map.entry("NGN", new Locale("en", "NG")),
            Map.entry("NIO", new Locale("es", "NI")),
            Map.entry("NOK", new Locale("nb", "NO")),
            Map.entry("NPR", new Locale("ne", "NP")),
            Map.entry("NZD", new Locale("en", "NZ")),
            Map.entry("OMR", new Locale("ar", "OM")),
            Map.entry("PAB", new Locale("es", "PA")),
            Map.entry("PEN", new Locale("es", "PE")),
            Map.entry("PGK", new Locale("en", "PG")),
            Map.entry("PHP", new Locale("fil", "PH")),
            Map.entry("PKR", new Locale("ur", "PK")),
            Map.entry("PLN", new Locale("pl", "PL")),
            Map.entry("PYG", new Locale("es", "PY")),
            Map.entry("QAR", new Locale("ar", "QA")),
            Map.entry("RON", new Locale("ro", "RO")),
            Map.entry("RSD", new Locale("sr", "RS")),
            Map.entry("RUB", new Locale("ru", "RU")),
            Map.entry("RWF", new Locale("rw", "RW")),
            Map.entry("SAR", new Locale("ar", "SA")),
            Map.entry("SBD", new Locale("en", "SB")),
            Map.entry("SCR", new Locale("fr", "SC")),
            Map.entry("SDG", new Locale("ar", "SD")),
            Map.entry("SEK", new Locale("sv", "SE")),
            Map.entry("SGD", new Locale("en", "SG")),
            Map.entry("SHP", new Locale("en", "SH")),
            Map.entry("SLE", new Locale("en", "SL")),
            Map.entry("SOS", new Locale("so", "SO")),
            Map.entry("SRD", new Locale("nl", "SR")),
            Map.entry("SSP", new Locale("en", "SS")),
            Map.entry("STN", new Locale("pt", "ST")),
            Map.entry("SVC", new Locale("es", "SV")),
            Map.entry("SYP", new Locale("ar", "SY")),
            Map.entry("SZL", new Locale("en", "SZ")),
            Map.entry("THB", new Locale("th", "TH")),
            Map.entry("TJS", new Locale("tg", "TJ")),
            Map.entry("TMT", new Locale("tk", "TM")),
            Map.entry("TND", new Locale("ar", "TN")),
            Map.entry("TOP", new Locale("to", "TO")),
            Map.entry("TRY", new Locale("tr", "TR")),
            Map.entry("TTD", new Locale("en", "TT")),
            Map.entry("TWD", new Locale("zh", "TW")),
            Map.entry("TZS", new Locale("sw", "TZ")),
            Map.entry("UAH", new Locale("uk", "UA")),
            Map.entry("UGX", new Locale("en", "UG")),
            Map.entry("USD", Locale.US),
            Map.entry("UYU", new Locale("es", "UY")),
            Map.entry("UZS", new Locale("uz", "UZ")),
            Map.entry("VES", new Locale("es", "VE")),
            Map.entry("VND", new Locale("vi", "VN")),
            Map.entry("VUV", new Locale("bi", "VU")),
            Map.entry("WST", new Locale("sm", "WS")),
            Map.entry("XAF", new Locale("fr", "CM")),
            Map.entry("XAG", Locale.US),
            Map.entry("XAU", Locale.US),
            Map.entry("XCD", new Locale("en", "AG")),
            Map.entry("XCG", new Locale("en", "CW")),
            Map.entry("XOF", new Locale("fr", "SN")),
            Map.entry("XPD", Locale.US),
            Map.entry("XPF", new Locale("fr", "PF")),
            Map.entry("XPT", Locale.US),
            Map.entry("YER", new Locale("ar", "YE")),
            Map.entry("ZAR", new Locale("en", "ZA")),
            Map.entry("ZMW", new Locale("en", "ZM")),
            Map.entry("ZWG", new Locale("en", "ZW"))
    );

    /**
     * Disables the default constructor.
     */
    private CurrencyFormatter() {
        // no-op
    }

    /**
     * Formats a monetary amount using the locale conventions associated
     * with the given ISO currency code.
     *
     * @param amount  the numeric amount
     * @param isoCode the ISO 4217 currency code (must not be {@code null} or blank)
     * @return the formatted currency string
     * @throws IllegalArgumentException if {@code isoCode} is {@code null}, blank, or unknown
     */
    @NonNull
    public static String format(double amount, @NonNull String isoCode) {
        return format(amount, isoCode, false);
    }

    /**
     * Formats a monetary amount using the locale conventions associated
     * with the given ISO currency code.
     *
     * @param amount  the numeric amount
     * @param isoCode the ISO 4217 currency code (must not be {@code null} or blank)
     * @param rounded whether to round to the locale's default fraction digits
     * @return the formatted currency string
     * @throws IllegalArgumentException if {@code isoCode} is {@code null}, blank, or unknown
     */
    @NonNull
    public static String format(double amount, @NonNull String isoCode, boolean rounded) {
        var code = Validation.requireIsoCurrency("ISO currency code", isoCode).toUpperCase(Locale.ROOT);
        var locale = LOCALES.get(code);

        if (locale == null) {
            throw new IllegalArgumentException("Unknown ISO currency code: " + code);
        }

        return format(amount, locale, rounded);
    }

    /**
     * Formats a monetary amount using the given locale's currency and conventions.
     *
     * <p>This is the core formatter used by all ISO‑based overloads. It applies the
     * locale's default currency, symbol placement, grouping rules, and decimal
     * separators.</p>
     *
     * <p><strong>Warning:</strong> The currency is determined by {@code locale}, not by any
     * ISO code. For example, {@code format(100, Locale.JAPAN, false)} produces
     * {@code "￥100"}, not a USD amount formatted with Japanese conventions. To format a
     * specific currency using a different locale's style, create a {@link NumberFormat}
     * and call {@link NumberFormat#setCurrency setCurrency} yourself.</p>
     *
     * <p>When {@code rounded} is {@code true}, the formatter rounds to the currency's
     * default fraction digits using {@link java.math.RoundingMode#HALF_UP HALF_UP}.
     * When {@code false}, rounding is still applied, but the maximum fraction digits are
     * increased to {@code 15} to preserve precision while avoiding the excessive decimal
     * places you would get from a raw {@code double}. This is especially useful for
     * currencies like {@code JPY} that normally have {@code 0} fraction digits.</p>
     *
     * <p>This method is thread‑safe. A new {@link NumberFormat} instance is created on
     * each call, so no external synchronization is required.</p>
     *
     * @param amount  the numeric amount to format
     * @param locale  the locale whose currency and formatting rules to apply; must not be {@code null}
     * @param rounded {@code true} to round to the currency's default fraction digits,
     *                {@code false} to allow up to 15 fraction digits for extra precision
     * @return the formatted currency string, never {@code null}
     * @throws NullPointerException if {@code locale} is {@code null}
     */
    @NonNull
    public static String format(double amount, @NonNull Locale locale, boolean rounded) {
        Objects.requireNonNull(locale, Validation.formatNullMessage("locale"));
        var formatter = NumberFormat.getCurrencyInstance(locale);

        if (rounded) {
            formatter.setRoundingMode(RoundingMode.HALF_UP);
            // uses currency.getDefaultFractionDigits() by default
        } else {
            formatter.setMaximumFractionDigits(15); // cap precision, avoids 0.30000000000000004 issues
        }

        return formatter.format(amount);
    }
}