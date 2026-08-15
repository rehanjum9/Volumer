package com.volumer.app

enum class EnvironmentMode { UNKNOWN, PRIVATE_OR_QUIET, PUBLIC_CROWDED }

data class VolumerSettings(
    val autoMonitoring: Boolean = false,
    val publicVolumePercent: Int = 20,
    val returnMinimumPercent: Int = 45,
    val crowdDeviceThreshold: Int = 6,
    val environmentMode: EnvironmentMode = EnvironmentMode.UNKNOWN,
    val appAdjustedVolume: Boolean = false,
    val savedVolumePercent: Int = 50,
    val lastNearbyDevices: Int = 0,
    val insidePrivatePlace: Boolean = false,
    val privateLat: Double? = null,
    val privateLng: Double? = null,
    val privateRadiusMeters: Float = 120f,
    val lastReason: String = "Not monitoring yet"
) { val hasPrivatePlace: Boolean get() = privateLat != null && privateLng != null }
