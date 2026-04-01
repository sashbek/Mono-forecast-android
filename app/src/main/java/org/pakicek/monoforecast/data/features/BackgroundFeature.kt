package org.pakicek.monoforecast.data.features

import kotlinx.coroutines.flow.StateFlow

interface BackgroundFeature<T> {
    val state: StateFlow<T>
    suspend fun start()
    suspend fun stop()
}