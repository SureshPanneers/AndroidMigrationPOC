package com.example.android.basicnetworking.common.logger

/**
 * Helper singleton that mimics the API of [android.util.Log] but delegates to a chain of [LogNode]s.
 *
 * This is a direct Kotlin port of the Google sample's `com.example.android.common.logger.Log`
 * utility. Setter/getter boilerplate is replaced by a Kotlin property.
 */
object Log {
    const val NONE = -1
    const val VERBOSE = 2
    const val DEBUG = 3
    const val INFO = 4
    const val WARN = 5
    const val ERROR = 6
    const val ASSERT = 7

    /** First node in the chain. All log calls are forwarded to this node (and down the chain). */
    @JvmStatic
    var logNode: LogNode? = null

    @JvmStatic
    @JvmOverloads
    fun println(priority: Int, tag: String?, msg: String?, tr: Throwable? = null) {
        logNode?.println(priority, tag, msg, tr)
    }

    // ---- VERBOSE ----
    @JvmStatic fun v(tag: String?, msg: String?) = println(VERBOSE, tag, msg, null)
    @JvmStatic fun v(tag: String?, msg: String?, tr: Throwable?) = println(VERBOSE, tag, msg, tr)

    // ---- DEBUG ----
    @JvmStatic fun d(tag: String?, msg: String?) = println(DEBUG, tag, msg, null)
    @JvmStatic fun d(tag: String?, msg: String?, tr: Throwable?) = println(DEBUG, tag, msg, tr)

    // ---- INFO ----
    @JvmStatic fun i(tag: String?, msg: String?) = println(INFO, tag, msg, null)
    @JvmStatic fun i(tag: String?, msg: String?, tr: Throwable?) = println(INFO, tag, msg, tr)

    // ---- WARN ----
    @JvmStatic fun w(tag: String?, msg: String?) = println(WARN, tag, msg, null)
    @JvmStatic fun w(tag: String?, msg: String?, tr: Throwable?) = println(WARN, tag, msg, tr)
    @JvmStatic fun w(tag: String?, tr: Throwable?) = println(WARN, tag, null, tr)

    // ---- ERROR ----
    @JvmStatic fun e(tag: String?, msg: String?) = println(ERROR, tag, msg, null)
    @JvmStatic fun e(tag: String?, msg: String?, tr: Throwable?) = println(ERROR, tag, msg, tr)

    // ---- WTF (ASSERT) ----
    @JvmStatic fun wtf(tag: String?, msg: String?) = println(ASSERT, tag, msg, null)
    @JvmStatic fun wtf(tag: String?, msg: String?, tr: Throwable?) = println(ASSERT, tag, msg, tr)
    @JvmStatic fun wtf(tag: String?, tr: Throwable?) = println(ASSERT, tag, null, tr)
}
