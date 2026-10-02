/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */
package com.example.android.basicnetworking

import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.android.basicnetworking.common.logger.Log
import com.example.android.basicnetworking.common.logger.LogFragment
import com.example.android.basicnetworking.common.logger.LogWrapper
import com.example.android.basicnetworking.common.logger.MessageOnlyLogFilter
import com.example.android.basicnetworking.databinding.SampleMainBinding
import com.example.android.basicnetworking.network.NetworkMonitor
import com.example.android.basicnetworking.network.NetworkStatus
import kotlinx.coroutines.launch

/**
 * Sample application demonstrating how to test whether a device is connected,
 * and if so, whether the connection happens to be Wi-Fi or mobile (it could be
 * something else, e.g. Ethernet, VPN).
 *
 * This sample uses the logging framework to display log output in the log
 * fragment ([LogFragment]).
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: SampleMainBinding
    private var logFragment: LogFragment? = null
    private val networkMonitor: NetworkMonitor by lazy { NetworkMonitor(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SampleMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the intro text fragment.
        val introFragment = supportFragmentManager
            .findFragmentById(R.id.intro_fragment) as? SimpleTextFragment
        introFragment?.setText(R.string.intro_message)
        introFragment?.textView?.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)

        // Initialize the logging framework.
        initializeLogging()

        // Register the MenuProvider (modern replacement for onCreateOptionsMenu/onOptionsItemSelected).
        addMenuProvider(mainMenuProvider, this, Lifecycle.State.STARTED)

        // Keep the network monitor hot while the activity is at least STARTED.
        // We do not auto-log here — the TEST menu item is responsible for logging,
        // preserving legacy behavior.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                networkMonitor.status.collect { /* reserved for future UI */ }
            }
        }
    }

    private val mainMenuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.main, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
            R.id.test_action -> {
                checkNetworkConnection()
                true
            }
            R.id.clear_action -> {
                logFragment?.logView?.text = ""
                true
            }
            else -> false
        }
    }

    /**
     * Check whether the device is connected, and if so, whether the connection
     * is Wi-Fi, mobile, something else, or unavailable.
     *
     * Uses the modern NetworkCapabilities API via [NetworkMonitor.currentStatus],
     * replacing the deprecated getActiveNetworkInfo / isConnected / TYPE_WIFI /
     * TYPE_MOBILE API surface.
     */
    private fun checkNetworkConnection() {
        when (networkMonitor.currentStatus()) {
            NetworkStatus.Wifi -> Log.i(TAG, getString(R.string.wifi_connection))
            NetworkStatus.Mobile -> Log.i(TAG, getString(R.string.mobile_connection))
            NetworkStatus.Other -> Log.i(TAG, getString(R.string.other_connection))
            NetworkStatus.Unavailable -> Log.i(TAG, getString(R.string.no_wifi_or_mobile))
        }
    }

    /** Create a chain of targets that will receive log data. */
    private fun initializeLogging() {
        // Wraps Android's native log framework.
        val logWrapper = LogWrapper()
        Log.logNode = logWrapper

        // A filter that strips out everything except the message text.
        val msgFilter = MessageOnlyLogFilter()
        logWrapper.next = msgFilter

        // On-screen logging via a fragment with a TextView.
        logFragment = supportFragmentManager
            .findFragmentById(R.id.log_fragment) as? LogFragment
        msgFilter.next = logFragment?.logView
    }

    companion object {
        const val TAG = "Basic Network Demo"
    }
}
