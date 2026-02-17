package org.pakicek.monoforecast

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import org.pakicek.monoforecast.domain.model.RideDifficulty
import org.pakicek.monoforecast.databinding.ActivityMainBinding
import org.pakicek.monoforecast.domain.model.AppTheme
import org.pakicek.monoforecast.domain.repositories.SettingsRepository

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding must not be null")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupTheme()

        // Обработка кнопки настроек
        binding.btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Обработка 4 кнопок опций
        binding.forecastActivityButton.setOnClickListener {
            val intent = Intent(this, ForecastActivity::class.java)
            startActivity(intent)
        }

        binding.bluetoothActivityButton.setOnClickListener {
            Toast.makeText(this, "BLE Connect: not implemented", Toast.LENGTH_SHORT).show()
        }

        binding.locationActivityButton.setOnClickListener {
            Toast.makeText(this, "Location tracker: not implemented", Toast.LENGTH_SHORT).show()
        }

        binding.logsActivityButton.setOnClickListener {
            val intent = Intent(this, LogsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun formatDifficulty(diff: RideDifficulty): String {
        return when(diff) {
            is RideDifficulty.Easy -> "The weather is fine!"
            is RideDifficulty.Moderate -> "Warning: ${diff.warnings}"
            is RideDifficulty.Hard -> "Danger: ${diff.dangerReason}"
            is RideDifficulty.Extreme -> "Maybe you should stay home?"
        }
    }

    private fun setupTheme() {

        val settingsRepository = SettingsRepository(this)
        val theme = settingsRepository.getTheme()

        when (theme) {
            AppTheme.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            AppTheme.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}