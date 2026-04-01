package org.pakicek.monoforecast.presentation.ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.databinding.ActivityBluetoothBinding
import org.pakicek.monoforecast.logic.service.MainService
import org.pakicek.monoforecast.logic.viewmodel.ble.BluetoothViewModel
import org.pakicek.monoforecast.logic.viewmodel.ble.BluetoothViewModelFactory
import org.pakicek.monoforecast.presentation.ble.adapter.DevicesAdapter
import org.pakicek.monoforecast.presentation.ble.adapter.MetricsAdapter

class BluetoothActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "BluetoothActivity"
    }

    private lateinit var binding: ActivityBluetoothBinding
    private val viewModel: BluetoothViewModel by viewModels {
        BluetoothViewModelFactory()
    }

    private lateinit var devicesAdapter: DevicesAdapter
    private lateinit var metricsAdapter: MetricsAdapter

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            checkBluetoothAndStart()
        } else {
            val deniedPermissions = permissions.filter { !it.value }.keys
            Toast.makeText(
                this,
                "Bluetooth permissions required: $deniedPermissions",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }

    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            connectToMainService()
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
        setupMainServiceConnection()

        checkAndRequestPermissions()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnScan.setOnClickListener {
            Log.d(TAG, "Scan button clicked")
            if (hasPermissions()) {
                viewModel.startScan()
                binding.tvStatus.text = "Scanning for devices..."
            } else {
                Toast.makeText(this, "Please grant Bluetooth permissions", Toast.LENGTH_SHORT).show()
                checkAndRequestPermissions()
            }
        }

        binding.btnStop.setOnClickListener {
            Log.d(TAG, "Stop button clicked")
            if (hasPermissions()) {
                viewModel.stopScan()
                binding.tvStatus.text = "Scan stopped"
            }
        }

        binding.btnDisconnect.setOnClickListener @androidx.annotation.RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT) {
            Log.d(TAG, "Disconnect button clicked")
            viewModel.disconnect()
        }

        devicesAdapter = DevicesAdapter @androidx.annotation.RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT) { device ->
            Log.d(TAG, "Device selected: ${device.name} (${device.address})")
            viewModel.connectToDevice(device)
            binding.tvStatus.text = "Connecting to ${device.name}..."
        }
        binding.recyclerDevices.layoutManager = LinearLayoutManager(this)
        binding.recyclerDevices.adapter = devicesAdapter

        metricsAdapter = MetricsAdapter { metric ->
            Log.d(TAG, "Metric clicked: ${metric.name} = ${metric.value} ${metric.unit}")
            Toast.makeText(this, "${metric.name}: ${metric.value} ${metric.unit}", Toast.LENGTH_SHORT).show()
        }
        binding.recyclerMetrics.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerMetrics.adapter = metricsAdapter

        binding.btnScan.isEnabled = false
        binding.btnStop.isEnabled = false
        binding.btnDisconnect.isEnabled = false

        binding.tvStatus.text = "Waiting for permissions..."
    }

    private fun observeData() {
        viewModel.currentSpeed.observe(this) { speed ->
            binding.speedometer.setSpeed(speed)
        }

        viewModel.metrics.observe(this) { metricsList ->
            Log.d(TAG, "Metrics updated: ${metricsList.size} metrics")
            metricsAdapter.submitList(metricsList)
        }

        viewModel.connectionStatus.observe(this) { status ->
            binding.tvStatus.text = status
        }

        viewModel.devices.observe(this) { devices ->
            Log.d(TAG, "Devices LiveData updated: ${devices.size} devices")
            devicesAdapter.submitList(devices)

            devices.forEach { device ->
                Log.d(TAG, "  Device in list: ${device.name} (${device.address})")
            }
        }

        viewModel.isConnected.observe(this) { isConnected ->
            Log.d(TAG, "isConnected changed: $isConnected")
            binding.btnDisconnect.isEnabled = isConnected

            if (isConnected) {
                Log.d(TAG, "Device connected - hiding device list, showing metrics")
                binding.tvStatus.text = "Connected"

                binding.recyclerDevices.visibility = View.GONE
                binding.recyclerMetrics.visibility = View.VISIBLE
                binding.btnScan.visibility = View.GONE
                binding.btnStop.visibility = View.GONE
                binding.btnDisconnect.visibility = View.VISIBLE
            } else {
                Log.d(TAG, "Device disconnected - showing device list, hiding metrics")

                binding.recyclerDevices.visibility = View.VISIBLE
                binding.recyclerMetrics.visibility = View.GONE
                if (hasPermissions()) {
                    binding.btnScan.visibility = View.VISIBLE
                    binding.btnStop.visibility = View.VISIBLE
                }
                binding.btnDisconnect.visibility = View.VISIBLE

                // viewModel.clearDevices() // опционально
            }
        }

        viewModel.isScanning.observe(this) { isScanning ->
            Log.d(TAG, "isScanning changed: $isScanning")
            val hasPerms = hasPermissions()
            binding.btnScan.isEnabled = !isScanning && hasPerms
            binding.btnStop.isEnabled = isScanning && hasPerms

            if (isScanning) {
                binding.tvStatus.text = "Scanning for devices..."
            }
        }

        viewModel.isConnected.observe(this) { isConnected ->
            Log.d(TAG, "isConnected changed: $isConnected")
            binding.btnDisconnect.isEnabled = isConnected

            if (isConnected) {
                binding.tvStatus.text = "Connected"
                binding.recyclerDevices.visibility = View.GONE
                binding.recyclerMetrics.visibility = View.VISIBLE
                binding.btnScan.visibility = View.GONE
                binding.btnStop.visibility = View.GONE
            } else {
                binding.recyclerDevices.visibility = View.VISIBLE
                binding.recyclerMetrics.visibility = View.GONE
                binding.btnScan.visibility = View.VISIBLE
                binding.btnStop.visibility = View.VISIBLE
            }
        }
    }

    private fun setupMainServiceConnection() {
        lifecycleScope.launch {
            MainService.deviceFound?.collect { device ->
                Log.d(TAG, "Device found via MainService: ${device.name}")
                viewModel.addDeviceDirectly(device)
            }
        }

        lifecycleScope.launch {
            MainService.metricsUpdate?.collect { metrics ->
                Log.d(TAG, "Metrics update via MainService")
                viewModel.updateMetricsDirectly(metrics)
            }
        }

        lifecycleScope.launch {
            MainService.error?.collect { error ->
                Log.e(TAG, "Error from MainService: $error")
                viewModel.onError(error)
            }
        }

        lifecycleScope.launch {
            MainService.connectionState?.collect { isConnected ->
                Log.d(TAG, "Connection state via MainService: $isConnected")
                if (isConnected) {
                    viewModel.onConnected()
                } else {
                    viewModel.onDisconnected()
                }
            }
        }
    }

    private fun connectToMainService() {
        if (MainService.instance == null) {
            Log.d(TAG, "MainService not running, starting...")
            val intent = Intent(this, MainService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }

        MainService.instance?.bleFeature?.let { bleFeature ->
            Log.d(TAG, "Setting BleFeature to ViewModel")
            viewModel.setBLEService(bleFeature)
        } ?: run {
            Log.e(TAG, "BleFeature is null, retrying in 1 second")
            binding.tvStatus.postDelayed({
                connectToMainService()
            }, 1000)
        }
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
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

    private fun checkAndRequestPermissions() {
        val permissionsNeeded = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }

        if (permissionsNeeded.isNotEmpty()) {
            requestPermissionsLauncher.launch(permissionsNeeded.toTypedArray())
        } else {
            checkBluetoothAndStart()
        }
    }

    private fun checkBluetoothAndStart() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
        } else {
            connectToMainService()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")

        viewModel.isScanning.value?.let { isScanning ->
            val hasPerms = hasPermissions()
            binding.btnScan.isEnabled = !isScanning && hasPerms
            binding.btnStop.isEnabled = isScanning && hasPerms
        }

        if (hasPermissions() && MainService.instance?.bleFeature == null) {
            connectToMainService()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        viewModel.cleanup()
    }
}