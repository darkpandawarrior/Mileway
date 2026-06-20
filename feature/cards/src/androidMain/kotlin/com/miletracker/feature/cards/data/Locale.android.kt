package com.miletracker.feature.cards.data

import java.util.Locale

actual fun currentLocaleTag(): String = Locale.getDefault().language
