package org.pakicek.monoforecast.presentation.settings

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.pakicek.monoforecast.MonoForecastApp
import org.pakicek.monoforecast.databinding.ActivitySettingsBinding
import org.pakicek.monoforecast.domain.model.settings.*

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    private val viewModel: SettingsViewModel by viewModels {
        val container = (application as MonoForecastApp).container
        SettingsViewModelFactory(container.settingsRepository, container.logsRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupInsets()
        binding.btnBack.setOnClickListener { finish() }
        setupSpinners()
    }

    private fun setupSpinners() {
        setupSpinner(binding.themeSpinner, AppTheme.entries, viewModel.getTheme(),
            labelMapper = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
        ) {
            viewModel.saveTheme(it)
            applyAppTheme(it)
        }

        setupSpinner(binding.apiSpinner, WeatherApi.entries, viewModel.getApi(),
            labelMapper = { api ->
                when(api) {
                    WeatherApi.NINJA_API -> "Ninja API"
                    WeatherApi.OPEN_METEO -> "Open-Meteo"
                    WeatherApi.MOCK -> "Mock (Random)"
                }
            }
        ) { viewModel.saveApi(it) }

        setupSpinner(binding.activitySpinner, UserActivity.entries, viewModel.getActivity(),
            labelMapper = { act ->
                when(act) {
                    UserActivity.BIKE -> "Bike"
                    UserActivity.MONO_WHEEL -> "Mono Wheel"
                    UserActivity.BICYCLE -> "Bicycle"
                }
            }
        ) { viewModel.saveActivity(it) }

        setupSpinner(binding.cacheSpinner, CacheDuration.entries, viewModel.getCacheDuration(),
            labelMapper = { dur ->
                when(dur) {
                    CacheDuration.ALWAYS_UPDATE -> "Always update"
                    CacheDuration.MIN_15 -> "15 minutes"
                    CacheDuration.HOUR_1 -> "1 hour"
                    CacheDuration.HOUR_3 -> "3 hours"
                }
            }
        ) { viewModel.saveCacheDuration(it) }

        setupSpinner(binding.gnssSpinner, GnssInterval.entries, viewModel.getGnssInterval(),
            labelMapper = { it.displayName }
        ) { viewModel.saveGnssInterval(it) }

        setupSpinner(binding.bleSpinner, BleMode.entries, viewModel.getBleMode(),
            labelMapper = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
        ) { viewModel.saveBleMode(it) }
    }

    private fun <T : Enum<T>> setupSpinner(
        spinner: Spinner,
        items: List<T>,
        selectedItem: T,
        labelMapper: (T) -> String,
        onItemSelected: (T) -> Unit
    ) {
        val displayItems = items.map { labelMapper(it) }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, displayItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(items.indexOf(selectedItem))

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (items[position] != selectedItem || spinner.tag == "init") onItemSelected(items[position])
                spinner.tag = "init"
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun applyAppTheme(theme: AppTheme) {
        AppCompatDelegate.setDefaultNightMode(when (theme) {
            AppTheme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            AppTheme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        })
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}