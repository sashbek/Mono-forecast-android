package org.pakicek.monoforecast.presentation.logs

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.pakicek.monoforecast.MonoForecastApp
import org.pakicek.monoforecast.R
import org.pakicek.monoforecast.databinding.ActivityLogsBinding
import org.pakicek.monoforecast.presentation.logs.fragments.LogListFragment
import org.pakicek.monoforecast.presentation.utils.showSnackbar

class LogsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLogsBinding
    private val viewModel: LogsViewModel by viewModels {
        LogsViewModelFactory((application as MonoForecastApp).container.logsRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupInsets()
        binding.btnBack.setOnClickListener {
            if (supportFragmentManager.backStackEntryCount > 0) supportFragmentManager.popBackStack() else finish()
        }
        binding.btnClear.setOnClickListener {
            viewModel.clearLogs()
            binding.root.showSnackbar("Logs deleted")
        }

        supportFragmentManager.addOnBackStackChangedListener {
            binding.btnClear.visibility = if (supportFragmentManager.backStackEntryCount > 0) View.GONE else View.VISIBLE
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.logsFragmentContainer, LogListFragment()).commit()
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