package com.miletracker.core.ui

import androidx.compose.ui.window.ComposeUIViewController
import com.miletracker.core.common.AppLog
import com.miletracker.core.ui.platform.LocalManagerProvider
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    AppLog.init()
    return ComposeUIViewController {
        // PF.4: seed the UIViewController-scoped platform managers at the iOS root.
        LocalManagerProvider {
            AppHost { IosDemoApp() }
        }
    }
}
