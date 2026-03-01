package org.pakicek.monoforecast

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.pakicek.monoforecast.databinding.ActivitySettingsBinding
import org.pakicek.monoforecast.domain.model.dto.enums.AppTheme
import org.pakicek.monoforecast.domain.model.dto.enums.UserActivity
import org.pakicek.monoforecast.domain.model.dto.enums.WeatherApi
import org.pakicek.monoforecast.domain.repositories.SettingsRepository
import org.pakicek.monoforecast.logic.factories.SettingsViewModelFactory
import org.pakicek.monoforecast.logic.viewmodel.SettingsViewModel

class SettingsActivity : AppCompatActivity() {
    private var _binding: ActivitySettingsBinding? = null
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding must not be null")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupInsets()

        val repository = SettingsRepository(this)
        val factory = SettingsViewModelFactory(repository)
        val viewModel = androidx.lifecycle.ViewModelProvider(this, factory)[SettingsViewModel::class.java]

        setupThemeSpinner(viewModel)
        setupApiSpinner(viewModel)
        setupActivitySpinner(viewModel)

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupInsets() {
        // Делаем отступы для системных панелей для контента header
        val header : ConstraintLayout = findViewById(R.id.headerContentWrap)
        ViewCompat.setOnApplyWindowInsetsListener(header) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutout = insets.displayCutout

            val leftInset = maxOf(systemBars.left, cutout?.safeInsetLeft ?: 0)
            val rightInset = maxOf(systemBars.right, cutout?.safeInsetRight ?: 0)

            v.setPadding(
                leftInset,
                systemBars.top,
                rightInset,
                0
            )

            insets
        }

        // Делаем отступы для системных панелей для основного контента
        val content : ScrollView = findViewById(R.id.settingsWrap)
        ViewCompat.setOnApplyWindowInsetsListener(content) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutout = insets.displayCutout

            val leftInset = maxOf(systemBars.left, cutout?.safeInsetLeft ?: 0)
            val rightInset = maxOf(systemBars.right, cutout?.safeInsetRight ?: 0)

            v.setPadding(
                leftInset,
                0,
                rightInset,
                0
            )

            insets
        }
    }

    private fun setupThemeSpinner(viewModel: SettingsViewModel){
        // Задаем элементы выпадающего списка тем
        val options = listOf("System", "Light", "Dark")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            options
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.themeSpinner.adapter = adapter

        // Выставляем в выпадающий список актуальный элемент
        val savedTheme = viewModel.getTheme()
        when(savedTheme) {
            AppTheme.DARK -> binding.themeSpinner.setSelection(options.indexOf("Dark"))
            AppTheme.LIGHT -> binding.themeSpinner.setSelection(options.indexOf("Light"))
            else -> binding.themeSpinner.setSelection(options.indexOf("System"))
        }

        // Добавляем обработчик для выбора из выпадающего списка
        binding.themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selected = options[position]

                val mode = when (selected) {
                    "Light" -> AppCompatDelegate.MODE_NIGHT_NO
                    "Dark" -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }

                AppCompatDelegate.setDefaultNightMode(mode)

                // Сохраняем тему
                when(selected) {
                    "Light" -> viewModel.saveTheme(AppTheme.LIGHT)
                    "Dark" -> viewModel.saveTheme(AppTheme.DARK)
                    "System" -> viewModel.saveTheme(AppTheme.SYSTEM)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupApiSpinner(viewModel: SettingsViewModel){
        // Задаем элементы выпадающего списка тем
        val options = listOf("NinjaAPI")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            options
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.apiSpinner.adapter = adapter

        // Выставляем в выпадающий список актуальный элемент
        val api = viewModel.getApi()
        when(api) {
            WeatherApi.NINJA_API -> binding.apiSpinner.setSelection(options.indexOf("NinjaAPI"))
        }

        // Добавляем обработчик для выбора из выпадающего списка
        binding.apiSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selected = options[position]

                when(selected) {
                    "NinjaAPI" -> viewModel.saveApi(WeatherApi.NINJA_API)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupActivitySpinner(viewModel: SettingsViewModel){
        // Задаем элементы выпадающего списка тем
        val options = listOf("Bike", "Mono Wheel", "Bicycle")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            options
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.activitySpinner.adapter = adapter

        // Выставляем в выпадающий список актуальный элемент
        val activity = viewModel.getActivity()
        when(activity) {
            UserActivity.BIKE -> binding.activitySpinner.setSelection(options.indexOf("Bike"))
            UserActivity.MONO_WHEEL -> binding.activitySpinner.setSelection(options.indexOf("Mono Wheel"))
            else -> binding.activitySpinner.setSelection(options.indexOf("Bicycle"))
        }

        // Добавляем обработчик для выбора из выпадающего списка
        binding.activitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selected = options[position]

                when (selected) {
                    "Bike" -> viewModel.saveActivity(UserActivity.BIKE)
                    "Mono Wheel" -> viewModel.saveActivity(UserActivity.MONO_WHEEL)
                    "Bicycle" -> viewModel.saveActivity(UserActivity.BICYCLE)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}