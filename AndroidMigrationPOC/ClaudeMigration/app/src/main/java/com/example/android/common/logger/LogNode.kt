package com.example.android.common.logger

/**
 * Basic interface for a logging system that can output to one or more targets.
 * Note that in addition to classes that will output these logs in some format,
 * one can also implement this interface over a filter and insert that in the chain,
 * such that no targets further down see certain data, or see manipulated forms of the data.
 */
interface LogNode {
    fun println(priority: Int, tag: String?, msg: String?, tr: Throwable?)
}
