package org.pakicek.monoforecast.fragments

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.R
import org.pakicek.monoforecast.databinding.FragmentLogDetailsBinding
import org.pakicek.monoforecast.domain.model.dto.logs.LogFrameEntity
import org.pakicek.monoforecast.domain.repositories.LogsRepository
import org.pakicek.monoforecast.domain.repositories.SettingsRepository
import org.pakicek.monoforecast.logic.factories.LogsViewModelFactory
import org.pakicek.monoforecast.logic.viewmodel.LogsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogDetailsFragment : Fragment(R.layout.fragment_log_details) {

    private var _binding: FragmentLogDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LogsViewModel by activityViewModels {
        val context = requireContext().applicationContext
        val logsRepo = LogsRepository(context)
        val settingsRepo = SettingsRepository(context)
        LogsViewModelFactory(logsRepo, settingsRepo)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLogDetailsBinding.bind(view)

        setupUI()
        observeData()
    }

    private fun setupUI() {
        binding.textView.movementMethod = ScrollingMovementMethod()

        binding.closeButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun observeData() {
        val fileId = arguments?.getLong(ARG_FILE_ID) ?: 0L

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getLogsForFile(fileId).collect { logs ->
                    updateLogsText(logs, fileId)
                }
            }
        }
    }

    private fun updateLogsText(logs: List<LogFrameEntity>, fileId: Long) {
        if (logs.isEmpty()) {
            binding.textView.text = getString(R.string.log_details_empty)
            return
        }

        val sb = StringBuilder()
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        sb.append(getString(R.string.log_details_header_fmt, fileId))

        logs.forEach { log ->
            val time = sdf.format(Date(log.timestamp))
            sb.append("[$time] ${log.type.name} ${log.message}\n")
        }

        binding.textView.text = sb.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_FILE_ID = "file_id"
        fun newInstance(fileId: Long) = LogDetailsFragment().apply {
            arguments = Bundle().apply { putLong(ARG_FILE_ID, fileId) }
        }
    }
}