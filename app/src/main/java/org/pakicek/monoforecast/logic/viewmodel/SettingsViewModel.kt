package org.pakicek.monoforecast.logic.viewmodel

import androidx.lifecycle.ViewModel
import org.pakicek.monoforecast.domain.model.dto.enums.AppTheme
import org.pakicek.monoforecast.domain.model.dto.enums.CacheDuration
import org.pakicek.monoforecast.domain.model.dto.enums.UserActivity
import org.pakicek.monoforecast.domain.model.dto.enums.WeatherApi
import org.pakicek.monoforecast.domain.repositories.SettingsRepository

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

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

    fun getCacheDuration(): CacheDuration = repository.getCacheDuration()

    fun saveCacheDuration(duration: CacheDuration) {
        repository.saveCacheDuration(duration)
    }
}