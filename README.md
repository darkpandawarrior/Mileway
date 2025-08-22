# MileTracker

A production-grade mileage & trip tracking app built with **Compose Multiplatform** — demonstrating the location-engineering, offline-first, and multi-module architecture patterns I use in production apps serving 50k+ MAU.

> 🚧 Actively being polished — architecture diagram, screenshots, and test coverage write-up coming.

## What it does

- **High-accuracy trip tracking** — foreground service with sensor-fusion distance computation (GPS + accelerometer), spike filtering, and an advanced distance breakdown persisted per track
- **Odometer OCR** — on-device ML Kit text recognition + document scanner for odometer photo capture
- **Geofenced check-ins** — local geofence detection with manual check-in fallback
- **Route rendering** — OpenStreetMap (osmdroid) route visualization
- **Trip insights** — analyzers for trip quality, activity classification, system impact, and distance integrity
- **Exports** — CSV / GPX / KML / GeoJSON / JSON
- **Receipts & attachments** — camera capture persisted alongside trip submissions
- **Theming & settings** — palette customization, locale switching, experimental feature toggles

## Architecture

```
app                  → composition root, navigation
core/ui              → design system, shared composables
core/data            → Room entities, DAOs, repositories (commonMain)
core/network         → API layer
feature/tracking     → location engine, geofencing, check-ins
feature/logging      → manual trip logging
feature/media        → camera, OCR, attachments
feature/profile      → profile, settings, debug tools
stub                 → fake network layer for offline-first dev & tests
```

- **Compose Multiplatform 1.10** · **Kotlin 2.3** · **Room 2.8** (KMP) · **Koin** DI
- MVI presentation with single immutable `UiState` per screen
- Repository pattern with a swappable stub network layer
- kotlinx-datetime / serialization / coroutines throughout

## Why this exists

I led GPS accuracy improvements from **50% → 95%** on a production fleet-tracking platform. This repo rebuilds those patterns — predictive tracking, sensor fusion, OEM battery-manager survival — in a clean, public codebase.

## Running

```bash
./gradlew :app:installDebug
```

Requires JDK 17+ and an Android device/emulator (API 26+). The `stub` module fakes the backend, so the app runs fully offline.
