package com.miletracker.feature.profile.ui.previews

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "Phone · Light", showBackground = true)
annotation class PhonePreview

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class LightDarkPreview
