# JReader
Android-only Kotlin Multiplatform + Compose public E-Hentai reader MVP.

## Build
`./gradlew :composeApp:assembleDebug` (CI builds; local builds intentionally not run).

Content comes live from public E-Hentai HTML. Users must follow site terms and local law. No content ships with app.

## MangaDex Manhwa
Manhwa metadata and chapter images come from official free [MangaDex API](https://api.mangadex.org/docs/). JReader visibly credits MangaDex and each chapter's scanlation group. MangaDex-backed content must not be placed behind paid access or used with ads. API clients must follow MangaDex acceptable-use rules and rate limits. No MangaDex content ships with app.
