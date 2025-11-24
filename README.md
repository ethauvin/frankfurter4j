# Frankfurter for Java: Retrieve Reference Exchange Rates

[![License (3-Clause BSD)](https://img.shields.io/badge/license-BSD%203--Clause-blue.svg?style=flat-square)](http://opensource.org/licenses/BSD-3-Clause)
[![Java](https://img.shields.io/badge/java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![bld](https://img.shields.io/badge/2.3.0-FA9052?label=bld&labelColor=2392FF)](https://rife2.com/bld)
[![release](https://img.shields.io/github/release/ethauvin/frankfurter4j.svg?color=blue)](https://github.com/ethauvin/frankfurter4j/releases/latest)
[![Maven Central](https://img.shields.io/maven-central/v/net.thauvin.erik/frankfurter4j.svg?color=blue)](https://central.sonatype.com/artifact/net.thauvin.erik/frankfurter4j)
[![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fnet%2Fthauvin%2Ferik%2Ffrankfurter4j%2Fmaven-metadata.xml&label=snapshot)](https://github.com/ethauvin/frankfurter4j/packages/2561141/versions)

[![Known Vulnerabilities](https://snyk.io/test/github/ethauvin/frankfurter4j/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/ethauvin/frankfurter4j?targetFile=pom.xml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ethauvin_frankfurter4j&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ethauvin_frankfurter4j)
[![GitHub CI](https://github.com/ethauvin/frankfurter4j/actions/workflows/bld.yml/badge.svg)](https://github.com/ethauvin/frankfurter4j/actions/workflows/bld.yml)
[![CircleCI](https://circleci.com/gh/ethauvin/frankfurter4j/tree/main.svg?style=shield)](https://circleci.com/gh/ethauvin/frankfurter4j/tree/main)

Retrieve reference exchange rates from
[Frankfurter.dev](https://frankfurter.dev/), a free, open-source currency data
API.

## Examples (TL;DR)

```java
var latestRates = new LatestRates.Builder()
        .amount(100.0)
        .base("USD")
        .symbols("EUR", "GBP")
        .build();
var exchangeRates = latestRates.exchangeRates();
var euro = exchangeRates.rateFor("EUR");
var britishPound = exchangeRates.rateFor("GBP");
```

To get the latest exchange rates for the United States Dollar in Euro
and British Pound.

## bld

To use with [bld](https://rife2.com/bld), include the following dependency
in your build file:

```java
repositories = List.of(MAVEN_CENTRAL, CENTRAL_SNAPSHOTS);

scope(compile)
    .include(dependency("net.thauvin.erik:frankfurter4j:0.9.1"));
```

## Gradle, Maven, etc

To use with [Gradle](https://gradle.org/), include the following dependency
in your build file:

```gradle
repositories {
    maven {
        name = 'Central Portal Snapshots'
        url = 'https://central.sonatype.com/repository/maven-snapshots/'
    }
    mavenCentral()
}

dependencies {
    implementation("net.thauvin.erik:frankfurter4j:0.9.1")
}
```

Instructions for using with Maven, Ivy, etc. can be found
on [Maven Central](https://central.sonatype.com/artifact/net.thauvin.erik/frankfurter4j).

## Latest Rates

To fetch the latest working day's rates:

```java
var latestRates = new LatestRates.Builder().build();
var exchangeRates = latestRates.exchangeRates();
```

The latest exchange rates will be stored in the
[ExchangeRates](https://ethauvin.github.io/frankfurter4j/net/thauvin/erik/frankfurter/models/ExchangeRates.html)
class:

```java
if (exchangeRates.hasRateFor("JPY")) {
    var jpy = exchangeRates.rateFor("JPY");
}
```

To change the base currency use the builder's
[base](https://ethauvin.github.io/frankfurter4j/net/thauvin/erik/frankfurter/LatestRates.Builder.html#base(java.lang.String))
method. The default is `EUR`.

```java
var latestRates = new LatestRates.Builder()
        .base("USD")
        .build();
```

To limit the response to specific target currencies.

```java
var latestRates = new LatestRates.Builder()
        .symbols("CHF", "GBP")
        .build();
```

## Historical Rates

To retrieve rates for a specific date.

```java
var latestRates = new LatestRates.Builder()
        .date(LocalDate.of(1999, 1, 4))
        .build();
```

To change the base currency and filter target currencies.

```java
var latestRates = new LatestRates.Builder()
        .base("USD")
        .date(LocalDate.of(1999, 1, 4))
        .symbols("EUR")
        .build();
```

**Note**: As mentioned on the website, Frankfurter stores dates in UTC.
If you use a different time zone, be aware that you may be querying
with a different calendar date than intended. Also, data returned
for today is not stable and will update if new rates are published.

## Time Series Data

To fetch rates over a period.

```java
var timeSeries = new TimeSeries.Builder()
        .startDate(LocalDate.of(2000, 1, 1))
        .endDate(LocalDate.of(2000, 12, 31))
        .build();
var periodicRates = timeSeries.periodicRates();

```

The periodic rates will be stored in the
[TimeSeries](https://ethauvin.github.io/frankfurter4j/net/thauvin/erik/frankfurter/TimeSeries.html)
class.

```java
var firstMarketDay = LocalDate.of(2000, 1, 4);
if (periodicRates.hasRatesFor(firstMarketDay)) {
    // Get the Yen rate directly
    var yen = periodicRates.rateFor(firstMarketDay, "JPY");

    // Get the Dollar rate if available
    var rates = periodicRates.rateFor(firstMarketDay);
    if (rates.containsKey("USD")) {
        var usd = rates.get("USD");
    }
}

// Loop through all dates
periodicRates.dates().forEach(date -> {
    // Print the date
    System.out.println("Rates for " + date);
    // Loop through all rates
    periodicRates.ratesFor(date).forEach((symbol, rate) -> {
        // Print the symbol and rate, e.g., USD: 0.9059
        System.out.println("    " + symbol + ": " + rate); 
    });
});
```

To fetch rates up to the present.

```java
var timeSeries = new TimeSeries.Builder()
        .startDate(LocalDate.of(2025, 1, 1))
        .build();
```

To filter currencies to reduce response size and improve performance.

```java
var timeSeries = new TimeSeries.Builder()
        .startDate(LocalDate.of(2025, 1, 1))
        .endDate(LocalDate.now())
        .symbols("USD")
        .build();
```

## Available currencies

The currencies are stored in a
[CurrencyRegistry](https://ethauvin.github.io/frankfurter4j/net/thauvin/erik/frankfurter/CurrencyRegistry.html)
which contains [Currency](https://ethauvin.github.io/frankfurter4j/net/thauvin/erik/frankfurter/models/Currency.html)
records.

```java
var currencies = CurrencyRegistry.getInstance();
var usd = currencies.findBySymbol("USD"); // case-insensitive
if (usd.isPresent()) {
    var name = usd.get().name(); // United States Dollar
    var symbol = usd.get().symbol(); // USD
    var locale = usd.get().locale(); // Locale.US
}

currencies.findBySymbol("usd"); // case-insensitive
currencies.findBySymbol("EUR"); // the record for Euro

currencies.findByName("euro"); // the record for EUR
currencies.findByName(".*Japan.*"); // the record for JPY

currencies.search(".*Dollar$"); //  list of matching currencies

currencies.contains("JPY"); // true
```

The currently supported currencies are:

| Symbol  |  Name                 |
|:--------|:----------------------|
| `AUD`   | Australian Dollar     |
| `BGN`   | Bulgarian Lev         |
| `BRL`   | Brazilian Real        |
| `CAD`   | Canadian Dollar       |
| `CHF`   | Swiss Franc           |
| `CNY`   | Chinese Renminbi Yuan |
| `CZK`   | Czech Koruna          |
| `DKK`   | Danish Krone          |
| `EUR`   | Euro                  |
| `GBP`   | British Pound         |
| `HKD`   | Hong Kong Dollar      |
| `HUF`   | Hungarian Forint      |
| `IDR`   | Indonesian Rupiah     |
| `ILS`   | Israeli New Sheqel    |
| `INR`   | Indian Rupee          |
| `ISK`   | Icelandic Króna       |
| `JPY`   | Japanese Yen          |
| `KRW`   | South Korean Won      |
| `MXN`   | Mexican Peso          |
| `MYR`   | Malaysian Ringgit     |
| `NOK`   | Norwegian Krone       |
| `NZD`   | New Zealand Dollar    |
| `PHP`   | Philippine Peso       |
| `PLN`   | Polish Złoty          |
| `RON`   | Romanian Leu          |
| `SEK`   | Swedish Krona         |
| `SGD`   | Singapore Dollar      |
| `THB`   | Thai Baht             |
| `TRY`   | Turkish Lira          |
| `USD`   | United States Dollar  |
| `ZAR`   | South African Rand    |

This list is maintained internally as it is unlikely to change.
Although, to fully implement the API, the list could be refreshed using:

```java
currencies.refresh();
```

## Currency Conversion

You can perform currency conversion by fetching the exchange rate with a
specified amount.

```java
var latestRates = new LatestRates.Builder()
        .amount(10)
        .base("USD")
        .symbols("EUR")
        .build();
var exchangeRates = latestRates.exchangeRates();
var euro = exchangeRates.rateFor("EUR");

System.out.println("$10 = €" + euro); // $10 = €8.8059

```

## Working Days

You can retrieve a list of [working days](https://www.ecb.europa.eu/stats/policy_and_exchange_rates/euro_reference_exchange_rates/html/index.en.html)
(non-weekends, non-closing days) between two dates.

```java
var workingDays = FrankfurterUtils.workingDays(
        LocalDate.of(2021, 1, 1), LocalDate.of(2021, 1, 31));

var firstWorkingDay = workingDays.get(0); // 2021-01-04
var lastWorkingDay = workingDays.get(workingDays.size() - 1); // 2025-01-29
```

## Currency Format

You can also format amounts for specific currencies.

```java
CurrencyFormatter.formatCurrency("USD", 100.0); // $100.00
CurrencyFormatter.fomartCurrency("EUR", 1234.567); // 1.234,567 €
CurrencyFormatter.fomartCurrency("EUR", 1234.567, true); // 1.234,57 € rounded
```

## Contributing

If you want to contribute to this project, all you have to do is clone the GitHub
repository:

```console
git clone git@github.com:ethauvin/frankfurter4j.git
```

Then use [bld](https://rife2.com/bld) to build:

```console
cd frankfurter4j
./bld compile
```

The project has an [IntelliJ IDEA](https://www.jetbrains.com/idea/) project structure.
You can just open it after all the dependencies were downloaded and peruse
the code.

## More…

If all else fails, there's always more
[Documentation](https://ethauvin.github.io/frankfurter4j/).
