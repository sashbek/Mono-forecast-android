package org.pakicek.monoforecast.presentation.forecast

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.pakicek.monoforecast.MonoForecastApp
import org.pakicek.monoforecast.R
import org.pakicek.monoforecast.databinding.ActivityForecastBinding
import org.pakicek.monoforecast.domain.model.RideDifficulty
import org.pakicek.monoforecast.domain.model.dto.WeatherResponseDto
import org.pakicek.monoforecast.presentation.utils.showSnackbar

class ForecastActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForecastBinding

    private val viewModel: ForecastViewModel by viewModels {
        val app = application as MonoForecastApp
        ForecastViewModelFactory(app.container.forecastRepository)
    }

    private val weatherReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == WeatherSyncService.ACTION_WEATHER_UPDATED) {
                handleWeatherUpdate(intent)
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.containsValue(true)) {
                startWeatherSync()
            } else {
                binding.root.showSnackbar("Location permission needed", R.color.status_hard, android.R.color.white)
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
        viewModel.refreshData()
    }

    private fun setupObservers() {
        viewModel.weatherState.observe(this) { state ->
            updateUI(state.weather)
            updateStatusCard(state.difficulty)
            binding.weatherView.setWeatherCondition(state.condition)
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnRefresh.setOnClickListener { checkPermissionsAndStart() }
    }

    private fun updateUI(weather: WeatherResponseDto) {
        with(binding) {
            tvTemp.text = getString(R.string.fmt_temp, weather.main.temp)
            tvWind.text = getString(R.string.fmt_wind, weather.wind.speed)
            tvHumidity.text = getString(R.string.fmt_humidity, weather.main.humidity)
            tvClouds.text = getString(R.string.fmt_clouds, weather.cloudPct)
        }
    }

    private fun updateStatusCard(difficulty: RideDifficulty) {
        val (colorRes, titleRes, description) = when (difficulty) {
            is RideDifficulty.Easy -> Triple(R.color.status_easy, R.string.status_title_easy, getString(R.string.status_desc_easy))
            is RideDifficulty.Moderate -> Triple(R.color.status_moderate, R.string.status_title_moderate, getString(R.string.status_desc_warnings, difficulty.warnings.joinToString(", ")))
            is RideDifficulty.Hard -> Triple(R.color.status_hard, R.string.status_title_hard, getString(R.string.status_desc_reason, difficulty.reason))
            is RideDifficulty.Extreme -> Triple(R.color.status_extreme, R.string.status_title_extreme, getString(R.string.status_desc_reason, difficulty.reason))
        }

        with(binding) {
            statusCard.setCardBackgroundColor(ContextCompat.getColor(this@ForecastActivity, colorRes))
            tvStatusTitle.setText(titleRes)
            tvStatusDescription.text = description
        }
    }

    private fun checkPermissionsAndStart() {
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        val hasPermission = permissions.any { ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
        if (!hasPermission) requestPermissionLauncher.launch(permissions) else startWeatherSync()
    }

    private fun startWeatherSync() {
        binding.root.showSnackbar("Syncing weather...")
        startService(Intent(this, WeatherSyncService::class.java))
    }

    private fun handleWeatherUpdate(intent: Intent) {
        if (intent.getBooleanExtra(WeatherSyncService.EXTRA_IS_SUCCESS, false)) {
            viewModel.refreshData()
            binding.root.showSnackbar("Weather updated!", R.color.status_easy, android.R.color.white)
        } else {
            val msg = intent.getStringExtra(WeatherSyncService.EXTRA_ERROR_MESSAGE) ?: "Unknown error"
            binding.root.showSnackbar("Update failed: $msg", R.color.status_hard, android.R.color.white)
        }
    }

    private fun registerWeatherReceiver() {
        ContextCompat.registerReceiver(this, weatherReceiver, IntentFilter(WeatherSyncService.ACTION_WEATHER_UPDATED), ContextCompat.RECEIVER_NOT_EXPORTED)
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