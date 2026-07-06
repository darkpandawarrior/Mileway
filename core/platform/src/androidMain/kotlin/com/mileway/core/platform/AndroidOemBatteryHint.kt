package com.mileway.core.platform

import android.os.Build

actual fun currentDeviceManufacturer(): String? = Build.MANUFACTURER
