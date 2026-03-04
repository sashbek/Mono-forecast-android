package org.pakicek.monoforecast

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.snackbar.Snackbar
import org.pakicek.monoforecast.databinding.ActivityMainBinding
import org.pakicek.monoforecast.domain.model.dto.enums.AppTheme
import org.pakicek.monoforecast.domain.repositories.SettingsRepository

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val settingsRepository by lazy { SettingsRepository(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        setupTheme()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupTheme() {
        val theme = settingsRepository.getTheme()
        val mode = when (theme) {
            AppTheme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            AppTheme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            AppTheme.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun setupClickListeners() {
        with(binding) {
            btnSettings.setOnClickListener { navigateTo(SettingsActivity::class.java) }
            forecastActivityButton.setOnClickListener { navigateTo(ForecastActivity::class.java) }
            logsActivityButton.setOnClickListener { navigateTo(LogsActivity::class.java) }
            locationActivityButton.setOnClickListener { navigateTo(LocationActivity::class.java) }

            bluetoothActivityButton.setOnClickListener {
                Snackbar.make(binding.root, "BLE Connect: Not implemented", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateTo(clazz: Class<*>) {
        startActivity(Intent(this, clazz))
    }
}