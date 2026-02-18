package org.pakicek.monoforecast.logic.viewmodel

import androidx.lifecycle.ViewModel
import org.pakicek.monoforecast.domain.repositories.LogsRepository

class LogsViewModel(private val repository: LogsRepository) : ViewModel()