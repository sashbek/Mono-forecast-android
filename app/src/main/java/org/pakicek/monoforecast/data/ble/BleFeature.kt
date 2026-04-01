package org.pakicek.monoforecast.data.ble

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.data.features.BackgroundFeature
import org.pakicek.monoforecast.data.repository.SettingsRepositoryImpl
import org.pakicek.monoforecast.domain.model.BleState
import org.pakicek.monoforecast.domain.model.ble.WheelDevice
import org.pakicek.monoforecast.domain.model.ble.WheelMetrics
import org.pakicek.monoforecast.domain.model.settings.BleMode
import kotlin.random.Random

class BleFeature(private val context: Context) : BackgroundFeature<BleState> {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _state = MutableStateFlow(BleState.DISCONNECTED)
    override val state: StateFlow<BleState> = _state.asStateFlow()

    private val _deviceFound = MutableSharedFlow<WheelDevice>(extraBufferCapacity = 16)
    val deviceFound = _deviceFound.asSharedFlow()

    private val _metricsUpdate = MutableSharedFlow<WheelMetrics>(extraBufferCapacity = 16)
    val metricsUpdate = _metricsUpdate.asSharedFlow()

    private val _error = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val error = _error.asSharedFlow()

    private val settingsRepository = SettingsRepositoryImpl(context)

    private var bleService: BleService? = null
    private var isServiceBound = false
    private var eventsJob: Job? = null
    private var pendingDevice: WheelDevice? = null
    private var mockMetricsJob: Job? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? BleService.LocalBinder ?: return
            bleService = binder.getService()
            isServiceBound = true

            observeEvents()

            pendingDevice?.let { device ->
                bleService?.connectToDevice(device)
                pendingDevice = null
            }

            syncState()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bleService = null
            isServiceBound = false
            _state.value = BleState.DISCONNECTED
        }
    }

    init {
        bindToBleService()
    }

    private fun bindToBleService() {
        val intent = Intent(context, BleService::class.java)
        context.startService(intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun observeEvents() {
        if (eventsJob != null) return

        eventsJob = scope.launch {
            BleEventBus.events.collect { event ->
                when (event) {
                    is BleEventBus.Event.DeviceFound -> _deviceFound.emit(event.device)
                    is BleEventBus.Event.MetricsUpdate -> _metricsUpdate.emit(event.metrics)
                    is BleEventBus.Event.Connected -> _state.value = BleState.CONNECTED
                    is BleEventBus.Event.Disconnected -> _state.value = BleState.DISCONNECTED
                    is BleEventBus.Event.ScanStarted -> _state.value = BleState.SCANNING
                    is BleEventBus.Event.ScanStopped -> {
                        if (_state.value == BleState.SCANNING) {
                            _state.value = BleState.DISCONNECTED
                        }
                    }
                    is BleEventBus.Event.Error -> _error.emit(event.message)
                }
            }
        }
    }

    private fun syncState() {
        val service = bleService ?: return
        _state.value = when {
            service.isConnected() -> BleState.CONNECTED
            service.isScanning() -> BleState.SCANNING
            else -> BleState.DISCONNECTED
        }
    }

    fun startScan() {
        if (settingsRepository.getBleMode() == BleMode.MOCK) {
            _state.value = BleState.SCANNING
            scope.launch {
                delay(500)
                _deviceFound.emit(
                    WheelDevice(
                        address = "MOCK:00:11:22:33",
                        name = "Mock EUC",
                        rssi = -42,
                        bondState = 0,
                        adapterType = "test",
                        bluetoothDevice = null
                    )
                )
                _state.value = BleState.DISCONNECTED
            }
            return
        }

        if (!isServiceBound || bleService == null) {
            scope.launch { _error.emit("BLE service not ready") }
            return
        }

        bleService?.startScan()
    }

    fun stopScan() {
        if (settingsRepository.getBleMode() == BleMode.MOCK) {
            _state.value = BleState.DISCONNECTED
            return
        }

        if (!isServiceBound || bleService == null) return
        bleService?.stopScan()
    }

    fun connectToDevice(device: WheelDevice) {
        if (settingsRepository.getBleMode() == BleMode.MOCK && device.address.startsWith("MOCK")) {
            _state.value = BleState.CONNECTING
            scope.launch {
                delay(700)
                _state.value = BleState.CONNECTED
                startMockMetrics()
            }
            return
        }

        if (!isServiceBound || bleService == null) {
            pendingDevice = device
            _state.value = BleState.CONNECTING
            return
        }

        _state.value = BleState.CONNECTING
        bleService?.connectToDevice(device)
    }

    fun disconnect() {
        stopMockMetrics()

        if (settingsRepository.getBleMode() == BleMode.MOCK) {
            _state.value = BleState.DISCONNECTED
            return
        }

        if (!isServiceBound || bleService == null) return
        bleService?.disconnect()
        pendingDevice = null
    }

    private fun startMockMetrics() {
        stopMockMetrics()

        mockMetricsJob = scope.launch {
            var speed = 0f
            var voltage = 84.0f
            var distance = 120.0f
            var battery = 95

            while (true) {
                speed = (speed + Random.nextFloat() * 6f - 3f).coerceIn(0f, 45f)
                voltage = (voltage - 0.01f).coerceAtLeast(67f)
                distance += speed / 3600f
                battery = (battery - if (Random.nextFloat() > 0.95f) 1 else 0).coerceAtLeast(10)

                _metricsUpdate.emit(
                    WheelMetrics(
                        speed = speed,
                        batteryLevel = battery,
                        voltage = voltage,
                        current = (speed / 4f) + Random.nextFloat(),
                        temperature = 35f + Random.nextFloat() * 10f,
                        distance = distance
                    )
                )

                delay(500)
            }
        }
    }

    private fun stopMockMetrics() {
        mockMetricsJob?.cancel()
        mockMetricsJob = null
    }

    fun isConnected(): Boolean = _state.value == BleState.CONNECTED

    fun isScanning(): Boolean = _state.value == BleState.SCANNING

    override suspend fun start() {
        if (_state.value == BleState.DISCONNECTED) {
            startScan()
        }
    }

    override suspend fun stop() {
        disconnect()
        stopScan()
    }

    fun cleanup() {
        eventsJob?.cancel()

        if (isServiceBound) {
            try {
                context.unbindService(serviceConnection)
                isServiceBound = false
            } catch (_: Exception) {
            }
        }

        bleService = null
        pendingDevice = null
        scope.cancel()
    }
}