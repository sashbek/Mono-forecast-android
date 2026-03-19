package org.pakicek.monoforecast

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.databinding.ActivityLocationBinding
import org.pakicek.monoforecast.domain.repositories.SettingsRepository
import org.pakicek.monoforecast.logic.services.LocationTrackingService
import java.util.concurrent.TimeUnit

class LocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationBinding
    private lateinit var mapView: MapView
    private lateinit var settingsRepository: SettingsRepository
    private var mapObjects: MapObjectCollection? = null
    private var userMarker: PlacemarkMapObject? = null
    private val trackPoints = mutableListOf<Point>()
    private var timerJob: Job? = null

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == LocationTrackingService.ACTION_LOCATION_UPDATE) {
                val lat = intent.getDoubleExtra("lat", 0.0)
                val lon = intent.getDoubleExtra("lon", 0.0)
                updateMap(Point(lat, lon))
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.containsValue(true)) {
            startTrackingService()
        } else {
            Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(this)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settingsRepository = SettingsRepository(this)
        setupMap()
        setupButtons()
    }

    override fun onResume() {
        super.onResume()
        restoreUiState()
        val filter = IntentFilter(LocationTrackingService.ACTION_LOCATION_UPDATE)
        ContextCompat.registerReceiver(this, locationReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        stopTimer()
        unregisterReceiver(locationReceiver)
    }

    private fun setupMap() {
        mapView = binding.mapview
        mapObjects = mapView.mapWindow.map.mapObjects.addCollection()

        mapView.mapWindow.map.move(
            CameraPosition(Point(59.935493, 30.327392), 14.0f, 0.0f, 0.0f)
        )
    }

    private fun setupButtons() {
        binding.btnStartTracking.setOnClickListener {
            toggleTracking()
        }
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun restoreUiState() {
        val isTracking = settingsRepository.isTracking()
        updateButtonState(isTracking)

        if (isTracking) {
            startTimer()
        } else {
            binding.tvTimer.text = getString(R.string.zeroes_timer)
        }
    }

    private fun toggleTracking() {
        val isCurrentlyTracking = settingsRepository.isTracking()
        if (!isCurrentlyTracking) {
            checkPermissionsAndStart()
        } else {
            stopTrackingService()
        }
    }

    private fun checkPermissionsAndStart() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isEmpty()) {
            startTrackingService()
        } else {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }

    private fun startTrackingService() {
        val intent = Intent(this, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        updateButtonState(true)
        if (settingsRepository.getTrackingStartTime() == 0L) {
            settingsRepository.setTrackingStartTime(System.currentTimeMillis())
        }
        startTimer()
    }

    private fun stopTrackingService() {
        val intent = Intent(this, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_STOP
        }
        startService(intent)

        updateButtonState(false)
        stopTimer()
        binding.tvTimer.text = getString(R.string.zeroes_timer)
        trackPoints.clear()
        mapObjects?.clear()
        userMarker = null
    }

    private fun updateButtonState(isTracking: Boolean) {
        binding.btnStartTracking.isSelected = isTracking
        binding.btnStartTracking.text = getString(
            if (isTracking) R.string.stop_tracking_button_text else R.string.start_tracking_button_text
        )
    }

    @SuppressLint("DefaultLocale")
    private fun startTimer() {
        if (timerJob?.isActive == true) return

        timerJob = lifecycleScope.launch {
            while (isActive) {
                val startTime = settingsRepository.getTrackingStartTime()
                if (startTime > 0) {
                    val diff = System.currentTimeMillis() - startTime
                    val hours = TimeUnit.MILLISECONDS.toHours(diff)
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60
                    binding.tvTimer.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                }
                delay(1000)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun updateMap(point: Point) {
        trackPoints.add(point)
        mapObjects?.clear()

        if (trackPoints.size >= 2) {
            val polyline = Polyline(trackPoints)
            mapObjects?.addPolyline(polyline)?.apply {
                setStrokeColor(Color.BLUE)
                setStrokeWidth(5f)
            }
        }

        if (userMarker == null) {
            val imageProvider = ImageProvider.fromResource(this, android.R.drawable.ic_menu_mylocation)
            mapObjects!!.addPlacemark().apply {
                geometry = point
                setIcon(imageProvider)
            }
        } else {
            userMarker?.geometry = point
        }

        mapView.mapWindow.map.move(
            CameraPosition(point, 17.0f, 0.0f, 0.0f),
            com.yandex.mapkit.Animation(com.yandex.mapkit.Animation.Type.SMOOTH, 0.5f),
            null
        )
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}