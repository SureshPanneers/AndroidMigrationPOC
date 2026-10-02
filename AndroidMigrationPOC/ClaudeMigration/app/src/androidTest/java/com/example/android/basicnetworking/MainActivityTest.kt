package com.example.android.basicnetworking

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Modern [ActivityScenario]-based replacement for the legacy
 * `ActivityInstrumentationTestCase2<MainActivity>` precondition test.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Test
    fun activityLaunchesAndFragmentsAreAttached() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val introFragment = activity.supportFragmentManager
                    .findFragmentById(R.id.intro_fragment)
                val logFragment = activity.supportFragmentManager
                    .findFragmentById(R.id.log_fragment)

                assertNotNull("intro_fragment should be attached", introFragment)
                assertNotNull("log_fragment should be attached", logFragment)
            }
        }
    }
}
