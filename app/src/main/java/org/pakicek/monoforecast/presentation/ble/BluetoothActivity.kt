package org.pakicek.monoforecast.presentation.ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import org.pakicek.monoforecast.databinding.ActivityBluetoothBinding
import org.pakicek.monoforecast.presentation.ble.connection.models.WheelDevice
import org.pakicek.monoforecast.presentation.ble.connection.models.WheelMetrics
import org.pakicek.monoforecast.presentation.ble.connection.service.BLEService
import org.pakicek.monoforecast.presentation.ble.MetricsAdapter

class BluetoothActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "BluetoothActivity"
    }

    private lateinit var binding: ActivityBluetoothBinding
    private val viewModel: BluetoothViewModel by viewModels()

    private var bleService: BLEService? = null
    private var isServiceBound = false

    private lateinit var devicesAdapter: DevicesAdapter
    private lateinit var metricsAdapter: MetricsAdapter

    private lateinit var localBroadcastManager: LocalBroadcastManager

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
            bindBLEService()
        } else {
            Toast.makeText(this, "Bluetooth is required for this app", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    // Receiver для LocalBroadcastManager
    private val activityReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "!!! LOCAL BROADCAST RECEIVED !!! Action: ${intent?.action}")

            when (intent?.action) {
                BLEService.ACTION_DEVICE_FOUND -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val device = intent.getParcelableExtra(BLEService.EXTRA_DEVICE, WheelDevice::class.java)
                        device?.let {
                            Log.d(TAG, "Device found via LocalBroadcast: ${it.name}")
                            viewModel.addDeviceDirectly(it)
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        val device = intent.getParcelableExtra<WheelDevice>(BLEService.EXTRA_DEVICE)
                        device?.let {
                            Log.d(TAG, "Device found via LocalBroadcast: ${it.name}")
                            viewModel.addDeviceDirectly(it)
                        }
                    }
                }
                BLEService.ACTION_SCAN_STARTED -> {
                    Log.d(TAG, "Scan started event received via LocalBroadcast")
                    viewModel.onScanStarted()
                }
                BLEService.ACTION_SCAN_STOPPED -> {
                    Log.d(TAG, "Scan stopped event received via LocalBroadcast")
                    viewModel.onScanStopped()
                }
                BLEService.ACTION_CONNECTED -> {
                    Log.d(TAG, "Connected event received via LocalBroadcast")
                    viewModel.onConnected()
                }
                BLEService.ACTION_DISCONNECTED -> {
                    Log.d(TAG, "Disconnected event received via LocalBroadcast")
                    viewModel.onDisconnected()
                }
                BLEService.ACTION_METRICS_UPDATE -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val metrics = intent.getParcelableExtra(BLEService.EXTRA_METRICS, WheelMetrics::class.java)
                        metrics?.let {
                            Log.d(TAG, "Metrics update via LocalBroadcast")
                            viewModel.updateMetricsDirectly(it)
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        val metrics = intent.getParcelableExtra<WheelMetrics>(BLEService.EXTRA_METRICS)
                        metrics?.let {
                            Log.d(TAG, "Metrics update via LocalBroadcast")
                            viewModel.updateMetricsDirectly(it)
                        }
                    }
                }
                BLEService.ACTION_ERROR -> {
                    val error = intent.getStringExtra(BLEService.EXTRA_ERROR)
                    Log.d(TAG, "Error event received via LocalBroadcast: $error")
                    viewModel.onError(error)
                }
                BLEService.ACTION_BLUETOOTH_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BLEService.EXTRA_BLUETOOTH_STATE, -1)
                    when (state) {
                        BluetoothAdapter.STATE_OFF -> {
                            viewModel.onError("Bluetooth is off")
                        }
                        BluetoothAdapter.STATE_ON -> {
                            viewModel.onScanStarted()
                        }
                    }
                }
            }
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "Service connected")
            val binder = service as BLEService.LocalBinder
            bleService = binder.getService()
            isServiceBound = true
            viewModel.setBLEService(bleService)

            if (hasPermissions()) {
                Log.d(TAG, "Starting scan")
                viewModel.startScan()
                runOnUiThread {
                    binding.tvStatus.text = "Scanning for devices..."
                }
            } else {
                Log.e(TAG, "No permissions after service connected")
                runOnUiThread {
                    binding.tvStatus.text = "No permissions"
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "Service disconnected")
            isServiceBound = false
            bleService = null
            viewModel.setBLEService(null)
            runOnUiThread {
                binding.tvStatus.text = "Service disconnected"
            }
        }
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBluetoothBinding.inflate(layoutInflater)
        setContentView(binding.root)

        localBroadcastManager = LocalBroadcastManager.getInstance(this)

        setupInsets()
        setupUI()
        observeData()

        registerReceiver()
        checkAndRequestPermissions()
    }

    private fun registerReceiver() {
        Log.d(TAG, "Registering LocalBroadcastReceiver in Activity")

        val filter = IntentFilter().apply {
            addAction(BLEService.ACTION_DEVICE_FOUND)
            addAction(BLEService.ACTION_SCAN_STARTED)
            addAction(BLEService.ACTION_SCAN_STOPPED)
            addAction(BLEService.ACTION_CONNECTED)
            addAction(BLEService.ACTION_DISCONNECTED)
            addAction(BLEService.ACTION_METRICS_UPDATE)
            addAction(BLEService.ACTION_ERROR)
            addAction(BLEService.ACTION_BLUETOOTH_STATE_CHANGED)
        }

        try {
            localBroadcastManager.unregisterReceiver(activityReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver not registered
        }

        localBroadcastManager.registerReceiver(activityReceiver, filter)
        Log.d(TAG, "LocalBroadcastReceiver registered successfully")

        // Тестовый broadcast для проверки
        Handler(Looper.getMainLooper()).postDelayed({
            val testIntent = Intent(BLEService.ACTION_DEVICE_FOUND)
            testIntent.putExtra("test", true)
            localBroadcastManager.sendBroadcast(testIntent)
            Log.d(TAG, "Test LocalBroadcast sent")
        }, 1000)
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
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

        binding.btnDisconnect.setOnClickListener {
            Log.d(TAG, "Disconnect button clicked")
            viewModel.disconnect()
        }

        // Настройка адаптера для списка устройств
        devicesAdapter = DevicesAdapter { device ->
            Log.d(TAG, "Device selected: ${device.name} (${device.address})")
            viewModel.connectToDevice(device)
            binding.tvStatus.text = "Connecting to ${device.name}..."
        }
        binding.recyclerDevices.layoutManager = LinearLayoutManager(this)
        binding.recyclerDevices.adapter = devicesAdapter

        // СОЗДАЕМ АДАПТЕР ДЛЯ МЕТРИК
        metricsAdapter = MetricsAdapter { metric ->
            Log.d(TAG, "Metric clicked: $metric")
        }
        binding.recyclerMetrics.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerMetrics.adapter = metricsAdapter  // РАСКОММЕНТИРУЙТЕ ЭТУ СТРОКУ

        // Изначально кнопки управления недоступны
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
            if (status.isNotEmpty()) {
                binding.tvStatus.text = status
            }
        }

        viewModel.devices.observe(this) { devices ->
            Log.d(TAG, "Devices LiveData updated: ${devices.size} devices")
            devicesAdapter.submitList(devices)
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

            // Скрываем или показываем список устройств в зависимости от состояния подключения
            if (isConnected) {
                binding.tvStatus.text = "Connected"
                // Скрываем список устройств
                binding.recyclerDevices.visibility = View.GONE
                // Показываем метрики (если они были скрыты)
                binding.recyclerMetrics.visibility = View.VISIBLE
                // Можно также скрыть кнопки сканирования
                binding.btnScan.visibility = View.GONE
                binding.btnStop.visibility = View.GONE
            } else {
                // Показываем список устройств
                binding.recyclerDevices.visibility = View.VISIBLE
                // Скрываем метрики, если они отображались
                binding.recyclerMetrics.visibility = View.GONE
                // Показываем кнопки сканирования
                binding.btnScan.visibility = View.VISIBLE
                binding.btnStop.visibility = View.VISIBLE
            }
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
            bindBLEService()
        }
    }

    private fun bindBLEService() {
        val intent = Intent(this, BLEService::class.java)
        startService(intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume - isServiceBound: $isServiceBound")

        // Перерегистрируем receiver при возобновлении
        registerReceiver()

        viewModel.isScanning.value?.let { isScanning ->
            val hasPerms = hasPermissions()
            binding.btnScan.isEnabled = !isScanning && hasPerms
            binding.btnStop.isEnabled = isScanning && hasPerms
        }

        if (hasPermissions() && !isServiceBound) {
            Log.d(TAG, "Has permissions but service not bound, calling checkBluetoothAndStart")
            checkBluetoothAndStart()
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause - unregistering receiver")
        try {
            localBroadcastManager.unregisterReceiver(activityReceiver)
        } catch (e: IllegalArgumentException) {
            Log.d(TAG, "Receiver was not registered")
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        if (isServiceBound) {
            unbindService(serviceConnection)
        }
        viewModel.cleanup()
    }
}