package org.pakicek.monoforecast

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import org.pakicek.monoforecast.databinding.ActivityBluetoothBinding
import org.pakicek.monoforecast.logic.viewmodel.BluetoothViewModel
import org.pakicek.monoforecast.ui.adapter.MetricsAdapter

class BluetoothActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBluetoothBinding
    private val viewModel: BluetoothViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBluetoothBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupInsets()
        setupUI()
        observeData()

        viewModel.startSimulation()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        val adapter = MetricsAdapter()
        binding.recyclerMetrics.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerMetrics.adapter = adapter
    }

    private fun observeData() {
        viewModel.currentSpeed.observe(this) { speed ->
            binding.speedometer.setSpeed(speed)
        }

        viewModel.metrics.observe(this) { list ->
            (binding.recyclerMetrics.adapter as MetricsAdapter).submitList(list)
        }

        viewModel.connectionStatus.observe(this) { status ->
            binding.tvStatus.text = status
        }
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}