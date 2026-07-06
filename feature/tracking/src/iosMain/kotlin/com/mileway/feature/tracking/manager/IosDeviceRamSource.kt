package com.mileway.feature.tracking.manager

import platform.Foundation.NSProcessInfo

/** iOS actual: total physical RAM via [NSProcessInfo.physicalMemory], mirrors the Android actual. */
class IosDeviceRamSource : DeviceRamSource {
    override fun totalRamBytes(): Long = NSProcessInfo.processInfo.physicalMemory.toLong()
}
