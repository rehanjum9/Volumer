package com.volumer.app

import kotlin.math.max

object VolumePolicy {
    fun isCrowded(nearbyDevices: Int, threshold: Int): Boolean = nearbyDevices >= threshold.coerceAtLeast(1)
    fun returnVolume(savedPercent: Int, returnMinimumPercent: Int): Int = max(savedPercent.coerceIn(0, 100), returnMinimumPercent.coerceIn(0, 100))
}
