package com.example.android.common.logger

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/** Simple TextView which is used to output log data received through the [LogNode] interface. */
class LogView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr), LogNode {

    var next: LogNode? = null

    override fun println(priority: Int, tag: String?, msg: String?, tr: Throwable?) {
        val priorityStr = when (priority) {
            android.util.Log.VERBOSE -> "VERBOSE"
            android.util.Log.DEBUG -> "DEBUG"
            android.util.Log.INFO -> "INFO"
            android.util.Log.WARN -> "WARN"
            android.util.Log.ERROR -> "ERROR"
            android.util.Log.ASSERT -> "ASSERT"
            else -> null
        }

        val exceptionStr = tr?.let { android.util.Log.getStackTraceString(it) }

        val delimiter = "\t"
        val output = buildString {
            appendIfNotNull(priorityStr, delimiter)
            appendIfNotNull(tag, delimiter)
            appendIfNotNull(msg, delimiter)
            appendIfNotNull(exceptionStr, delimiter)
        }

        // In case this was originally called from a background thread, make sure the
        // update occurs on the UI thread.
        (context as? Activity)?.runOnUiThread {
            append(output)
        } ?: append(output)

        next?.println(priority, tag, msg, tr)
    }
}

private fun StringBuilder.appendIfNotNull(addStr: String?, delimiter: String) {
    if (addStr != null) {
        append(addStr)
        append(delimiter)
    }
}
