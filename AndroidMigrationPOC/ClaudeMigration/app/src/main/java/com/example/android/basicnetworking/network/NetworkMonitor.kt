package com.example.android.basicnetworking.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.getSystemService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Modern replacement for the deprecated [ConnectivityManager.getActiveNetworkInfo] /
 * [android.net.NetworkInfo] APIs used by the original sample.
 *
 * Uses [ConnectivityManager.activeNetwork] + [NetworkCapabilities] for on-demand checks,
 * and [ConnectivityManager.NetworkCallback] (wrapped in a [callbackFlow]) for apps that
 * want to react to connectivity changes over time.
 */
class NetworkMonitor(context: Context) {

    private val connectivityManager: ConnectivityManager =
        requireNotNull(context.applicationContext.getSystemService<ConnectivityManager>()) {
            "ConnectivityManager not available on this device"
        }

    /**
     * One-shot connectivity check, directly equivalent to the legacy
     * `connMgr.getActiveNetworkInfo()` + `activeInfo.isConnected()` + `activeInfo.getType()` flow.
     *
     * Deliberately does NOT require [NetworkCapabilities.NET_CAPABILITY_VALIDATED]: the legacy
     * `NetworkInfo.isConnected()` reported "connected" the moment the device associated with a
     * network, even before internet validation completed (e.g. captive portals, unvalidated
     * Wi-Fi). Requiring NET_CAPABILITY_VALIDATED here would be stricter than the original
     * behavior, so it is intentionally omitted to preserve behavioral parity.
     */
    fun currentStatus(): NetworkStatus {
        val activeNetwork = connectivityManager.activeNetwork ?: return NetworkStatus.OTHER
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            ?: return NetworkStatus.OTHER

        if (!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            return NetworkStatus.OTHER
        }

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkStatus.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkStatus.MOBILE
            else -> NetworkStatus.OTHER
        }
    }

    /**
     * Reactive variant for callers that want to observe connectivity changes over time,
     * built on the modern [ConnectivityManager.NetworkCallback] API.
     */
    fun statusFlow(): Flow<NetworkStatus> = callbackFlow {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(currentStatus())
            }

            override fun onLost(network: Network) {
                trySend(NetworkStatus.OTHER)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                trySend(currentStatus())
            }
        }

        connectivityManager.registerNetworkCallback(request, callback)
        trySend(currentStatus())

        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }
}
