package org.pakicek.monoforecast

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.content.edit

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

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
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val savedTheme = prefs.getString("theme_pref", "System")
        spinner.setSelection(options.indexOf(savedTheme))

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

                // Сохраняем выбор в SharedPreferences
                val prefs = getSharedPreferences("settings", MODE_PRIVATE)
                prefs.edit { putString("theme_pref", selected) }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Обработка кнопки возврата
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }
    }
}