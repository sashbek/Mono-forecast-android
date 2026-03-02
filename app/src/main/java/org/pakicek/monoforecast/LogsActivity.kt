package org.pakicek.monoforecast

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.pakicek.monoforecast.databinding.ActivityLogsBinding
import org.pakicek.monoforecast.domain.model.dao.LogsDb
import org.pakicek.monoforecast.domain.repositories.LogsRepository
import org.pakicek.monoforecast.domain.repositories.SettingsRepository
import org.pakicek.monoforecast.fragments.LogListFragment
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

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.logsFragmentContainer.id, LogListFragment())
                .commit()
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