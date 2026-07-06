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
API (v2)

## Examples (TL;DR)

```java
var client = new Frankfurter();

var latestRates = client.getRates();
if (latestRates instanceof ExchangeRates latest) {
    var pound = latest.find(CurrencyCode.GBP); // or latest.find("GBP")
    pound.ifPresent(rate ->
        System.out.println("1 GBP: " + rate.exchangeRate() + " EUR"));
}

var rate = client.getRate(CurrencyCode.USD, CurrencyCode.EUR);
if (rate instanceof Rate dollar) {
    System.out.println("1 USD: " + dollar.exchangeRate() + " EUR");
}
```

To get the latest exchange rates for the British Pound and
United States Dollar in Euro.

## bld

To use with [bld](https://rife2.com/bld), include the following dependency
in your build file:

```java
repositories = List.of(MAVEN_CENTRAL, CENTRAL_SNAPSHOTS);

scope(compile)
    .include(dependency("net.thauvin.erik:frankfurter4j:1.0.0-SNAPSHOT"));
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
    implementation("net.thauvin.erik:frankfurter4j:1.0.0-SNAPSHOT")
}
```

Instructions for using with Maven, Ivy, etc. can be found
on [Maven Central](https://central.sonatype.com/artifact/net.thauvin.erik/frankfurter4j).

## Latest Rates

Fetch the latest exchange rates.

```java
var client = new Frankfurter();
var latestRates = client.getRates();
```

The latest exchange rates are stored in the
[ExchangeRates](https://ethauvin.github.io/frankfurter4j/net/thauvin/erik/frankfurter/models/ExchangeRates.html)
class.

Find a specific rate.

```java
if (latestRates instanceof ExchangeRates rates) {
   var gbp = rates.find(CurrencyCode.GBP).orElse(null);
}
```

The rate is stored in the [Rate](https://ethauvin.github.io/frankfurter4j/net/thauvin/erik/frankfurter/models/Rate.html)
class.

Change the base currency with base. Filter target currencies with quotes.

```java
var client = new Frankfurter();
var latestResult = client.getRates(
        new RatesConfig.Builder()
                .base(CurrencyCode.USD)
                .quotes(CurrencyCode.EUR, CurrencyCode.GBP)
                .build()
    );
```

## Historical Rates

Look up rates for a specific date.

```java
var client = new Frankfurter();
var historicalRates = client.getRates(
        new RatesConfig.Builder()
                .date(LocalDate.parse("1999-01-04"))
                .build()
        );
```

**Note**: As mentioned on the website, Frankfurter stores dates in UTC.
If you use a different time zone, be aware that you may be querying
with a different calendar date than intended. Also, data returned
for today is not stable and will update if new rates are published.

## Time Series Data

Fetch rates over a period with from and to.

```java
var client = new Frankfurter();
var timeSeries = client.getRates(
        new RatesConfig.Builder()
                .from(LocalDate.parse("2024-01-01"))
                .to(LocalDate.parse("2024-01-10"))
                .build()
        );

```

## Grouping

Downsample a time series with group.

```java
var client = new Frankfurter();
var group = client.getRates(
        new RatesConfig.Builder()
                .from(LocalDate.of(2024, 1, 1))
                .group(Group.MONTH)
                .build()
        );
```

## Filter by Provider

Scope to specific providers with providers.

```java
var client = new Frankfurter();
var filtered = client.getRates(
            new RatesConfig.Builder()
                    .providers("ECB", "BAM")
                    .build()
    );

```

## Rate

Get the rate for a single currency pair.

```java
var client = new Frankfurter();
var rate = client.getRate(CurrencyCode.USD, CurrencyCode.EUR);
```

Optionally add date or providers.

```java
var rate = client.getRate(
        new RateConfig.Builder()
        .base(CurrencyCode.USD)
        .quote(CurrencyCode.EUR)
        .date(LocalDate.of(2026, 1, 1))
        .build()
    );
```

## Currency

Get details and provider coverage for a single currency.

```java
var client = new Frankfurter();
var currency = client.getCurrency(CurrencyCode.EUR);

if (currency instanceof Currency eur) {
    var name = eur.name();
}
```

## Providers

List the data sources behind the API.

```java
var client = new Frankfurter();
var providers = client.getProviders();

if (providers instanceof Providers p) {
        var ecb = p.find(CurrencyCode.ECB);
}

```

## Currencies

Get available currencies with provider coverage.

```java
var client = new Frankfurter();
var currencies = client.getCurrencies();

if (currencies instanceof Currencies c) {
        var usd = c.find(CurrencyCode.USD);
}
```

## Currency Format

Format amounts to specific local currencies.

```java
var rate = client.getRate(CurrencyCode.USD, CurrencyCode.GBP);

if (rate instanceof Rate r) {
    var amount = 12;
    var usd = CurrencyFormatter.format(amount, CurrencyCode.USD);
    var gbp = CurrencyFormatter.format(r.exchangeRate() * amount, CurrencyCode.GBP);
    System.out.println(usd + ": " + gbp); // e.g. $12.00: £9.00468
}
```

## Error

The API returns standard HTTP status codes with an error message.

```java
var rate = client.getRate("FOO", "BAR");

if (rate instanceof ErrorResponse error) {
    // 422: invalid currency: FOO,BAR
    System.out.println(error.status() + ": " + error.message());
}
```

## Contributing

See [CONTRIBUTING.md](https://github.com/ethauvin/frankfurter4j?tab=contributing-ov-file#readme) for information about
contributing to this project.

## More…

If all else fails, there's always more
[Documentation](https://ethauvin.github.io/frankfurter4j/).
