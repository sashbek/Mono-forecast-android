package org.pakicek.monoforecast

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.databinding.ActivityLogsBinding
import org.pakicek.monoforecast.domain.model.dao.LogsDb
import org.pakicek.monoforecast.domain.repositories.LogsRepository
import org.pakicek.monoforecast.domain.repositories.SettingsRepository
import org.pakicek.monoforecast.logic.factories.LogsViewModelFactory
import org.pakicek.monoforecast.logic.factories.SettingsViewModelFactory
import org.pakicek.monoforecast.logic.viewmodel.LogsViewModel
import org.pakicek.monoforecast.logic.viewmodel.SettingsViewModel

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

        setLoggingButton(viewModel)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnStartLogging.setOnClickListener {
            viewModel.toggleLogging()
            setLoggingButton(viewModel)
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setLoggingButton(viewModel: LogsViewModel) {
        val isLogging = viewModel.getLoggingStatus()
        binding.btnStartLogging.isSelected = isLogging
        binding.btnStartLogging.text = if (isLogging) "Stop Logging" else "Start Logging"
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        _viewModel = null
    }
}