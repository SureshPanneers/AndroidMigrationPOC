package com.example.android.basicnetworking.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Lifecycle-safe wrapper around [ConnectivityManager].
 *
 * - [status] : hot(ish) stream of [NetworkStatus] updates driven by [ConnectivityManager.NetworkCallback].
 * - [currentStatus] : synchronous snapshot used by the TEST menu action to mirror the legacy "query on tap" behavior.
 *
 * Only holds the application context to avoid activity leaks.
 */
class NetworkMonitor(context: Context) {

    private val appContext: Context = context.applicationContext
    private val connectivityManager: ConnectivityManager =
        appContext.getSystemService(ConnectivityManager::class.java)

    val status: Flow<NetworkStatus> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val caps = connectivityManager.getNetworkCapabilities(network)
                trySend(caps.toStatus())
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                trySend(networkCapabilities.toStatus())
            }

            override fun onLost(network: Network) {
                trySend(NetworkStatus.Unavailable)
            }

            override fun onUnavailable() {
                trySend(NetworkStatus.Unavailable)
            }
        }

        // Emit the current state at subscription time.
        trySend(currentStatus())
        connectivityManager.registerDefaultNetworkCallback(callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

    /** Synchronous snapshot — used by the TEST menu action. */
    fun currentStatus(): NetworkStatus {
        val activeNetwork: Network = connectivityManager.activeNetwork ?: return NetworkStatus.Unavailable
        val caps: NetworkCapabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork) ?: return NetworkStatus.Unavailable
        return caps.toStatus()
    }

    private fun NetworkCapabilities?.toStatus(): NetworkStatus {
        if (this == null) return NetworkStatus.Unavailable
        val hasInternet = hasCapability(NET_CAPABILITY_INTERNET)
        val isValidated = hasCapability(NET_CAPABILITY_VALIDATED)
        if (!hasInternet || !isValidated) return NetworkStatus.Unavailable
        return when {
            hasTransport(TRANSPORT_WIFI) -> NetworkStatus.Wifi
            hasTransport(TRANSPORT_CELLULAR) -> NetworkStatus.Mobile
            else -> NetworkStatus.Other
        }
    }
}
