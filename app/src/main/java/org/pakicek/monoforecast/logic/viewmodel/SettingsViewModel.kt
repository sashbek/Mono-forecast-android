package org.pakicek.monoforecast.logic.viewmodel

import androidx.lifecycle.ViewModel
import org.pakicek.monoforecast.domain.model.AppTheme
import org.pakicek.monoforecast.domain.model.UserActivity
import org.pakicek.monoforecast.domain.model.WeatherApi
import org.pakicek.monoforecast.domain.repositories.SettingsRepository

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    fun getTheme(): AppTheme = repository.getTheme()

    fun saveTheme(theme: AppTheme) {
        repository.saveTheme(theme)
    }

    fun getApi(): WeatherApi = repository.getApi()

    fun saveApi(api: WeatherApi) {
        repository.saveApi(api)
    }

    fun getActivity(): UserActivity = repository.getActivity()

    fun saveActivity(activity: UserActivity) {
        repository.saveActivity(activity)
    }
}
