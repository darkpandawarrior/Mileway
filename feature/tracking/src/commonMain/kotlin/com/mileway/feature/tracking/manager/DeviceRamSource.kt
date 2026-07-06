package com.mileway.feature.tracking.manager

/** Platform query for total physical device RAM, feeds [DeviceTierManager.tierFor]. */
interface DeviceRamSource {
    fun totalRamBytes(): Long
}
