package org.pakicek.monoforecast.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import org.pakicek.monoforecast.R
import org.pakicek.monoforecast.databinding.FragmentLogListBinding

class LogListFragment : Fragment(R.layout.fragment_log_list) {
    private var _binding: FragmentLogListBinding? = null

    private val binding
        get() = _binding ?: throw IllegalStateException("Binding must not be null")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLogListBinding.bind(view)

        val button = view.findViewById<Button>(R.id.btnFakeLog)

        binding.btnFakeLog.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.logsFragmentContainer, LogDetailsFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}