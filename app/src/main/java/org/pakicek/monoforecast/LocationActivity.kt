package org.pakicek.monoforecast

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import org.pakicek.monoforecast.databinding.ActivityLocationBinding

class LocationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLocationBinding
    private lateinit var mapView: MapView

    private var isTracking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MapKitFactory.initialize(this)
        mapView = binding.mapview

        mapView.mapWindow.map.move(
            CameraPosition(
                Point(60.0, 30.3),
                /* zoom = */ 10.0f,
                /* azimuth = */ 0.0f,
                /* tilt = */ 0.0f
            )
        )

        binding.btnStartTracking.setOnClickListener {
            toggleTracking()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
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

    private fun toggleTracking() {
        isTracking = !isTracking
        binding.btnStartTracking.isSelected = isTracking

        val textRes =
            if (isTracking) R.string.stop_tracking_button_text else R.string.start_tracking_button_text
        binding.btnStartTracking.setText(textRes)
    }
}