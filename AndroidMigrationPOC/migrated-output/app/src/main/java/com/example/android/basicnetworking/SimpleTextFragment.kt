package com.example.android.basicnetworking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.example.android.basicnetworking.databinding.FragmentSimpleTextBinding

/**
 * Simple fragment that displays a single static text string.
 *
 * Preserves the public API of the legacy Java version:
 * - `setText(@StringRes resId: Int)`
 * - `textView` (replaces `getTextView()`)
 */
class SimpleTextFragment : Fragment() {

    private var _binding: FragmentSimpleTextBinding? = null
    private val binding get() = _binding!!

    @StringRes
    private var pendingTextRes: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        _binding = FragmentSimpleTextBinding.inflate(inflater, container, false)
        if (pendingTextRes != 0) {
            binding.textView.setText(pendingTextRes)
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun setText(@StringRes resId: Int) {
        pendingTextRes = resId
        _binding?.textView?.setText(resId)
    }

    val textView: TextView get() = binding.textView
}
