package org.pakicek.monoforecast

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.pakicek.monoforecast.databinding.ActivityLogsBinding
import org.pakicek.monoforecast.domain.repositories.LogsRepository
import org.pakicek.monoforecast.domain.repositories.SettingsRepository
import org.pakicek.monoforecast.fragments.LogListFragment
import org.pakicek.monoforecast.logic.factories.LogsViewModelFactory
import org.pakicek.monoforecast.logic.viewmodel.LogsViewModel
import org.pakicek.monoforecast.utils.showSnackbar

class LogsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLogsBinding

    private val viewModel: LogsViewModel by viewModels {
        val logsRepo = LogsRepository(this)
        val settingsRepo = SettingsRepository(this)
        LogsViewModelFactory(logsRepo, settingsRepo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupInsets()
        setupListeners()
        setupFragmentNavigation()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.logsFragmentContainer, LogListFragment())
                .commit()
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
            } else {
                finish()
            }
        }

        binding.btnClear.setOnClickListener {
            viewModel.clearLogs()
            binding.root.showSnackbar("All logs have been deleted")
        }
    }

    private fun setupFragmentNavigation() {
        supportFragmentManager.addOnBackStackChangedListener {
            val isDetailsOpen = supportFragmentManager.backStackEntryCount > 0
            binding.btnClear.visibility = if (isDetailsOpen) View.GONE else View.VISIBLE
        }
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}