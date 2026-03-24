package org.pakicek.monoforecast.presentation.logs.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.pakicek.monoforecast.R
import org.pakicek.monoforecast.databinding.FragmentLogDetailsBinding
import org.pakicek.monoforecast.domain.model.dto.enums.LogType
import org.pakicek.monoforecast.data.entities.LogWithDetails
import org.pakicek.monoforecast.presentation.logs.LogsViewModelFactory
import org.pakicek.monoforecast.presentation.logs.LogsViewModel
import org.pakicek.monoforecast.presentation.logs.LogDetailsAdapter

class LogDetailsFragment : Fragment(R.layout.fragment_log_details) {

    private var _binding: FragmentLogDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LogsViewModel by activityViewModels {
        val context = requireContext().applicationContext
        LogsViewModelFactory(context)
    }

    private val listAdapter = LogDetailsAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLogDetailsBinding.bind(view)

        setupUI()
        observeData()
    }

    private fun setupUI() {
        binding.closeButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = listAdapter
            setHasFixedSize(true)
            setItemViewCacheSize(20)
        }

        setupChartStyle()
    }

    private fun observeData() {
        val fileId = arguments?.getLong(ARG_FILE_ID) ?: 0L

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getLogsForFile(fileId).collect { logsList ->
                    listAdapter.submitList(logsList)

                    updateChartDataAsync(logsList)
                }
            }
        }
    }

    private fun setupChartStyle() {
        with(binding.chartTemp) {
            description.isEnabled = false
            axisRight.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.textColor = Color.WHITE
            axisLeft.textColor = Color.WHITE
            axisLeft.setDrawGridLines(true)
            legend.isEnabled = false
            setNoDataText("Loading data...")
            setNoDataTextColor(Color.WHITE)

            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
        }
    }

    private fun updateChartDataAsync(logs: List<LogWithDetails>) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            if (logs.isEmpty()) {
                withContext(Dispatchers.Main) { binding.chartTemp.clear() }
                return@launch
            }

            val entries = ArrayList<Entry>()
            val startTime = logs.first().log.timestamp

            val weatherLogs = logs.filter {
                it.log.type == LogType.WEATHER && it.weather != null
            }

            if (weatherLogs.isEmpty()) {
                withContext(Dispatchers.Main) { binding.chartTemp.clear() }
                return@launch
            }

            val maxPoints = 300
            val step = if (weatherLogs.size > maxPoints) weatherLogs.size / maxPoints else 1

            for (i in weatherLogs.indices step step) {
                val item = weatherLogs[i]
                val xValue = (item.log.timestamp - startTime) / 1000f
                val yValue = item.weather!!.tempC.toFloat()
                entries.add(Entry(xValue, yValue))
            }

            withContext(Dispatchers.Main) {
                if (_binding == null) return@withContext

                if (entries.isNotEmpty()) {
                    val dataSet = LineDataSet(entries, "Temperature").apply {
                        color = "#FF9800".toColorInt()
                        valueTextColor = Color.WHITE
                        lineWidth = 2f
                        setDrawCircles(false)
                        setDrawValues(false)
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                        setDrawFilled(true)
                        fillColor = "#FF9800".toColorInt()
                        fillAlpha = 50
                    }

                    val lineData = LineData(dataSet)
                    binding.chartTemp.data = lineData
                    binding.chartTemp.invalidate()
                    binding.chartTemp.animateX(500)
                } else {
                    binding.chartTemp.clear()
                }
            }
        }
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