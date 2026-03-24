package org.pakicek.monoforecast.domain.repository

import kotlinx.coroutines.flow.Flow
import org.pakicek.monoforecast.data.entities.FileEntity
import org.pakicek.monoforecast.data.entities.LogWithDetails

interface ILogsRepository {
    suspend fun isLoggingActive(): Boolean
    suspend fun startNewFile()
    suspend fun endLastFile()

    fun getAllFiles(): Flow<List<FileEntity>>

    suspend fun getLogsForSession(fileId: Long): Flow<List<LogWithDetails>>

    suspend fun clearAll()
    suspend fun insertSetting(setting: String, value: String)
    suspend fun saveLocationLog(lat: Double, lon: Double)
}