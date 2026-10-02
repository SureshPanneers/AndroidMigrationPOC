package com.example.android.basicnetworking.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.core.content.getSystemService
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pure JVM-safe unit tests for [NetworkMonitor]'s mapping logic, replacing the manual,
 * device-dependent verification that was previously only possible via instrumentation.
 */
class NetworkStatusMapperTest {

    private fun monitorWith(capabilities: NetworkCapabilities?): NetworkMonitor {
        val network = mockk<Network>()
        val connectivityManager = mockk<ConnectivityManager> {
            every { activeNetwork } returns if (capabilities == null) null else network
            every { getNetworkCapabilities(network) } returns capabilities
        }
        val appContext = mockk<Context>(relaxed = true)
        val context = mockk<Context> {
            every { applicationContext } returns appContext
        }
        every { appContext.getSystemService<ConnectivityManager>() } returns connectivityManager

        return NetworkMonitor(context)
    }

    private fun capabilitiesWith(vararg transports: Int, hasInternet: Boolean = true): NetworkCapabilities =
        mockk {
            every { hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns hasInternet
            every { hasTransport(any()) } answers { transports.contains(firstArg()) }
        }

    @Test
    fun `no active network maps to OTHER`() {
        val monitor = monitorWith(capabilities = null)
        assertEquals(NetworkStatus.OTHER, monitor.currentStatus())
    }

    @Test
    fun `wifi transport with internet maps to WIFI`() {
        val monitor = monitorWith(capabilitiesWith(NetworkCapabilities.TRANSPORT_WIFI))
        assertEquals(NetworkStatus.WIFI, monitor.currentStatus())
    }

    @Test
    fun `cellular transport with internet maps to MOBILE`() {
        val monitor = monitorWith(capabilitiesWith(NetworkCapabilities.TRANSPORT_CELLULAR))
        assertEquals(NetworkStatus.MOBILE, monitor.currentStatus())
    }

    @Test
    fun `no internet capability maps to OTHER even with a transport`() {
        val monitor = monitorWith(
            capabilitiesWith(NetworkCapabilities.TRANSPORT_WIFI, hasInternet = false)
        )
        assertEquals(NetworkStatus.OTHER, monitor.currentStatus())
    }

    @Test
    fun `unvalidated wifi still maps to WIFI (legacy NetworkInfo isConnected parity)`() {
        // NET_CAPABILITY_VALIDATED is intentionally NOT checked by NetworkMonitor, to preserve
        // the legacy NetworkInfo#isConnected() behavior of reporting captive-portal / unvalidated
        // Wi-Fi as connected.
        val monitor = monitorWith(capabilitiesWith(NetworkCapabilities.TRANSPORT_WIFI))
        assertEquals(NetworkStatus.WIFI, monitor.currentStatus())
    }

    @Test
    fun `other transport with internet maps to OTHER`() {
        val monitor = monitorWith(capabilitiesWith(NetworkCapabilities.TRANSPORT_ETHERNET))
        assertEquals(NetworkStatus.OTHER, monitor.currentStatus())
    }
}
