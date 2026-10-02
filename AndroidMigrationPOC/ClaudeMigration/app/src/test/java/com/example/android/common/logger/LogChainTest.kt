package com.example.android.common.logger

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Verifies the LogWrapper -> MessageOnlyLogFilter chain forwards message-only output correctly. */
class LogChainTest {

    private class CapturingLogNode : LogNode {
        var lastPriority: Int? = null
        var lastTag: String? = null
        var lastMsg: String? = null

        override fun println(priority: Int, tag: String?, msg: String?, tr: Throwable?) {
            lastPriority = priority
            lastTag = tag
            lastMsg = msg
        }
    }

    @Test
    fun `message only filter strips priority and tag`() {
        val capture = CapturingLogNode()
        val filter = MessageOnlyLogFilter(capture)

        filter.println(android.util.Log.INFO, "SomeTag", "hello world", null)

        assertEquals(Log.NONE, capture.lastPriority)
        assertNull(capture.lastTag)
        assertEquals("hello world", capture.lastMsg)
    }

    @Test
    fun `log wrapper forwards to next node unchanged`() {
        val capture = CapturingLogNode()
        val wrapper = LogWrapper().apply { next = capture }

        wrapper.println(android.util.Log.DEBUG, "Tag", "message", null)

        assertEquals(android.util.Log.DEBUG, capture.lastPriority)
        assertEquals("Tag", capture.lastTag)
        assertEquals("message", capture.lastMsg)
    }

    @Test
    fun `full chain delivers message only output to the final node`() {
        val capture = CapturingLogNode()
        val filter = MessageOnlyLogFilter(capture)
        val wrapper = LogWrapper().apply { next = filter }
        Log.setLogNode(wrapper)

        Log.i("MyTag", "chained message")

        assertEquals(Log.NONE, capture.lastPriority)
        assertNull(capture.lastTag)
        assertEquals("chained message", capture.lastMsg)
    }
}
