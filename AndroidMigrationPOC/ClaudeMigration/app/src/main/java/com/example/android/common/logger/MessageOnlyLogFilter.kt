package com.example.android.common.logger

/**
 * Simple [LogNode] filter which removes everything except the message. Useful for
 * situations like on-screen log output where you don't want a lot of metadata displayed,
 * just easy-to-read message updates as they're happening.
 */
class MessageOnlyLogFilter(var next: LogNode? = null) : LogNode {

    override fun println(priority: Int, tag: String?, msg: String?, tr: Throwable?) {
        next?.println(Log.NONE, null, msg, null)
    }
}
