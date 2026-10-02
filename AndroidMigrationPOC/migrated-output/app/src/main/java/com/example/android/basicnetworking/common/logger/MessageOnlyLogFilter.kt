package com.example.android.basicnetworking.common.logger

/**
 * Log node that forwards only the raw message text (dropping priority, tag and throwable)
 * to the next node.
 */
class MessageOnlyLogFilter : LogNode {
    /** Next node in the chain. */
    var next: LogNode? = null

    override fun println(priority: Int, tag: String?, msg: String?, tr: Throwable?) {
        next?.println(Log.NONE, null, msg, null)
    }
}
