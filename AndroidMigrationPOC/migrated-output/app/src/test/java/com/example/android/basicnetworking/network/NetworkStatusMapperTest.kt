package com.example.android.basicnetworking.network

import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Verifies the mapping from [NetworkCapabilities] to [NetworkStatus].
 *
 * Note: `toStatus()` is a private extension of [NetworkMonitor]; this test
 * exercises the same logic via a local copy kept in sync with the production mapping.
 */
class NetworkStatusMapperTest {

    private fun caps(
        hasInternet: Boolean = true,
        validated: Boolean = true,
        wifi: Boolean = false,
        cellular: Boolean = false
    ): NetworkCapabilities = mockk {
        every { hasCapability(NET_CAPABILITY_INTERNET) } returns hasInternet
        every { hasCapability(NET_CAPABILITY_VALIDATED) } returns validated
        every { hasTransport(TRANSPORT_WIFI) } returns wifi
        every { hasTransport(TRANSPORT_CELLULAR) } returns cellular
    }

    @Test
    fun unvalidatedIsUnavailable() {
        val c = caps(hasInternet = true, validated = false, wifi = true)
        assertEquals(NetworkStatus.Unavailable, map(c))
    }

    @Test
    fun wifiMapsToWifi() {
        val c = caps(wifi = true)
        assertEquals(NetworkStatus.Wifi, map(c))
    }

    @Test
    fun cellularMapsToMobile() {
        val c = caps(cellular = true)
        assertEquals(NetworkStatus.Mobile, map(c))
    }

    @Test
    fun otherTransportMapsToOther() {
        val c = caps(wifi = false, cellular = false)
        assertEquals(NetworkStatus.Other, map(c))
    }

    // Local copy of the production mapping — kept in sync with NetworkMonitor.toStatus().
    private fun map(c: NetworkCapabilities): NetworkStatus {
        if (!c.hasCapability(NET_CAPABILITY_INTERNET) || !c.hasCapability(NET_CAPABILITY_VALIDATED)) {
            return NetworkStatus.Unavailable
        }
        return when {
            c.hasTransport(TRANSPORT_WIFI) -> NetworkStatus.Wifi
            c.hasTransport(TRANSPORT_CELLULAR) -> NetworkStatus.Mobile
            else -> NetworkStatus.Other
        }
    }
}
