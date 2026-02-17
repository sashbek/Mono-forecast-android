package org.pakicek.monoforecast

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import org.pakicek.monoforecast.domain.model.RideDifficulty
import androidx.core.view.WindowInsetsControllerCompat
import org.pakicek.monoforecast.domain.model.AppTheme
import org.pakicek.monoforecast.domain.repositories.SettingsRepository

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupTheme()
        setContentView(R.layout.activity_main)

        // Обработка кнопки настроек
        val settingsButton = findViewById<ImageButton>(R.id.btnSettings)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Обработка 4 кнопок опций
        val forecastButton = findViewById<LinearLayout>(R.id.forecastActivityButton)
        val bluetoothButton = findViewById<LinearLayout>(R.id.bluetoothActivityButton)
        val locationButton = findViewById<LinearLayout>(R.id.locationActivityButton)
        val logsButton = findViewById<LinearLayout>(R.id.logsActivityButton)

        forecastButton.setOnClickListener {
            val intent = Intent(this, ForecastActivity::class.java)
            startActivity(intent)
        }

        bluetoothButton.setOnClickListener {
            Toast.makeText(this, "BLE Connect: not implemented", Toast.LENGTH_SHORT).show()
        }

        locationButton.setOnClickListener {
            Toast.makeText(this, "Location tracker: not implemented", Toast.LENGTH_SHORT).show()
        }

        logsButton.setOnClickListener {
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