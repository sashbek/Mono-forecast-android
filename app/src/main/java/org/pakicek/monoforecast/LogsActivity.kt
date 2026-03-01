package org.pakicek.monoforecast

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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

class LogsActivity : AppCompatActivity() {
    private var _binding: ActivityLogsBinding? = null
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding must not be null")

    private var _viewModel: LogsViewModel? = null

    private val viewModel
        get() = _viewModel ?: throw IllegalStateException("ViewModel must not be null")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLogsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = LogsDb.getInstance(this)
        val repository = LogsRepository.getInstance(db.logsDao())
        val settingsRepository = SettingsRepository(this)

        val factory = LogsViewModelFactory(repository, settingsRepository)
        _viewModel = androidx.lifecycle.ViewModelProvider(this, factory)[LogsViewModel::class.java]

        viewModel.isLogging.observe(this) { isLogging ->

            val logging = isLogging == true
            binding.btnStartLogging.isSelected = logging
            binding.btnStartLogging.text =
                if (logging) "Stop Logging" else "Start Logging"
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getLogs().collect { logsList ->
                    binding.tvLogs.text = logsList.joinToString("\n") { "${it.id} ${it.type} ${it.timestamp}" }
                }
            }
        }

        binding.btnStartLogging.setOnClickListener {
            viewModel.toggleLogging()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}