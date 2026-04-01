package org.pakicek.monoforecast.presentation.logs.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.MonoForecastApp
import org.pakicek.monoforecast.R
import org.pakicek.monoforecast.databinding.FragmentLogListBinding
import org.pakicek.monoforecast.presentation.logs.LogsViewModel
import org.pakicek.monoforecast.presentation.logs.LogsViewModelFactory
import org.pakicek.monoforecast.presentation.logs.adapter.LogFilesAdapter

class LogListFragment : Fragment(R.layout.fragment_log_list) {
    private val viewModel: LogsViewModel by activityViewModels {
        LogsViewModelFactory((requireContext().applicationContext as MonoForecastApp).container.logsRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentLogListBinding.bind(view)
        val adapter = LogFilesAdapter {
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.logsFragmentContainer,
                    LogDetailsFragment.newInstance(it.id)
                )
                .addToBackStack(null).commit()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
        binding.btnStartLogging.setOnClickListener { viewModel.toggleLogging() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getAllFiles().collect { adapter.submitList(it) }
            }
        }
        viewModel.isLogging.observe(viewLifecycleOwner) {
            binding.btnStartLogging.isSelected = it
            binding.btnStartLogging.text = getString(if (it) R.string.stop_logging_button_text else R.string.start_logging_button_text)
        }
    }
}