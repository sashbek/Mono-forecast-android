package org.pakicek.monoforecast.presentation.location

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.PolylineMapObject
import com.yandex.runtime.image.ImageProvider
import org.pakicek.monoforecast.MonoForecastApp
import org.pakicek.monoforecast.R
import org.pakicek.monoforecast.data.features.GnssFeature
import org.pakicek.monoforecast.databinding.ActivityLocationBinding
import org.pakicek.monoforecast.presentation.services.AppNotificationManager
import org.pakicek.monoforecast.presentation.services.NotificationActionReceiver

class LocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationBinding

    private val viewModel: LocationViewModel by viewModels {
        LocationViewModelFactory((application as MonoForecastApp).container.settingsRepository)
    }

    private val points = mutableListOf<Point>()

    private var userMarker: PlacemarkMapObject? = null
    private var trackPolyline: PolylineMapObject? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                GnssFeature.ACTION_LOCATION_UPDATE -> {
                    val point = Point(
                        intent.getDoubleExtra("lat", 0.0),
                        intent.getDoubleExtra("lon", 0.0)
                    )
                    points.add(point)
                    updateMap(point)
                }
            }
        }
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.containsValue(true)) {
                startTrackingCommand()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(this)

        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        setupObservers()
    }

    override fun onResume() {
        super.onResume()

        viewModel.refreshState()

        val filter = IntentFilter().apply {
            addAction(GnssFeature.ACTION_LOCATION_UPDATE)
        }
        ContextCompat.registerReceiver(this, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)

        binding.mapview.onStart()
    }

    override fun onPause() {
        binding.mapview.onStop()
        unregisterReceiver(receiver)
        super.onPause()
    }

    override fun onStart() {
        MapKitFactory.getInstance().onStart()
        super.onStart()
    }

    override fun onStop() {
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnStartTracking.setOnClickListener {
            if (viewModel.isTracking.value == true) {
                stopTrackingCommand()
            } else {
                checkPermissionsAndStart()
            }
        }

        binding.btnClearTrack.setOnClickListener {
            clearMapData()
        }

        binding.btnCenter.setOnClickListener {
            if (points.isNotEmpty()) {
                val position = CameraPosition(points.last(), 17f, 0f, 0f)
                binding.mapview.mapWindow.map.move(
                    position,
                    Animation(Animation.Type.SMOOTH, 0.8f),
                    null
                )
            }
        }
    }

    private fun setupObservers() {
        viewModel.isTracking.observe(this) { isTracking ->
            binding.btnStartTracking.isSelected = isTracking
            binding.btnStartTracking.text = getString(
                if (isTracking) R.string.stop_tracking_button_text
                else R.string.start_tracking_button_text
            )
        }

        viewModel.timer.observe(this) { timer ->
            binding.tvTimer.text = timer
        }
    }

    private fun checkPermissionsAndStart() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val needRequest = permissions.any {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (needRequest) {
            permissionLauncher.launch(permissions.toTypedArray())
        } else {
            startTrackingCommand()
        }
    }

    private fun startTrackingCommand() {
        val intent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = AppNotificationManager.ACTION_TOGGLE_GNSS
        }
        sendBroadcast(intent)
        viewModel.startTracking()
    }

    private fun stopTrackingCommand() {
        val intent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = AppNotificationManager.ACTION_TOGGLE_GNSS
        }
        sendBroadcast(intent)
        viewModel.stopTracking()
    }

    private fun updateMap(point: Point) {
        val mapObjects = binding.mapview.mapWindow.map.mapObjects

        if (userMarker == null) {
            userMarker = mapObjects.addPlacemark(
                point,
                ImageProvider.fromResource(this, android.R.drawable.ic_menu_mylocation)
            )
        } else {
            userMarker?.geometry = point
        }

        if (points.size > 1) {
            if (trackPolyline == null) {
                trackPolyline = mapObjects.addPolyline(Polyline(points)).apply {
                    setStrokeColor(Color.BLUE)
                    setStrokeWidth(5f)
                }
            } else {
                trackPolyline?.geometry = Polyline(points)
            }
        }

        binding.mapview.mapWindow.map.move(
            CameraPosition(point, 17f, 0f, 0f)
        )
    }

    private fun clearMapData() {
        points.clear()
        binding.mapview.mapWindow.map.mapObjects.clear()
        userMarker = null
        trackPolyline = null
    }
}