# Frankfurter for Java: Retrieve Reference Exchange Rates

[![License (3-Clause BSD)](https://img.shields.io/badge/license-BSD%203--Clause-blue.svg?style=flat-square)](http://opensource.org/licenses/BSD-3-Clause)
[![Java](https://img.shields.io/badge/java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![bld](https://img.shields.io/badge/2.3.0-FA9052?label=bld&labelColor=2392FF)](https://rife2.com/bld)
[![Release](https://img.shields.io/github/release/ethauvin/frankfurter4j.svg)](https://github.com/ethauvin/frankfurter4j/releases/latest)
[![Maven Central](https://img.shields.io/maven-central/v/net.thauvin.erik./frankfurter.svg?color=blue)](https://central.sonatype.com/artifact/net.thauvin.erik/frankfurter4j)
[![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fnet%2Fthauvin%2Ferik%2Ffrankfurter4j%2Fmaven-metadata.xml&label=snapshot)](https://github.com/ethauvin/frankfurter4j/packages/2561141/versions)

[![Known Vulnerabilities](https://snyk.io/test/github/ethauvin/frankfurter4j/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/ethauvin/frankfurter4j?targetFile=pom.xml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ethauvin_frankfurter4j&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ethauvin_frankfurter4j)
[![GitHub CI](https://github.com/ethauvin/frankfurter4j/actions/workflows/bld.yml/badge.svg)](https://github.com/ethauvin/frankfurter4j/actions/workflows/bld.yml)
[![CircleCI](https://circleci.com/gh/ethauvin/frankfurter4j/tree/main.svg?style=shield)](https://circleci.com/gh/ethauvin/frankfurter4j/tree/main)

Retrieve reference exchange rates from [Frankfurter.dev](https://frankfurter.dev/), a free, open-source currency data
API.

## Examples (TL;DR)

```java
var latestRates = new LatestRates.Builder()
        .amount(100.0)
        .base("USD")
        .symbols("EUR", "GBP")
        .build();
var exchangeRates = latestRates.getExchangeRates();
var euro = exchangeRates.getRateFor("EUR");
var britishPound = exchangeRates.getRateFor("GBP");
```

To get the latest exchange rates for the United States Dollar in Euro and British Pound.

## bld

To use with [bld](https://rife2.com/bld), include the following dependency in
your [build](https://github.com/ethauvin/frankfurter4j/blob/master/examples/src/bld/java/com/example/Frankfurter4jExample.java)
file:

```java
repositories = List.of(MAVEN_CENTRAL, CENTRAL_SNAPSHOTS);

scope(compile)
    .include(dependency("net.thauvin.erik:frankfurter4j:0.9.0"));
```

## Gradle, Maven, etc.

To use with [Gradle](https://gradle.org/), include the following dependency in your build file:

```gradle
repositories {
    maven {
        name = 'Central Portal Snapshots'
        url = 'https://central.sonatype.com/repository/maven-snapshots/'
    }
    mavenCentral()
}

dependencies {
    implementation("net.thauvin.erik:frankfurter4j:0.9.0")
}
```

Instructions for using with Maven, Ivy, etc. can be found
on [Maven Central](https://search.maven.org/search?q=g:%22net.thauvin.erik%22%20AND%20a:%22frankfurter4j%22).

## Latest Rates

To fetch the latest working day's rates:

```java
var latestRates = new LatestRates.Builder().build();
var exchangeRates = latestRates.getExchangeRates();
```

The latest exchange rates will be stored in the `ExchangeRates` class:

```java
if (exchangeRates.hasRateFor("JPY")) {
    var jpy = exchangeRates.getRateFor("JPY");
}
```

To change the base currency use the builder's `base` method. The default is `EUR`.

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

**Note**: As mentioned on the website, Frankfurter stores dates in UTC. If you use a different time zone, be aware that
you may be querying with a different calendar date than intended. Also, data returned for today is not stable and will
update if new rates are published.

## Time Series Data

To fetch rates over a period.

```java
var timeSeries = new TimeSeries.Builder()
        .startDate(LocalDate.of(2000, 1, 1))
        .endDate(LocalDate.of(2000, 12, 31))
        .build();
var periodicRates = timeSeries.getPeriodicRates();

```

The periodic rates will be stored in the `TimeSeriesData` class.

```java
var firstMarketDay = LocalDate.of(2000, 1, 4);
if (periodicRates.hasRatesFor(firstMarketDay)) {
    // Get the Yen rate directly
    var yen = periodicRates.getRateFor(firstMarketDay, "JPY");

    // Get the Dollar rate if available
    var rates = periodicRates.getRatesFor(firstMarketDay);
    if (rates.containsKey("USD")) {
        var usd = rates.get("USD");
    }
}

// Loop through all dates
periodicRates.getDates().forEach(date -> {
    // Print the date
    System.out.println("Rates for " + date);
    // Loop through all rates
    periodicRates.getRatesFor(date).forEach((symbol, rate) -> {
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

To get the supported currency symbols and their full names.

```java
var currencies = AvailableCurrencies.getCurrencies();
```

The currencies are stored in a `Currencies` class that extends `Hashmap`.

```java
currencies.get("USD"); // returns "United States Dollar"
currencies.getFullNameFor("usd"); // case-insensitive

currencies.getFullNameFor("EUR"); // returns "Euro"

currencies.getSymbolFor("euro"); // returns "EUR"
currencies.getSymbolFor(Pattern.compile(".*Japan.*")); // returns "JPY"
```

## Currency Conversion

You can perform currency conversion by fetching the exchange rate with a specified amount.

```java
var latestRates = new LatestRates.Builder()
        .amount(10)
        .base("USD")
        .symbols("EUR")
        .build();
var exchangeRates = latestRates.getExchangeRates();
var euro = exchangeRates.getRateFor("EUR");

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
FrankfurterUtils.formatCurrency("USD", 100.0); // $100.00
FrankfurterUtils.fomartCurrency("EUR", 1234.567); // 1.234,567 €
FrankfurterUtils.fomartCurrency("EUR", 1234.567, true); // 1.234,57 € rounded
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

The project has an [IntelliJ IDEA](https://www.jetbrains.com/idea/) project structure. You can just open it after all
the dependencies were downloaded and peruse the code.
