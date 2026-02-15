package org.pakicek.monoforecast

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.pakicek.monoforecast.domain.model.RideDifficulty
import org.pakicek.monoforecast.domain.repositories.ForecastRepository
import org.pakicek.monoforecast.logic.ForecastAnalyzer

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Обработка кнопки настроек
        val settingsButton = findViewById<ImageButton>(R.id.btnSettings)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Обработка 4 кнопок опций
        val btnOption1 = findViewById<LinearLayout>(R.id.btnOption1)
        val btnOption2 = findViewById<LinearLayout>(R.id.btnOption2)
        val btnOption3 = findViewById<LinearLayout>(R.id.btnOption3)
        val btnOption4 = findViewById<LinearLayout>(R.id.btnOption4)

        btnOption1.setOnClickListener {
            Toast.makeText(this, "Выбрана опция 1", Toast.LENGTH_SHORT).show()
            // Здесь переход на другое Activity
            // val intent = Intent(this, Option1Activity::class.java)
            // startActivity(intent)
        }

        btnOption2.setOnClickListener {
            Toast.makeText(this, "Выбрана опция 2", Toast.LENGTH_SHORT).show()
        }

        btnOption3.setOnClickListener {
            Toast.makeText(this, "Выбрана опция 3", Toast.LENGTH_SHORT).show()
        }

        btnOption4.setOnClickListener {
            Toast.makeText(this, "Выбрана опция 4", Toast.LENGTH_SHORT).show()
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
}