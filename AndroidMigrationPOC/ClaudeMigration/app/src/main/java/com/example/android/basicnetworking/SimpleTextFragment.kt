package com.example.android.basicnetworking

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

/**
 * Simple fragment containing only a TextView. Used to display intro/tutorial-style text.
 */
class SimpleTextFragment : Fragment() {

    private var text: String? = null
    private var textId: Int = -1

    private var _textView: TextView? = null
    val textView: TextView
        get() = requireNotNull(_textView) { "textView accessed before onCreateView" }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        processArguments()

        val createdView = TextView(requireActivity()).apply {
            gravity = Gravity.CENTER
        }
        _textView = createdView

        text?.let {
            createdView.text = it
            Log.i(TAG, it)
        }
        return createdView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _textView = null
    }

    /** Changes the text for this TextView, according to the resource ID provided. */
    fun setText(stringId: Int) {
        textView.text = getString(stringId)
    }

    private fun processArguments() {
        val args = arguments ?: return
        when {
            args.containsKey(TEXT_KEY) -> {
                text = args.getString(TEXT_KEY)
                Log.d("Constructor", "Added Text.")
            }
            args.containsKey(TEXT_ID_KEY) -> {
                textId = args.getInt(TEXT_ID_KEY)
                text = getString(textId)
            }
        }
    }

    companion object {
        private const val TAG = "SimpleTextFragment"
        const val TEXT_KEY = "text"
        const val TEXT_ID_KEY = "text_id"
    }
}
