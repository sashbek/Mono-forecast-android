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

        // Задаем элементы выпадающего списка
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
                    "System" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    else -> AppCompatDelegate.getDefaultNightMode()
                }

                AppCompatDelegate.setDefaultNightMode(mode)

                // Сохраняем тему
                viewModel.saveTheme(AppTheme.valueOf(selected.uppercase()))
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

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
}