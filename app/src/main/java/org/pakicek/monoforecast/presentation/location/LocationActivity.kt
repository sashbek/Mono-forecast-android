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
import com.yandex.runtime.image.ImageProvider
import org.pakicek.monoforecast.MonoForecastApp
import org.pakicek.monoforecast.R
import org.pakicek.monoforecast.databinding.ActivityLocationBinding

class LocationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLocationBinding
    private val viewModel: LocationViewModel by viewModels {
        LocationViewModelFactory((application as MonoForecastApp).container.settingsRepository)
    }
    private val points = mutableListOf<Point>()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, i: Intent?) {
            when (i?.action) {
                LocationTrackingService.ACTION_UPDATE -> {
                    val p = Point(i.getDoubleExtra("lat", 0.0), i.getDoubleExtra("lon", 0.0))
                    points.add(p)
                    updateMap(p)
                }
                LocationTrackingService.ACTION_STOPPED -> {
                    viewModel.stopTracking()
                }
            }
        }
    }

    private val permLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (it.containsValue(true)) startService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(this)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStartTracking.setOnClickListener {
            if (viewModel.isTracking.value == true) stopService() else checkPerms()
        }
        binding.btnBack.setOnClickListener { finish() }
        binding.btnClearTrack.setOnClickListener {
            points.clear()
            binding.mapview.mapWindow.map.mapObjects.clear()
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

        viewModel.isTracking.observe(this) { isTracking ->
            binding.btnStartTracking.isSelected = isTracking
            binding.btnStartTracking.text = getString(
                if (isTracking) R.string.stop_tracking_button_text
                else R.string.start_tracking_button_text
            )
        }
        viewModel.timer.observe(this) { binding.tvTimer.text = it }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshState()

        val filter = IntentFilter().apply {
            addAction(LocationTrackingService.ACTION_UPDATE)
            addAction(LocationTrackingService.ACTION_STOPPED)
        }
        ContextCompat.registerReceiver(this, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        binding.mapview.onStart()
    }

    override fun onPause() {
        binding.mapview.onStop()
        unregisterReceiver(receiver)
        super.onPause()
    }

    private fun checkPerms() {
        val p = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= 33) p.add(Manifest.permission.POST_NOTIFICATIONS)
        if (p.any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) permLauncher.launch(p.toTypedArray()) else startService()
    }

    private fun startService() {
        val intent = Intent(this, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START
        }
        startService(intent)
        viewModel.startTracking()
    }

    private fun stopService() {
        val intent = Intent(this, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_STOP
        }
        startService(intent)
        viewModel.stopTracking()
    }

    private fun updateMap(p: Point) {
        val map = binding.mapview.mapWindow.map
        map.mapObjects.clear()
        if (points.size > 1) map.mapObjects.addPolyline(Polyline(points)).apply { setStrokeColor(Color.BLUE) }
        map.mapObjects.addPlacemark(p, ImageProvider.fromResource(this, android.R.drawable.ic_menu_mylocation))
        map.move(CameraPosition(p, 17f, 0f, 0f))
    }

    override fun onStart() { MapKitFactory.getInstance().onStart(); super.onStart() }
    override fun onStop() { MapKitFactory.getInstance().onStop(); super.onStop() }
}