package com.example.android.basicnetworking.network

/**
 * Represents the current connectivity state of the device, replacing the
 * deprecated boolean pair (`wifiConnected`, `mobileConnected`) from the
 * legacy sample.
 */
sealed interface NetworkStatus {
    data object Wifi : NetworkStatus
    data object Mobile : NetworkStatus
    data object Other : NetworkStatus
    data object Unavailable : NetworkStatus
}
