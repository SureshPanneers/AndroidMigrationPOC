package com.example.android.basicnetworking.common.logger

/**
 * Basic interface for a node in a chain of logging targets.
 */
fun interface LogNode {
    fun println(priority: Int, tag: String?, msg: String?, tr: Throwable?)
}
