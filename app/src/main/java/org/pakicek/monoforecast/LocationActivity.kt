package org.pakicek.monoforecast

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
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
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import com.yandex.mapkit.Animation
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.databinding.ActivityLocationBinding
import org.pakicek.monoforecast.domain.api.gnss.providers.GNSSProvider

class LocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationBinding
    private lateinit var mapView: MapView
    private lateinit var gnssProvider: GNSSProvider

    private var mapObjects: MapObjectCollection? = null
    private var userMarker: PlacemarkMapObject? = null
    private val trackPoints = mutableListOf<Point>()
    private var isTracking = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted && coarseLocationGranted) {
            startLocationUpdates()
        } else {
            Toast.makeText(this, "Location service: permission denied", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация Яндекс Карт
        MapKitFactory.initialize(this)
        mapView = binding.mapview
        mapObjects = mapView.mapWindow.map.mapObjects.addCollection()

        // Инициализация GNSS провайдера
        gnssProvider = GNSSProvider(this)

        // Наблюдаем за изменениями местоположения
        observeLocationUpdates()

        // Проверяем разрешения и запускаем
        checkPermissionsAndStart()

        setupButtons()
    }

    private fun setupButtons() {
        binding.btnStartTracking.setOnClickListener {
            toggleTracking()
        }

        binding.btnBack.setOnClickListener {
            stopLocationUpdates()
            finish()
        }

        // Добавляем обработчики для новых кнопок
        binding.btnCenter.setOnClickListener {
            centerOnCurrentLocation()
        }

        binding.btnClearTrack.setOnClickListener {
            clearTrack()
        }
    }

    private fun checkPermissionsAndStart() {
        when {
            checkPermissions() -> {
                startLocationUpdates()
            }
            else -> {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun observeLocationUpdates() {
        lifecycleScope.launch {
            gnssProvider.locationState.collect { state ->
                when (state) {
                    is GNSSProvider.LocationState.Available -> {
                        updateUserLocation(state)
                        if (isTracking) {
                            addTrackPoint(Point(state.latitude, state.longitude))
                        }
                    }
                    GNSSProvider.LocationState.Disabled -> {
                        showGpsDisabledMessage()
                    }
                    GNSSProvider.LocationState.PermissionDenied -> {
                        showPermissionDeniedMessage()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (gnssProvider.isGpsEnabled()) {
            gnssProvider.startLocationUpdates()
        } else {
            showGpsDisabledMessage()
        }
    }

    private fun stopLocationUpdates() {
        gnssProvider.stopLocationUpdates()
    }

    private fun updateUserLocation(location: GNSSProvider.LocationState.Available) {
        val point = Point(location.latitude, location.longitude)

        // Создаем или обновляем маркер пользователя
        if (userMarker == null) {
            userMarker = mapObjects?.addPlacemark(
                point,
                ImageProvider.fromResource(this, android.R.drawable.ic_menu_mylocation)
            )
        } else {
            userMarker?.geometry = point
        }

        // Если это первое местоположение, центрируем карту
        if (trackPoints.isEmpty()) {
            centerOnLocation(point)
        }
    }

    private fun addTrackPoint(point: Point) {
        trackPoints.add(point)
        drawTrack()
    }

    private fun drawTrack() {
        if (trackPoints.size < 2) return

        // Удаляем старую полилинию и маркер
        mapObjects?.clear()

        // Добавляем маркер пользователя обратно
        trackPoints.lastOrNull()?.let { lastPoint ->
            userMarker = mapObjects?.addPlacemark(
                lastPoint,
                ImageProvider.fromResource(this, android.R.drawable.ic_menu_mylocation)
            )
        }

        // Создаем новую полилинию из всех точек
        val polyline = Polyline(trackPoints)
        mapObjects?.addPolyline(polyline)?.apply {
            setStrokeColor(Color.BLUE)
            setStrokeWidth(5f)
        }
    }

    private fun clearTrack() {
        trackPoints.clear()
        mapObjects?.clear()
        userMarker = null

        // Возвращаем маркер на текущую позицию
        val currentState = gnssProvider.locationState.value
        if (currentState is GNSSProvider.LocationState.Available) {
            updateUserLocation(currentState)
        }

        Toast.makeText(this, "Track cleared", Toast.LENGTH_SHORT).show()
    }

    private fun centerOnCurrentLocation() {
        val currentState = gnssProvider.locationState.value
        if (currentState is GNSSProvider.LocationState.Available) {
            centerOnLocation(Point(currentState.latitude, currentState.longitude))
        } else {
            Toast.makeText(this, "Found location", Toast.LENGTH_SHORT).show()
        }
    }

    private fun centerOnLocation(point: Point) {
        mapView.mapWindow.map.move(
            CameraPosition(
                point,
                15.0f, // zoom
                0.0f,  // azimuth
                0.0f   // tilt
            ),
            com.yandex.mapkit.Animation(com.yandex.mapkit.Animation.Type.SMOOTH, 1f),
            null
        )
    }

    private fun showGpsDisabledMessage() {
        Toast.makeText(this, "GPS disabled. Please turn on the GPS", Toast.LENGTH_LONG).show()
    }

    private fun showPermissionDeniedMessage() {
        Toast.makeText(this, "Permission to use location service denied", Toast.LENGTH_LONG).show()
    }

    private fun toggleTracking() {
        isTracking = !isTracking
        binding.btnStartTracking.isSelected = isTracking

        val text = if (isTracking) {
            "Stop tracking"
        } else {
            "Tracking started"
        }
        binding.btnStartTracking.text = text

        if (isTracking) {
            // Очищаем трек перед началом новой записи
            clearTrack()
            Toast.makeText(this, "Tracking started", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Tracking stopped", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        if (checkPermissions()) {
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }
}