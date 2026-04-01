package org.pakicek.monoforecast.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.pakicek.monoforecast.data.local.entity.FileEntity
import org.pakicek.monoforecast.data.local.entity.LocationBlockEntity
import org.pakicek.monoforecast.data.local.entity.LogFrameEntity
import org.pakicek.monoforecast.data.local.entity.LogWithDetails
import org.pakicek.monoforecast.data.local.entity.SettingsBlockEntity
import org.pakicek.monoforecast.data.local.entity.WeatherBlockEntity

@Dao
interface LogsDao {

    @Insert
    suspend fun insertLog(log: LogFrameEntity): Long

    @Query("SELECT * FROM logs ORDER BY timestamp ASC")
    fun getAllLogs(): Flow<List<LogFrameEntity>>

    @Transaction
    @Query("SELECT * FROM logs ORDER BY timestamp ASC")
    fun getLogsWithDetailsFlow(): Flow<List<LogWithDetails>>

    @Transaction
    @Query(
        """
        SELECT * FROM logs 
        WHERE timestamp >= :start 
        AND (:end IS NULL OR timestamp <= :end)
        ORDER BY timestamp ASC
        """
    )
    fun getLogsByTimeRange(start: Long, end: Long?): Flow<List<LogWithDetails>>

    @Query("DELETE FROM logs")
    suspend fun clearLogsTable()

    @Insert
    suspend fun insertFile(file: FileEntity)

    @Query("SELECT * FROM files ORDER BY id DESC")
    fun getAllFilesFlow(): Flow<List<FileEntity>>

    @Query("SELECT * FROM files ORDER BY id DESC LIMIT 1")
    suspend fun getLastFile(): FileEntity?

    @Query("SELECT * FROM files WHERE id = :fileId LIMIT 1")
    suspend fun getFileById(fileId: Long): FileEntity?

    @Update
    suspend fun updateFile(file: FileEntity)

    @Query("DELETE FROM files")
    suspend fun clearFilesTable()

    @Insert
    suspend fun insertSettings(settings: SettingsBlockEntity)

    @Transaction
    suspend fun insertLogWithSettings(log: LogFrameEntity, settings: SettingsBlockEntity) {
        val logId = insertLog(log)
        settings.logId = logId
        insertSettings(settings)
    }

    @Query("SELECT * FROM settings")
    suspend fun getAllSettings(): List<SettingsBlockEntity>

    @Query("DELETE FROM settings")
    suspend fun clearSettingsTable()

    @Insert
    suspend fun insertLocationBlock(block: LocationBlockEntity)

    @Transaction
    suspend fun insertLocationLog(log: LogFrameEntity, location: LocationBlockEntity) {
        val id = insertLog(log)
        insertLocationBlock(location.copy(logId = id))
    }

    @Insert
    suspend fun insertWeatherBlock(block: WeatherBlockEntity)

    @Transaction
    suspend fun insertWeatherLog(log: LogFrameEntity, weather: WeatherBlockEntity) {
        val id = insertLog(log)
        insertWeatherBlock(weather.copy(logId = id))
    }

    @Transaction
    suspend fun clearAllData() {
        clearSettingsTable()
        clearLogsTable()
        clearFilesTable()
    }
}