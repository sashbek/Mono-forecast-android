package org.pakicek.monoforecast.presentation.ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import org.pakicek.monoforecast.databinding.ActivityBluetoothBinding
import org.pakicek.monoforecast.presentation.ble.adapter.DevicesAdapter
import org.pakicek.monoforecast.presentation.ble.adapter.MetricsAdapter

class BluetoothActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBluetoothBinding
    private val viewModel: BluetoothViewModel by viewModels {
        BluetoothViewModelFactory()
    }

    private lateinit var devicesAdapter: DevicesAdapter
    private lateinit var metricsAdapter: MetricsAdapter

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            checkBluetoothAndStart()
        } else {
            Toast.makeText(this, "Bluetooth permissions denied", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.attach(applicationContext)
        } else {
            Toast.makeText(this, "Bluetooth is required for this app", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBluetoothBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupInsets()
        setupUI()
        observeData()
        checkAndRequestPermissions()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnScan.setOnClickListener {
            if (!hasPermissions()) {
                checkAndRequestPermissions()
                return@setOnClickListener
            }
            viewModel.startScan()
        }

        binding.btnStop.setOnClickListener {
            if (!hasPermissions()) return@setOnClickListener
            viewModel.stopScan()
        }

        binding.btnDisconnect.setOnClickListener {
            if (!hasPermissions()) return@setOnClickListener
            viewModel.disconnect()
        }

        devicesAdapter = DevicesAdapter { device ->
            if (!hasPermissions()) {
                checkAndRequestPermissions()
                return@DevicesAdapter
            }
            viewModel.connectToDevice(device)
        }

        binding.recyclerDevices.layoutManager = LinearLayoutManager(this)
        binding.recyclerDevices.adapter = devicesAdapter

        metricsAdapter = MetricsAdapter { metric ->
            Toast.makeText(this, "${metric.name}: ${metric.value} ${metric.unit}", Toast.LENGTH_SHORT).show()
        }

        binding.recyclerMetrics.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerMetrics.adapter = metricsAdapter

        binding.btnScan.isEnabled = false
        binding.btnStop.isEnabled = false
        binding.btnDisconnect.isEnabled = false
        binding.btnDisconnect.visibility = View.GONE

        binding.speedometerContainer.visibility = View.GONE
        binding.recyclerMetrics.visibility = View.GONE
        binding.recyclerDevices.visibility = View.VISIBLE

        binding.tvStatus.text = "Waiting for permissions..."
    }

    private fun observeData() {
        viewModel.currentSpeed.observe(this) { speed ->
            binding.speedometer.setSpeed(speed)
        }

        viewModel.metrics.observe(this) { metrics ->
            metricsAdapter.submitList(metrics)
        }

        viewModel.connectionStatus.observe(this) { status ->
            binding.tvStatus.text = status
        }

        viewModel.devices.observe(this) { devices ->
            devicesAdapter.submitList(devices)
        }

        viewModel.isScanning.observe(this) { scanning ->
            val hasPerms = hasPermissions()
            binding.btnScan.isEnabled = !scanning && hasPerms
            binding.btnStop.isEnabled = scanning && hasPerms
        }

        viewModel.isConnected.observe(this) { connected ->
            binding.btnDisconnect.isEnabled = connected
            binding.btnDisconnect.visibility = if (connected) View.VISIBLE else View.GONE

            if (connected) {
                binding.recyclerDevices.visibility = View.GONE
                binding.speedometerContainer.visibility = View.VISIBLE
                binding.recyclerMetrics.visibility = View.VISIBLE
                binding.btnScan.visibility = View.GONE
                binding.btnStop.visibility = View.GONE
            } else {
                binding.recyclerDevices.visibility = View.VISIBLE
                binding.speedometerContainer.visibility = View.GONE
                binding.recyclerMetrics.visibility = View.GONE
                binding.btnScan.visibility = View.VISIBLE
                binding.btnStop.visibility = View.VISIBLE
            }
        }
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }

    private fun hasPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    private fun checkAndRequestPermissions() {
        val missing = requiredPermissions().filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            requestPermissionsLauncher.launch(missing.toTypedArray())
        } else {
            checkBluetoothAndStart()
        }
    }

    private fun checkBluetoothAndStart() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (!adapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
        } else {
            viewModel.attach(applicationContext)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cleanup()
    }
}