/*
 * CurrencyCode.java
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
import net.thauvin.erik.frankfurter.internal.CurrencyFormatter;

import java.util.*;
import java.util.Currency;
import java.util.stream.Collectors;

/**
 * ISO 4217 currency codes with representative locales.
 *
 * <p>Each constant provides the ISO code and a locale used for
 * formatting. Use {@link #format(double)} or {@link #format(double, boolean)}
 * to format amounts directly.</p>
 *
 * <p>This enum is immutable and thread-safe.</p>
 */
public enum CurrencyCode {
    AED("AED", new Locale("ar", "AE")),
    AFN("AFN", new Locale("fa", "AF")),
    ALL("ALL", new Locale("sq", "AL")),
    AMD("AMD", new Locale("hy", "AM")),
    ANG("ANG", new Locale("nl", "CW")),
    AOA("AOA", new Locale("pt", "AO")),
    ARS("ARS", new Locale("es", "AR")),
    AUD("AUD", new Locale("en", "AU")),
    AWG("AWG", new Locale("nl", "AW")),
    AZN("AZN", new Locale("az", "AZ")),
    BAM("BAM", new Locale("bs", "BA")),
    BBD("BBD", new Locale("en", "BB")),
    BDT("BDT", new Locale("bn", "BD")),
    BGN("BGN", new Locale("bg", "BG")),
    BHD("BHD", new Locale("ar", "BH")),
    BIF("BIF", new Locale("fr", "BI")),
    BMD("BMD", new Locale("en", "BM")),
    BND("BND", new Locale("ms", "BN")),
    BOB("BOB", new Locale("es", "BO")),
    BRL("BRL", new Locale("pt", "BR")),
    BSD("BSD", new Locale("en", "BS")),
    BTN("BTN", new Locale("dz", "BT")),
    BWP("BWP", new Locale("en", "BW")),
    BYN("BYN", new Locale("be", "BY")),
    BZD("BZD", new Locale("en", "BZ")),
    CAD("CAD", new Locale("en", "CA")),
    CDF("CDF", new Locale("fr", "CD")),
    CHF("CHF", new Locale("de", "CH")),
    CLP("CLP", new Locale("es", "CL")),
    CNY("CNY", Locale.CHINA),
    COP("COP", new Locale("es", "CO")),
    CRC("CRC", new Locale("es", "CR")),
    CUP("CUP", new Locale("es", "CU")),
    CVE("CVE", new Locale("pt", "CV")),
    CZK("CZK", new Locale("cs", "CZ")),
    DJF("DJF", new Locale("fr", "DJ")),
    DKK("DKK", new Locale("da", "DK")),
    DOP("DOP", new Locale("es", "DO")),
    DZD("DZD", new Locale("ar", "DZ")),
    EGP("EGP", new Locale("ar", "EG")),
    ERN("ERN", new Locale("ti", "ER")),
    ETB("ETB", new Locale("am", "ET")),
    EUR("EUR", Locale.GERMANY),
    FJD("FJD", new Locale("en", "FJ")),
    FKP("FKP", new Locale("en", "FK")),
    GBP("GBP", Locale.UK),
    GEL("GEL", new Locale("ka", "GE")),
    GGP("GGP", new Locale("en", "GG")),
    GHS("GHS", new Locale("en", "GH")),
    GIP("GIP", new Locale("en", "GI")),
    GMD("GMD", new Locale("en", "GM")),
    GNF("GNF", new Locale("fr", "GN")),
    GTQ("GTQ", new Locale("es", "GT")),
    GYD("GYD", new Locale("en", "GY")),
    HKD("HKD", new Locale("zh", "HK")),
    HNL("HNL", new Locale("es", "HN")),
    HTG("HTG", new Locale("ht", "HT")),
    HUF("HUF", new Locale("hu", "HU")),
    IDR("IDR", new Locale("id", "ID")),
    ILS("ILS", new Locale("he", "IL")),
    IMP("IMP", new Locale("en", "IM")),
    INR("INR", new Locale("en", "IN")),
    IQD("IQD", new Locale("ar", "IQ")),
    IRR("IRR", new Locale("fa", "IR")),
    ISK("ISK", new Locale("is", "IS")),
    JEP("JEP", new Locale("en", "JE")),
    JMD("JMD", new Locale("en", "JM")),
    JOD("JOD", new Locale("ar", "JO")),
    JPY("JPY", Locale.JAPAN),
    KES("KES", new Locale("en", "KE")),
    KGS("KGS", new Locale("ky", "KG")),
    KHR("KHR", new Locale("km", "KH")),
    KMF("KMF", new Locale("fr", "KM")),
    KRW("KRW", Locale.KOREA),
    KWD("KWD", new Locale("ar", "KW")),
    KYD("KYD", new Locale("en", "KY")),
    KZT("KZT", new Locale("kk", "KZ")),
    LAK("LAK", new Locale("lo", "LA")),
    LBP("LBP", new Locale("ar", "LB")),
    LKR("LKR", new Locale("si", "LK")),
    LRD("LRD", new Locale("en", "LR")),
    LSL("LSL", new Locale("en", "LS")),
    LYD("LYD", new Locale("ar", "LY")),
    MAD("MAD", new Locale("ar", "MA")),
    MDL("MDL", new Locale("ro", "MD")),
    MGA("MGA", new Locale("mg", "MG")),
    MKD("MKD", new Locale("mk", "MK")),
    MMK("MMK", new Locale("my", "MM")),
    MNT("MNT", new Locale("mn", "MN")),
    MOP("MOP", new Locale("zh", "MO")),
    MRU("MRU", new Locale("ar", "MR")),
    MUR("MUR", new Locale("en", "MU")),
    MVR("MVR", new Locale("dv", "MV")),
    MWK("MWK", new Locale("en", "MW")),
    MXN("MXN", new Locale("es", "MX")),
    MYR("MYR", new Locale("ms", "MY")),
    MZN("MZN", new Locale("pt", "MZ")),
    NAD("NAD", new Locale("en", "NA")),
    NGN("NGN", new Locale("en", "NG")),
    NIO("NIO", new Locale("es", "NI")),
    NOK("NOK", new Locale("nb", "NO")),
    NPR("NPR", new Locale("ne", "NP")),
    NZD("NZD", new Locale("en", "NZ")),
    OMR("OMR", new Locale("ar", "OM")),
    PAB("PAB", new Locale("es", "PA")),
    PEN("PEN", new Locale("es", "PE")),
    PGK("PGK", new Locale("en", "PG")),
    PHP("PHP", new Locale("fil", "PH")),
    PKR("PKR", new Locale("ur", "PK")),
    PLN("PLN", new Locale("pl", "PL")),
    PYG("PYG", new Locale("es", "PY")),
    QAR("QAR", new Locale("ar", "QA")),
    RON("RON", new Locale("ro", "RO")),
    RSD("RSD", new Locale("sr", "RS")),
    RUB("RUB", new Locale("ru", "RU")),
    RWF("RWF", new Locale("rw", "RW")),
    SAR("SAR", new Locale("ar", "SA")),
    SBD("SBD", new Locale("en", "SB")),
    SCR("SCR", new Locale("fr", "SC")),
    SDG("SDG", new Locale("ar", "SD")),
    SEK("SEK", new Locale("sv", "SE")),
    SGD("SGD", new Locale("en", "SG")),
    SHP("SHP", new Locale("en", "SH")),
    SLE("SLE", new Locale("en", "SL")),
    SOS("SOS", new Locale("so", "SO")),
    SRD("SRD", new Locale("nl", "SR")),
    SSP("SSP", new Locale("en", "SS")),
    STN("STN", new Locale("pt", "ST")),
    SVC("SVC", new Locale("es", "SV")),
    SYP("SYP", new Locale("ar", "SY")),
    SZL("SZL", new Locale("en", "SZ")),
    THB("THB", new Locale("th", "TH")),
    TJS("TJS", new Locale("tg", "TJ")),
    TMT("TMT", new Locale("tk", "TM")),
    TND("TND", new Locale("ar", "TN")),
    TOP("TOP", new Locale("to", "TO")),
    TRY("TRY", new Locale("tr", "TR")),
    TTD("TTD", new Locale("en", "TT")),
    TWD("TWD", new Locale("zh", "TW")),
    TZS("TZS", new Locale("sw", "TZ")),
    UAH("UAH", new Locale("uk", "UA")),
    UGX("UGX", new Locale("en", "UG")),
    USD("USD", Locale.US),
    UYU("UYU", new Locale("es", "UY")),
    UZS("UZS", new Locale("uz", "UZ")),
    VES("VES", new Locale("es", "VE")),
    VND("VND", new Locale("vi", "VN")),
    VUV("VUV", new Locale("bi", "VU")),
    WST("WST", new Locale("sm", "WS")),
    XAF("XAF", new Locale("fr", "CM")),
    XAG("XAG", Locale.US),
    XAU("XAU", Locale.US),
    XCD("XCD", new Locale("en", "AG")),
    XCG("XCG", new Locale("en", "CW")),
    XOF("XOF", new Locale("fr", "SN")),
    XPD("XPD", Locale.US),
    XPF("XPF", new Locale("fr", "PF")),
    XPT("XPT", Locale.US),
    YER("YER", new Locale("ar", "YE")),
    ZAR("ZAR", new Locale("en", "ZA")),
    ZMW("ZMW", new Locale("en", "ZM")),
    ZWG("ZWG", new Locale("en", "ZW"));

    private static final Map<String, CurrencyCode> BY_CODE = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(CurrencyCode::getCode, c -> c));

    private final String code;
    private final Locale locale;

    CurrencyCode(String code, Locale locale) {
        this.code = code;
        this.locale = locale;
    }

    /**
     * Look up a currency by ISO code.
     *
     * @param code ISO 4217 code, case-insensitive
     * @return Optional containing the currency if found
     */
    @NonNull
    public static Optional<CurrencyCode> fromCode(@NonNull String code) {
        return Optional.ofNullable(BY_CODE.get(code.toUpperCase(Locale.ROOT)));
    }

    /**
     * Format an amount using this currency's locale conventions.
     *
     * @see CurrencyFormatter#format(double, Locale, boolean)
     */
    @NonNull
    public String format(double amount) {
        return CurrencyFormatter.format(amount, locale, false);
    }

    /**
     * Format an amount using this currency's locale conventions.
     *
     * @param rounded true to round to default fraction digits
     * @see CurrencyFormatter#format(double, Locale, boolean)
     */
    @NonNull
    public String format(double amount, boolean rounded) {
        return CurrencyFormatter.format(amount, locale, rounded);
    }

    /**
     * @return the ISO 4217 currency code
     */
    @NonNull
    public String getCode() {
        return code;
    }

    /**
     * @return the representative locale for formatting
     */
    @NonNull
    public Locale getLocale() {
        return locale;
    }

    /**
     * @return the {@link java.util.Currency} instance, if available
     */
    @NonNull
    public Currency toCurrency() {
        return Currency.getInstance(code);
    }
}
