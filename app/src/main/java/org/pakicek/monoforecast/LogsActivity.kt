package org.pakicek.monoforecast

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.pakicek.monoforecast.databinding.ActivityLogsBinding

class LogsActivity : AppCompatActivity() {
    private var _binding: ActivityLogsBinding? = null
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding must not be null")

    private var isLogging = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLogsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnStartLogging.setOnClickListener {
            isLogging = !isLogging
            binding.btnStartLogging.isSelected = isLogging
            binding.btnStartLogging.text = if (isLogging) "Stop Logging" else "Start Logging"
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}