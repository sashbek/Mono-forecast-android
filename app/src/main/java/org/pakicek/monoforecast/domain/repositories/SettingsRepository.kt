package org.pakicek.monoforecast.domain.repositories

import android.content.Context
import org.pakicek.monoforecast.domain.model.AppTheme

class SettingsRepository(context: Context) {

    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun getTheme(): AppTheme {
        val stored = prefs.getString("theme_pref", "SYSTEM")
        val value = AppTheme.valueOf(stored ?: "SYSTEM")
        return value
    }

    fun saveTheme(theme: AppTheme) {
        val value = theme.name
        prefs.edit()
            .putString("theme_pref", value)
            .apply()
    }
}