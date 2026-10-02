package com.example.android.basicnetworking.common.logger

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.android.basicnetworking.databinding.FragmentLogBinding

/**
 * Fragment hosting an on-screen [LogView] inside a scrollable container.
 *
 * Public API preserved from the legacy Java version:
 * - `logView` property (replaces `getLogView()`).
 */
class LogFragment : Fragment() {

    private var _binding: FragmentLogBinding? = null
    private val binding get() = _binding!!

    val logView: LogView get() = binding.logView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
