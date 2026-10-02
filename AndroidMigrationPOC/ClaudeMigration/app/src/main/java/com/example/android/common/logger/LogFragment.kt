package com.example.android.common.logger

import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.fragment.app.Fragment

/**
 * Simple fragment which contains a [LogView] and uses it to output log data it receives
 * through the [LogNode] interface.
 */
class LogFragment : Fragment() {

    private lateinit var logView: LogView
    private lateinit var scrollView: ScrollView

    val logViewInstance: LogView
        get() = logView

    private fun inflateViews(): View {
        scrollView = ScrollView(requireActivity())
        val scrollParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        scrollView.layoutParams = scrollParams

        logView = LogView(requireActivity())
        val logParams = ViewGroup.LayoutParams(scrollParams).apply {
            height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        logView.layoutParams = logParams
        logView.isClickable = true
        logView.isFocusable = true
        logView.typeface = Typeface.MONOSPACE

        // Want to set padding as 16 dips, setPadding takes pixels.
        val paddingDips = 16
        val scale = resources.displayMetrics.density
        val paddingPixels = ((paddingDips * scale) + .5).toInt()
        logView.setPadding(paddingPixels, paddingPixels, paddingPixels, paddingPixels)
        logView.compoundDrawablePadding = paddingPixels

        logView.gravity = Gravity.BOTTOM
        logView.setTextAppearance(android.R.style.TextAppearance_Holo_Medium)

        scrollView.addView(logView)
        return scrollView
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val result = inflateViews()

        logView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN)
            }
        })
        return result
    }

    fun getLogView(): LogView = logView
}
