# Cryptocurrency Price Tracker

An offline-first Android app that lists the top cryptocurrencies by market cap with live
prices from the public [CoinGecko](https://www.coingecko.com/en/api) API, plus a detail
screen for per-coin stats. Jetpack Compose UI, Room cache, Hilt DI, Retrofit networking.

## Features

- Top 20 coins by market cap: icon, name, ticker, price, 24h change (green/red).
- Detail screen: market cap rank, market cap, 24h volume, 24h high/low.
- Search by name or ticker, case-insensitive.
- Sort by market cap (API order), price, or 24h change.
- Pull-to-refresh on both screens.
- Offline-first: the last successful response is cached in Room and shown immediately on
  next launch, with no network required.
- Error handling that distinguishes "nothing to show" (full-screen error + Retry) from
  "refresh failed but stale data is on screen" (snackbar).

## Setup

Requirements:

- Android Studio (any version shipping AGP 9.2.x support) or the Gradle CLI.
- JDK 21 for the Gradle daemon. `gradle/gradle-daemon-jvm.properties` pins
  `toolchainVersion=21` and lists foojay download URLs, so Gradle will auto-provision a
  JDK 21 if one is not already installed.
- Android SDK with API 37 installed (`compileSdk = 37`).
- An emulator or device on Android 7.0 (API 24) or newer.

Steps:

```bash
git clone <this-repo>
cd Cryptocurrencypricetracker
# local.properties needs sdk.dir pointing at your Android SDK.
# Android Studio writes this for you on first open.
./gradlew installDebug          # build + install on a connected device/emulator
./gradlew testDebugUnitTest     # JVM unit tests
./gradlew assembleDebug         # APK only
```

No API key, `.env`, or secrets are required. CoinGecko's free public endpoint is called
anonymously; `BASE_URL` is a constant in `di/NetworkModule.kt`.

If `./gradlew` fails with `JAVA_HOME is not set`, point it at a JDK before invoking:

```bash
JAVA_HOME=/path/to/jdk ./gradlew testDebugUnitTest
```

## Architecture

Single-module app, layered UI → data → remote/local, with Room as the single source of
truth.

```
ui/            Compose screens + ViewModels (coins, detail, navigation, theme, format)
domain/        Coin — the model the UI renders
data/          CoinRepository, CoinMappers (DTO → entity → domain)
data/remote/   CoinGeckoApi (Retrofit), CoinDto
data/local/    CryptoDatabase, CoinDao, CoinEntity
di/            NetworkModule, DatabaseModule (Hilt, SingletonComponent)
```

**Data flow.** Screens collect a `StateFlow<UiState>` from a `@HiltViewModel`. The
ViewModel collects `repository.observeCoins()` — a Room `Flow` — so the UI renders cache
first and never blocks on the network. `refresh()` calls `CoinGeckoApi.getMarkets()` and
hands the result to `CoinDao.replaceAll()`, a `@Transaction` that deletes then re-inserts.
Room emits, the Flow updates, Compose recomposes. The network is write-only from the UI's
point of view; nothing reads an API response directly.

**Ordering.** `coins/markets?order=market_cap_desc` returns rows in rank order, but SQLite
has no inherent row order. `CoinEntity.listPosition` records the index each coin arrived at
so `ORDER BY listPosition ASC` reproduces the API's ranking. That is why the "Market cap"
sort option is a pass-through rather than a client-side comparator — it is the DB order.

**Search and sort** are derived properties on `CoinListUiState` (`visibleCoins`), computed
from `allCoins` + `query` + `sort`. No extra state to keep in sync, no round trip to the
DB, and the raw list stays available so an empty result can be attributed to the query
(`isEmptySearchResult`) rather than to an empty cache.

**Detail screen** takes a `coinId` from a type-safe Navigation route
(`@Serializable CoinDetailRoute`) via `SavedStateHandle`, then observes that single row.
It reads from the same cache, so no second network call is needed to open a coin.

**Errors.** `Throwable.toUserMessage()` (`ui/coins/ErrorMessage.kt`) maps `IOException` to
an offline message, HTTP 429 to a rate-limit message, other `HttpException`s to a
service-error message with the code. Both ViewModels then route that message by whether
data is already on screen: `error` (blocking, with Retry) when the cache is empty,
`refreshError` (one-shot snackbar, cleared via `onRefreshErrorShown()`) when it isn't.
`CancellationException` is rethrown rather than swallowed so scope cancellation still works.

**Concurrency.** Each ViewModel guards refresh with a `loadJob?.isActive` check, so a
pull-to-refresh during an in-flight load is a no-op instead of a duplicate request.

### Stack

Kotlin 2.2.10 · Compose BOM 2026.02.01 / Material 3 · Navigation Compose 2.9.4 with
kotlinx.serialization type-safe routes · Hilt 2.60.1 (KSP) · Room 2.8.4 (KSP) ·
Retrofit 3.0.0 + Gson · OkHttp 5.1.0 + logging interceptor · Coil 3.3.0 for coin icons ·
coroutines 1.10.2.

## Testing

```bash
./gradlew testDebugUnitTest
```

JVM unit tests only, no device needed:

- `CoinListViewModelTest` — cache rendering, blocking vs snackbar error routing, snackbar
  retirement, search by name and symbol, all three sort modes, empty-search vs empty-cache,
  duplicate-refresh suppression.
- `CoinDetailViewModelTest` — route argument plumbing, not-found state, error routing,
  duplicate-refresh suppression.
- `CoinMappersTest` — symbol uppercasing, zero fallback for always-displayed fields, nulls
  preserved for optional stats, `listPosition`, full field round trip.
- `FormattersTest` — price precision above/below $1, signed percentages, T/B/M/K
  abbreviation, em-dash for unknown values.

MockK fakes the repository; `kotlinx-coroutines-test` with `StandardTestDispatcher` drives
`viewModelScope`.

## Assumptions

- **USD, hardcoded.** `vs_currency=usd` and every formatter emits `$`. No currency picker.
- **Top 20, fixed.** `per_page=20&page=1`. Enough to fill a phone screen without paging and
  well under CoinGecko's anonymous rate limit.
- **The cached list defines the app's universe.** Search filters the cached 20; the detail
  screen only opens coins in the cache. There is no per-coin lookup endpoint in use, so a
  coin that drops out of the top 20 on refresh shows "This coin is no longer in the tracked
  list."
- **Missing price and 24h change become `0.0`**, since both are always rendered. Optional
  stats stay null and render as `—`, so "unknown" is never displayed as "$0.00".
- **A refresh is whole-list.** Refreshing from the detail screen re-fetches the market list;
  it is one request either way and keeps a single write path into Room.
- **Room needs no migrations yet.** `fallbackToDestructiveMigration(dropAllTables = true)`
  is acceptable because every row is re-fetchable cache, not user data.

## Known limitations

- **No pagination or configurable page size.** The list is capped at 20 coins.
- **No price history or charts.** `coins/markets` does not return sparkline data as
  requested here, and no chart library is wired in.
- **No background or automatic refresh.** Data updates on launch, on pull-to-refresh, and
  on retry only. There is no polling, no WorkManager job, and no refresh when the app
  returns to the foreground, so prices can be stale while the app sits open.
- **No rate-limit backoff.** HTTP 429 produces a message asking the user to wait; there is
  no retry-with-backoff, request throttling, or caching layer in OkHttp.
- **`HttpLoggingInterceptor` is installed unconditionally**, including in release builds.
  It is at `BASIC` level (method, URL, status, timing — no bodies or headers), but it should
  be gated on `BuildConfig.DEBUG` before shipping.
- **Release build is unminified.** `isMinifyEnabled = false` and `proguard-rules.pro` is the
  untouched template. Enabling R8 will need keep rules verified for Gson's reflective DTO
  parsing.
- **UI strings are hardcoded in Composables**, not in `strings.xml`. Not localizable, and
  the two error-message strings live in Kotlin as well.
- **Theme is the Studio template.** Dynamic color defaults to on with the stock
  purple fallback; the gain color `0xFF12805C` is a literal rather than a theme token, so it
  is not tuned for dark mode.
- **Accessibility is partial.** The 24h change has a spoken description and icons that
  convey nothing have null descriptions, but the list rows are not merged into single
  semantic nodes, and no screen has been verified with TalkBack.
- **No instrumented or UI tests.** `ExampleInstrumentedTest` is the unmodified template, as
  is `ExampleUnitTest`. Compose UI test dependencies are declared but unused. There is also
  no DAO test against an in-memory Room database, so `replaceAll` and the `listPosition`
  ordering are covered only indirectly through the mapper tests.
- **Turbine is declared as a test dependency but never used** — tests assert on
  `uiState.value` after `advanceUntilIdle()` instead of on Flow emissions.
- **`android.disallowKotlinSourceSets=false`** in `gradle.properties` works around KSP
  2.2.10-2.0.2 not yet matching AGP 9's ownership of Kotlin source sets. Remove it once KSP
  catches up.
