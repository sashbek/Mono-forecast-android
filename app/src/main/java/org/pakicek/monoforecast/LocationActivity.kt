package org.pakicek.monoforecast

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.pakicek.monoforecast.databinding.ActivityLocationBinding

class LocationActivity : AppCompatActivity() {
    private var _binding: ActivityLocationBinding? = null
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding must not be null")

    private var isTracking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnStartTracking.setOnClickListener {
            isTracking = !isTracking
            binding.btnStartTracking.isSelected = isTracking

            binding.btnStartTracking.text = if (isTracking) "Stop Tracking" else "Start Tracking"
        }
    }
}