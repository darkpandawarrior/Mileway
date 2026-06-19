package com.miletracker.core.ui

import androidx.compose.ui.window.ComposeUIViewController
import com.miletracker.core.common.AppLog
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    AppLog.init()
    return ComposeUIViewController {
        AppHost { IosDemoApp() }
    }
}
