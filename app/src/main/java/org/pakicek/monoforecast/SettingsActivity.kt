package org.pakicek.monoforecast

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.content.edit
import org.pakicek.monoforecast.domain.model.AppTheme
import org.pakicek.monoforecast.domain.model.UserActivity
import org.pakicek.monoforecast.domain.model.WeatherApi
import org.pakicek.monoforecast.domain.repositories.SettingsRepository
import org.pakicek.monoforecast.logic.factories.SettingsViewModelFactory
import org.pakicek.monoforecast.logic.viewmodel.SettingsViewModel

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        setupInsets()

        val repo = SettingsRepository(this)
        val factory = SettingsViewModelFactory(repo)
        val viewModel = androidx.lifecycle.ViewModelProvider(this, factory)[SettingsViewModel::class.java]

        setupThemeSpinner(viewModel)
        setupApiSpinner(viewModel)
        setupActivitySpinner(viewModel)

        // Обработка кнопки возврата
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
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
        val spinner: Spinner = findViewById(R.id.themeSpinner)
        val options = listOf("System", "Light", "Dark")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            options
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Выставляем в выпадающий список актуальный элемент
        val savedTheme = viewModel.getTheme()
        when(savedTheme) {
            AppTheme.DARK -> spinner.setSelection(options.indexOf("Dark"))
            AppTheme.LIGHT -> spinner.setSelection(options.indexOf("Light"))
            else -> spinner.setSelection(options.indexOf("System"))
        }

        // Добавляем обработчик для выбора из выпадающего списка
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
        val spinner: Spinner = findViewById(R.id.apiSpinner)
        val options = listOf("NinjaAPI")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            options
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Выставляем в выпадающий список актуальный элемент
        val api = viewModel.getApi()
        when(api) {
            WeatherApi.NINJA_API -> spinner.setSelection(options.indexOf("NinjaAPI"))
        }

        // Добавляем обработчик для выбора из выпадающего списка
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
        val spinner: Spinner = findViewById(R.id.activitySpinner)
        val options = listOf("Bike", "Mono Wheel", "Bicycle")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            options
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Выставляем в выпадающий список актуальный элемент
        val activity = viewModel.getActivity()
        when(activity) {
            UserActivity.BIKE -> spinner.setSelection(options.indexOf("Bike"))
            UserActivity.MONO_WHEEL -> spinner.setSelection(options.indexOf("Mono Wheel"))
            else -> spinner.setSelection(options.indexOf("Bicycle"))
        }

        // Добавляем обработчик для выбора из выпадающего списка
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
}