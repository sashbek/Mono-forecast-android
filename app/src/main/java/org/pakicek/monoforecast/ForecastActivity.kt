package org.pakicek.monoforecast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.pakicek.monoforecast.databinding.ActivityForecastBinding
import org.pakicek.monoforecast.domain.model.RideDifficulty
import org.pakicek.monoforecast.domain.model.dto.WeatherResponseDto
import org.pakicek.monoforecast.domain.repositories.ForecastRepository
import org.pakicek.monoforecast.logic.factories.ForecastViewModelFactory
import org.pakicek.monoforecast.logic.services.WeatherSyncService
import org.pakicek.monoforecast.logic.viewmodel.ForecastViewModel

class ForecastActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForecastBinding
    private val viewModel: ForecastViewModel by viewModels {
        ForecastViewModelFactory(ForecastRepository(this))
    }

    private val weatherReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == WeatherSyncService.ACTION_WEATHER_UPDATED) {
                Toast.makeText(context, "Data has been updated!", Toast.LENGTH_SHORT).show()
                viewModel.refreshData()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForecastBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupInsets()
        setupObservers()
        setupListeners()
        registerWeatherReceiver()
    }

    private fun setupObservers() {
        viewModel.weatherState.observe(this) { state ->
            updateUI(state.weather, state.difficulty)
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnRefresh.setOnClickListener { startWeatherSync() }
    }

    private fun startWeatherSync() {
        Toast.makeText(this, "Syncing weather...", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, WeatherSyncService::class.java)
        startService(intent)
    }

    private fun updateUI(weather: WeatherResponseDto, difficulty: RideDifficulty) {
        with(binding) {
            tvTemp.text = getString(R.string.fmt_temp, weather.main.temp)
            tvWind.text = getString(R.string.fmt_wind, weather.wind.speed)
            tvHumidity.text = getString(R.string.fmt_humidity, weather.main.humidity)
            tvClouds.text = getString(R.string.fmt_clouds, weather.cloudPct)
        }
        updateStatusCard(difficulty)
    }

    private fun updateStatusCard(difficulty: RideDifficulty) {
        val (colorRes, titleRes, description) = when (difficulty) {
            is RideDifficulty.Easy -> Triple(
                R.color.status_easy,
                R.string.status_title_easy,
                getString(R.string.status_desc_easy)
            )
            is RideDifficulty.Moderate -> Triple(
                R.color.status_moderate,
                R.string.status_title_moderate,
                getString(R.string.status_desc_warnings, difficulty.warnings.joinToString(", "))
            )
            is RideDifficulty.Hard -> Triple(
                R.color.status_hard,
                R.string.status_title_hard,
                getString(R.string.status_desc_reason, difficulty.reason)
            )
            is RideDifficulty.Extreme -> Triple(
                R.color.status_extreme,
                R.string.status_title_extreme,
                getString(R.string.status_desc_reason, difficulty.reason)
            )
        }

        with(binding) {
            statusCard.setCardBackgroundColor(ContextCompat.getColor(this@ForecastActivity, colorRes))
            tvStatusTitle.setText(titleRes)
            tvStatusDescription.text = description
        }
    }

    private fun registerWeatherReceiver() {
        val filter = IntentFilter(WeatherSyncService.ACTION_WEATHER_UPDATED)
        ContextCompat.registerReceiver(this, weatherReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(weatherReceiver)
    }
}