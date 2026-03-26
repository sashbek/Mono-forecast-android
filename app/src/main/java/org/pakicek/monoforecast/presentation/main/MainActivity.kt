package org.pakicek.monoforecast.presentation.main

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import org.pakicek.monoforecast.MonoForecastApp
import org.pakicek.monoforecast.databinding.ActivityMainBinding
import org.pakicek.monoforecast.domain.model.dto.enums.AppTheme
import org.pakicek.monoforecast.presentation.ble.BluetoothActivity
import org.pakicek.monoforecast.presentation.forecast.ForecastActivity
import org.pakicek.monoforecast.presentation.location.LocationActivity
import org.pakicek.monoforecast.presentation.logs.LogsActivity
import org.pakicek.monoforecast.presentation.services.MainService
import org.pakicek.monoforecast.presentation.settings.SettingsActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val settingsRepo = (application as MonoForecastApp).container.settingsRepository
        setupTheme(settingsRepo.getTheme())

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()

        val intent = Intent(this, MainService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    private fun setupTheme(theme: AppTheme) {
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
            bluetoothActivityButton.setOnClickListener { navigateTo(BluetoothActivity::class.java) }
        }
    }

    private fun navigateTo(clazz: Class<*>) {
        startActivity(Intent(this, clazz))
    }
}