package com.example.android.common.logger

/**
 * Helper object for a list (or tree) of LoggerNodes.
 *
 * When this is set as the head of the list, it functions as a drop-in
 * replacement for [android.util.Log]. Most of the methods here only map a
 * call to its equivalent in [LogNode].
 */
object Log {
    const val NONE = -1
    val VERBOSE = android.util.Log.VERBOSE
    val DEBUG = android.util.Log.DEBUG
    val INFO = android.util.Log.INFO
    val WARN = android.util.Log.WARN
    val ERROR = android.util.Log.ERROR
    val ASSERT = android.util.Log.ASSERT

    private var logNode: LogNode? = null

    fun getLogNode(): LogNode? = logNode

    fun setLogNode(node: LogNode?) {
        logNode = node
    }

    @JvmOverloads
    fun println(priority: Int, tag: String?, msg: String?, tr: Throwable? = null) {
        logNode?.println(priority, tag, msg, tr)
    }

    @JvmOverloads
    fun v(tag: String?, msg: String?, tr: Throwable? = null) = println(VERBOSE, tag, msg, tr)

    @JvmOverloads
    fun d(tag: String?, msg: String?, tr: Throwable? = null) = println(DEBUG, tag, msg, tr)

    @JvmOverloads
    fun i(tag: String?, msg: String?, tr: Throwable? = null) = println(INFO, tag, msg, tr)

    @JvmOverloads
    fun w(tag: String?, msg: String?, tr: Throwable? = null) = println(WARN, tag, msg, tr)

    fun w(tag: String?, tr: Throwable?) = w(tag, null, tr)

    @JvmOverloads
    fun e(tag: String?, msg: String?, tr: Throwable? = null) = println(ERROR, tag, msg, tr)

    @JvmOverloads
    fun wtf(tag: String?, msg: String?, tr: Throwable? = null) = println(ASSERT, tag, msg, tr)

    fun wtf(tag: String?, tr: Throwable?) = wtf(tag, null, tr)
}
