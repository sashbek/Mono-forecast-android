package org.pakicek.monoforecast.presentation.logs.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.MonoForecastApp
import org.pakicek.monoforecast.R
import org.pakicek.monoforecast.databinding.FragmentLogDetailsBinding
import org.pakicek.monoforecast.presentation.logs.LogDetailsAdapter
import org.pakicek.monoforecast.presentation.logs.LogsViewModel
import org.pakicek.monoforecast.presentation.logs.LogsViewModelFactory

class LogDetailsFragment : Fragment(R.layout.fragment_log_details) {
    private val viewModel: LogsViewModel by activityViewModels {
        LogsViewModelFactory((requireContext().applicationContext as MonoForecastApp).container.logsRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentLogDetailsBinding.bind(view)
        val adapter = LogDetailsAdapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.closeButton.setOnClickListener { parentFragmentManager.popBackStack() }

        val id = arguments?.getLong("ID") ?: 0L

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.getLogsForFile(id).collect { adapter.submitList(it) } }
                launch {
                    viewModel.chartData.collect { entries ->
                        if (entries.isNotEmpty()) {
                            val set = LineDataSet(entries, "Temp").apply {
                                color = Color.RED
                                setDrawCircles(false)
                                setDrawValues(false)
                            }
                            binding.chartTemp.data = LineData(set)
                            binding.chartTemp.invalidate()
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(id: Long) = LogDetailsFragment().apply { arguments = Bundle().apply { putLong("ID", id) } }
    }
}