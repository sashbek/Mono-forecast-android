package org.pakicek.monoforecast.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import org.pakicek.monoforecast.R
import org.pakicek.monoforecast.databinding.FragmentLogDetailsBinding

class LogDetailsFragment : Fragment(R.layout.fragment_log_details) {

    private var _binding: FragmentLogDetailsBinding? = null
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding must not be null")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLogDetailsBinding.bind(view)

        binding.closeButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}