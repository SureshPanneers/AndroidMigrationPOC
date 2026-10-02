package com.example.android.common.logger

/**
 * Helper class which wraps Android's native Log utility in the [LogNode] interface, so
 * normal logcat output can be one of several targets receiving and outputting logs simultaneously.
 */
class LogWrapper : LogNode {

    var next: LogNode? = null

    override fun println(priority: Int, tag: String?, msg: String?, tr: Throwable?) {
        var useMsg = msg ?: ""
        if (tr != null) {
            useMsg += "\n" + android.util.Log.getStackTraceString(tr)
        }

        // Functionally identical to Log.x(tag, useMsg).
        android.util.Log.println(priority, tag, useMsg)

        next?.println(priority, tag, msg, tr)
    }
}
