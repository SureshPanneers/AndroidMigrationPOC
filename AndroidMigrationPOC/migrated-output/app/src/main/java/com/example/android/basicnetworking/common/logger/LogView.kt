package com.example.android.basicnetworking.common.logger

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 * [LogNode] implementation backed by a [AppCompatTextView]. Appends each incoming message
 * to its own text content, so it can be shown on screen.
 *
 * Thread-safe: callbacks can arrive on background threads, so we post appends to the UI thread.
 */
class LogView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatTextView(context, attrs, defStyle), LogNode {

    /** Next node in the chain. */
    var next: LogNode? = null

    override fun println(priority: Int, tag: String?, msg: String?, tr: Throwable?) {
        val line = buildString {
            if (!msg.isNullOrEmpty()) append(msg)
            if (tr != null) {
                append("\n")
                append(android.util.Log.getStackTraceString(tr))
            }
            append("\n")
        }
        // Ensure UI mutations happen on the UI thread.
        post { append(line) }
        next?.println(priority, tag, msg, tr)
    }
}
