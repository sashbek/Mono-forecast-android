package org.pakicek.monoforecast.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.R
import org.pakicek.monoforecast.databinding.FragmentLogListBinding
import org.pakicek.monoforecast.domain.repositories.LogsRepository
import org.pakicek.monoforecast.domain.repositories.SettingsRepository
import org.pakicek.monoforecast.fragments.LogDetailsFragment.Companion.newInstance
import org.pakicek.monoforecast.logic.factories.LogsViewModelFactory
import org.pakicek.monoforecast.logic.viewmodel.LogsViewModel
import org.pakicek.monoforecast.ui.adapter.LogFilesAdapter

class LogListFragment : Fragment(R.layout.fragment_log_list) {

    private var _binding: FragmentLogListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LogsViewModel by activityViewModels {
        val context = requireContext().applicationContext
        val logsRepo = LogsRepository(context)
        val settingsRepo = SettingsRepository(context)
        LogsViewModelFactory(logsRepo, settingsRepo)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLogListBinding.bind(view)

        setupRecyclerView()
        setupListeners()
        observeData()
    }

    private fun setupRecyclerView() {
        val adapter = LogFilesAdapter { file ->
            // Навигация к деталям
            parentFragmentManager.beginTransaction()
                .replace(R.id.logsFragmentContainer, newInstance(file.id))
                .addToBackStack(null)
                .commit()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnStartLogging.setOnClickListener {
            viewModel.toggleLogging()
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getAllFiles().collect { files ->
                    (binding.recyclerView.adapter as? LogFilesAdapter)?.submitList(files)
                }
            }
        }

        viewModel.isLogging.observe(viewLifecycleOwner) { isLogging ->
            val logging = isLogging == true
            with(binding.btnStartLogging) {
                isSelected = logging
                text = getString(
                    if (logging) R.string.stop_logging_button_text else R.string.start_logging_button_text
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}