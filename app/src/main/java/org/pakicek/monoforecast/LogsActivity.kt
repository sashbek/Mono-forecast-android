package org.pakicek.monoforecast

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.databinding.ActivityLogsBinding
import org.pakicek.monoforecast.domain.model.dao.LogsDb
import org.pakicek.monoforecast.domain.repositories.LogsRepository
import org.pakicek.monoforecast.domain.repositories.SettingsRepository
import org.pakicek.monoforecast.logic.factories.LogsViewModelFactory
import org.pakicek.monoforecast.logic.viewmodel.LogsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLogsBinding

    private val viewModel: LogsViewModel by viewModels {
        val db = LogsDb.getInstance(applicationContext)
        val logsRepo = LogsRepository.getInstance(db.logsDao())
        val settingsRepo = SettingsRepository(this)
        LogsViewModelFactory(logsRepo, settingsRepo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupInsets()
        setupUI()
        observeData()
    }

    private fun setupUI() {
        binding.tvLogs.movementMethod = ScrollingMovementMethod()

        binding.btnStartLogging.setOnClickListener {
            viewModel.toggleLogging()
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.btnCopy.setOnClickListener {
            Toast.makeText(this, "Log copy not implemented yet", Toast.LENGTH_SHORT).show()
        }

        binding.btnShare.setOnClickListener {
            Toast.makeText(this, "Log share not implemented yet", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeData() {
        viewModel.isLogging.observe(this) { isLogging ->
            val logging = isLogging == true
            binding.btnStartLogging.isSelected = logging
            binding.btnStartLogging.text = getString(
                if (logging) R.string.stop_logging_button_text else R.string.start_logging_button_text
            )
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getLogs().collect { logsList ->
                    displayLogs(logsList)
                }
            }
        }
    }

    private fun displayLogs(logs: List<org.pakicek.monoforecast.domain.model.dto.logs.LogFrameEntity>) {
        if (logs.isEmpty()) {
            binding.tvLogs.text = getString(R.string.logs_placeholder_text)
            return
        }

        val sb = StringBuilder()
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        logs.forEach { log ->
            val time = sdf.format(Date(log.timestamp))
            sb.append("[$time] ${log.type.name}\n")
        }

        binding.tvLogs.text = sb.toString()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}