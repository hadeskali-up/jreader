# JReader

Android-only Kotlin Multiplatform + Compose manga and gallery reader.

## Features

- Unified MangaDex and E-Hentai source adapters
- Browse, search, category and genre filters
- Library, custom categories, history, and continue reading
- Reading progress and page resume
- Vertical webtoon, paged LTR, and paged RTL readers
- Zoom, pan, fit modes, page retry, and offline page loading
- Real MangaDex chapter downloads to app-private storage
- Persistent foreground download queue with retry and safe delete
- Versioned private JSON backup and restore
- Existing favorites and history migration
- Loading, empty, error, and retry states

## Deliberate limits

- Downloads run only while JReader remains alive; WorkManager background downloading is not claimed.
- E-Hentai offline downloads are not supported.
- Local folder/SAF import and third-party extension APK execution are not included.
- Backup currently stays in app-private storage.

## Build

`./gradlew :composeApp:assembleDebug` (GitHub Actions builds releases; Android builds are not run on the project VPS).

Content comes live from public E-Hentai HTML and the official MangaDex API. No content ships with the app. Users must follow source terms and local law.

## MangaDex

JReader visibly credits MangaDex and each chapter's scanlation group. MangaDex-backed content must not be placed behind paid access or used with ads. API clients must follow [MangaDex acceptable-use rules](https://api.mangadex.org/docs/) and rate limits.

## Attribution

Architecture and reader concepts are informed by Apache-2.0 licensed Komikku and Mihon. See `NOTICE`. JReader does not bundle or execute third-party extension APKs.
