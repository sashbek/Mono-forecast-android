package org.pakicek.monoforecast

import android.graphics.Typeface
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import org.pakicek.monoforecast.domain.model.RideDifficulty
import org.pakicek.monoforecast.domain.repositories.ForecastRepository
import org.pakicek.monoforecast.logic.ForecastAnalyzer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = ForecastRepository()
        val analyzer = ForecastAnalyzer()
        val weather = repository.getCurrentWeather()
        val difficulty = analyzer.analyzeDifficulty(weather)

        val myTextView = TextView(this)

        myTextView.textSize = 20f
        myTextView.setPadding(50, 50, 50, 50)
        myTextView.typeface = Typeface.MONOSPACE

        val textToDisplay = """
            Mono Forecast
            
            Vehicle
            TODO
            
            Weather:
            Temperature: ${weather.tempC} C
            Wind: ${weather.windSpeedMs} m/s
            Rain: ${weather.rainMm} mm
            
            Result: ${formatDifficulty(difficulty)}
        """.trimIndent()
        myTextView.text = textToDisplay

        setContentView(myTextView)
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