package com.example.android.basicnetworking

import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import com.example.android.basicnetworking.databinding.ActivityMainBinding
import com.example.android.basicnetworking.network.NetworkMonitor
import com.example.android.basicnetworking.network.NetworkStatus
import com.example.android.common.logger.Log
import com.example.android.common.logger.LogFragment
import com.example.android.common.logger.LogWrapper
import com.example.android.common.logger.MessageOnlyLogFilter

/**
 * Sample application demonstrating how to test whether a device is connected,
 * and if so, whether the connection happens to be Wi-Fi or mobile (it could be
 * something else).
 *
 * This sample uses the logging framework to display log output in the log fragment ([LogFragment]).
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var networkMonitor: NetworkMonitor

    // Reference to the fragment showing events, so we can clear it with a button as necessary.
    private lateinit var logFragment: LogFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        networkMonitor = NetworkMonitor(this)

        // Initialize text fragment that displays intro text.
        val introFragment = supportFragmentManager
            .findFragmentById(R.id.intro_fragment) as SimpleTextFragment
        introFragment.setText(R.string.intro_message)
        introFragment.textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f)

        initializeLogging()

        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    // When the user clicks TEST, display the connection status.
                    R.id.test_action -> {
                        checkNetworkConnection()
                        true
                    }
                    // Clear the log view fragment.
                    R.id.clear_action -> {
                        logFragment.getLogView().text = ""
                        true
                    }
                    else -> false
                }
            }
        })
    }

    /**
     * Check whether the device is connected, and if so, whether the connection
     * is Wi-Fi or mobile (it could be something else).
     */
    private fun checkNetworkConnection() {
        when (networkMonitor.currentStatus()) {
            NetworkStatus.WIFI -> Log.i(TAG, getString(R.string.wifi_connection))
            NetworkStatus.MOBILE -> Log.i(TAG, getString(R.string.mobile_connection))
            NetworkStatus.OTHER -> Log.i(TAG, getString(R.string.no_wifi_or_mobile))
        }
    }

    /** Create a chain of targets that will receive log data. */
    private fun initializeLogging() {
        // Using Log, front-end to the logging chain, emulates android.util.Log method signatures.

        // Wraps Android's native log framework.
        val logWrapper = LogWrapper()
        Log.setLogNode(logWrapper)

        // A filter that strips out everything except the message text.
        val msgFilter = MessageOnlyLogFilter()
        logWrapper.next = msgFilter

        // On screen logging via a fragment with a TextView.
        logFragment = supportFragmentManager.findFragmentById(R.id.log_fragment) as LogFragment
        msgFilter.next = logFragment.getLogView()
    }

    companion object {
        const val TAG = "Basic Network Demo"
    }
}
