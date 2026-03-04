package org.pakicek.monoforecast

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.pakicek.monoforecast.databinding.ActivityLocationBinding

class LocationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLocationBinding

    private var isTracking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStartTracking.setOnClickListener {
            toggleTracking()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun toggleTracking() {
        isTracking = !isTracking
        binding.btnStartTracking.isSelected = isTracking

        val textRes = if (isTracking) R.string.stop_tracking_button_text else R.string.start_tracking_button_text
        binding.btnStartTracking.setText(textRes)
    }
}