package org.pakicek.monoforecast

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
import org.pakicek.monoforecast.databinding.ActivitySettingsBinding
import org.pakicek.monoforecast.domain.model.dto.enums.AppTheme
import org.pakicek.monoforecast.domain.model.dto.enums.CacheDuration
import org.pakicek.monoforecast.domain.model.dto.enums.UserActivity
import org.pakicek.monoforecast.domain.model.dto.enums.WeatherApi
import org.pakicek.monoforecast.domain.repositories.SettingsRepository
import org.pakicek.monoforecast.logic.factories.SettingsViewModelFactory
import org.pakicek.monoforecast.logic.viewmodel.SettingsViewModel

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(SettingsRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupInsets()
        setupSpinners()

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupSpinners() {
        setupThemeSpinner()
        setupApiSpinner()
        setupActivitySpinner()
        setupCacheSpinner()
    }

    private fun setupThemeSpinner() {
        val items = listOf(AppTheme.SYSTEM, AppTheme.LIGHT, AppTheme.DARK)
        val displayNames = listOf("System", "Light", "Dark")

        val current = viewModel.getTheme()
        val selectedIndex = items.indexOf(current).takeIf { it >= 0 } ?: 0

        setupSpinner(binding.themeSpinner, displayNames, selectedIndex) { index ->
            val selectedTheme = items[index]
            if (selectedTheme != viewModel.getTheme()) {
                viewModel.saveTheme(selectedTheme)
                applyAppTheme(selectedTheme)
            }
        }
    }

    private fun setupApiSpinner() {
        val items = listOf(WeatherApi.NINJA_API, WeatherApi.OPEN_METEO, WeatherApi.MOCK)
        val displayNames = listOf("NinjaAPI", "Open-Meteo", "Mock (Random)")

        val current = viewModel.getApi()
        val selectedIndex = items.indexOf(current).takeIf { it >= 0 } ?: 0

        setupSpinner(binding.apiSpinner, displayNames, selectedIndex) { index ->
            val selectedApi = items[index]
            if (selectedApi != viewModel.getApi()) {
                viewModel.saveApi(selectedApi)
            }
        }
    }

    private fun setupActivitySpinner() {
        val items = UserActivity.entries.toList()
        val displayNames = items.map { it.name.replace("_", " ").lowercase().capitalizeWords() }

        val current = viewModel.getActivity()
        val selectedIndex = items.indexOf(current).takeIf { it >= 0 } ?: 0

        setupSpinner(binding.activitySpinner, displayNames, selectedIndex) { index ->
            val selectedActivity = items[index]
            if (selectedActivity != viewModel.getActivity()) {
                viewModel.saveActivity(selectedActivity)
            }
        }
    }

    private fun setupCacheSpinner() {
        val items = CacheDuration.entries
        val displayNames = listOf("Always update", "15 minutes", "1 hour", "3 hours")

        val current = viewModel.getCacheDuration()
        val selectedIndex = items.indexOf(current).takeIf { it >= 0 } ?: 1

        setupSpinner(binding.cacheSpinner, displayNames, selectedIndex) { index ->
            val selectedCache = items[index]
            if (selectedCache != viewModel.getCacheDuration()) {
                viewModel.saveCacheDuration(selectedCache)
            }
        }
    }

    private fun setupSpinner(
        spinner: Spinner,
        items: List<String>,
        selectedIndex: Int,
        onSelect: (Int) -> Unit
    ) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(selectedIndex)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                onSelect(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun applyAppTheme(theme: AppTheme) {
        val mode = when (theme) {
            AppTheme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            AppTheme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") {
        it.replaceFirstChar { char -> char.uppercase() }
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.headerContentWrap) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutout = insets.displayCutout

            val topInset = systemBars.top
            val leftInset = maxOf(systemBars.left, cutout?.safeInsetLeft ?: 0)
            val rightInset = maxOf(systemBars.right, cutout?.safeInsetRight ?: 0)

            v.setPadding(leftInset, topInset, rightInset, 0)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.settingsWrap) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutout = insets.displayCutout

            val leftInset = maxOf(systemBars.left, cutout?.safeInsetLeft ?: 0)
            val rightInset = maxOf(systemBars.right, cutout?.safeInsetRight ?: 0)
            val bottomInset = systemBars.bottom

            v.setPadding(leftInset, 0, rightInset, bottomInset)
            insets
        }
    }
}