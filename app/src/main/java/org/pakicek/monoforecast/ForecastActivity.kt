package org.pakicek.monoforecast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import org.pakicek.monoforecast.databinding.ActivityForecastBinding
import org.pakicek.monoforecast.domain.model.RideDifficulty
import org.pakicek.monoforecast.domain.model.dto.WeatherResponseDto
import org.pakicek.monoforecast.domain.repositories.ForecastRepository
import org.pakicek.monoforecast.logic.factories.ForecastViewModelFactory
import org.pakicek.monoforecast.logic.services.WeatherSyncService
import org.pakicek.monoforecast.logic.viewmodel.ForecastViewModel

class ForecastActivity : AppCompatActivity() {
    private var _binding: ActivityForecastBinding? = null
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding must not be null")

    private lateinit var viewModel: ForecastViewModel

    private val weatherReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == WeatherSyncService.ACTION_WEATHER_UPDATED) {
                Toast.makeText(context, "Data updated from Service!", Toast.LENGTH_SHORT).show()
                viewModel.refreshData()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityForecastBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupInsets()
        enableEdgeToEdge()

        val repo = ForecastRepository(this)
        val factory = ForecastViewModelFactory(repo)
        val viewModel = ViewModelProvider(this, factory)[ForecastViewModel::class.java]

        viewModel.weatherState.observe(this) { state ->
            updateUI(state.weather, state.difficulty)
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnRefresh.setOnClickListener {
            startWeatherSync()
        }

        registerWeatherReceiver()
    }

    private fun startWeatherSync() {
        Toast.makeText(this, "Syncing weather...", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, WeatherSyncService::class.java)
        startService(intent)
    }

    private fun updateUI(weather: WeatherResponseDto, difficulty: RideDifficulty) {
        binding.tvTemp.text = getString(R.string.fmt_temp, weather.main.temp)
        binding.tvWind.text = getString(R.string.fmt_wind, weather.wind.speed)
        binding.tvRain.text = getString(R.string.fmt_rain, weather.main.rain)

        updateStatusCard(difficulty)
    }

    private fun updateStatusCard(difficulty: RideDifficulty) {
        val colorRes: Int
        val titleRes: Int
        val description: String

        when (difficulty) {
            is RideDifficulty.Easy -> {
                colorRes = R.color.status_easy
                titleRes = R.string.status_title_easy
                description = getString(R.string.status_desc_easy)
            }
            is RideDifficulty.Moderate -> {
                colorRes = R.color.status_moderate
                titleRes = R.string.status_title_moderate
                val warningsText = difficulty.warnings.joinToString(", ")
                description = getString(R.string.status_desc_warnings, warningsText)
            }
            is RideDifficulty.Hard -> {
                colorRes = R.color.status_hard
                titleRes = R.string.status_title_hard
                description = getString(R.string.status_desc_reason, difficulty.dangerReason)
            }
            is RideDifficulty.Extreme -> {
                colorRes = R.color.status_extreme
                titleRes = R.string.status_title_extreme
                description = getString(R.string.status_desc_extreme)
            }
        }

        binding.statusCard.setCardBackgroundColor(ContextCompat.getColor(this, colorRes))
        binding.tvStatusTitle.setText(titleRes)
        binding.tvStatusDescription.text = description
    }

    private fun registerWeatherReceiver() {
        val filter = IntentFilter(WeatherSyncService.ACTION_WEATHER_UPDATED)
        ContextCompat.registerReceiver(
            this,
            weatherReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
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
        _binding = null
    }
}