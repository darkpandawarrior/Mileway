package com.miletracker.feature.cards.data

/**
 * Q.2 — current app/system language tag (e.g. "en", "ar", "hi"). Replaces the Dice source's
 * `AppCompatDelegate.getApplicationLocales()` / `LocaleManager` with an expect/actual so the mock
 * provider factory stays in commonMain. Android: `Locale`; iOS: `NSLocale`.
 */
expect fun currentLocaleTag(): String
