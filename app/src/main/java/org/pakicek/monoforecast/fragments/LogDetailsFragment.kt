package org.pakicek.monoforecast.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import org.pakicek.monoforecast.R
import org.pakicek.monoforecast.databinding.FragmentLogDetailsBinding

class LogDetailsFragment : Fragment(R.layout.fragment_log_details) {
    companion object {
        private const val ARG_FILE_ID = "file_id"

        fun newInstance(fileId: Long): LogDetailsFragment {
            val fragment = LogDetailsFragment()
            fragment.arguments = Bundle().apply {
                putLong(ARG_FILE_ID, fileId)
            }
            return fragment
        }
    }

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