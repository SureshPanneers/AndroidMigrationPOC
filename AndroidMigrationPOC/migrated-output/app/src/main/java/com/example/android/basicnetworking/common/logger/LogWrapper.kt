package com.example.android.basicnetworking.common.logger

/**
 * Log node that writes to Android's logcat via [android.util.Log] and then forwards
 * the same event to the next node in the chain (if any).
 *
 * Uses the fully-qualified name `android.util.Log` to avoid clashing with the
 * sibling [Log] singleton in this package.
 */
class LogWrapper : LogNode {
    /** Next node in the chain. */
    var next: LogNode? = null

    override fun println(priority: Int, tag: String?, msg: String?, tr: Throwable?) {
        val useMsg = buildString {
            append(msg.orEmpty())
            if (tr != null) {
                append("\n")
                append(android.util.Log.getStackTraceString(tr))
            }
        }
        android.util.Log.println(priority, tag ?: "", useMsg)
        next?.println(priority, tag, msg, tr)
    }
}
