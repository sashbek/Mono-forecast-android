package org.pakicek.monoforecast.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.pakicek.monoforecast.data.entities.FileEntity
import org.pakicek.monoforecast.data.entities.LocationBlockEntity
import org.pakicek.monoforecast.data.entities.LogFrameEntity
import org.pakicek.monoforecast.data.entities.LogWithDetails
import org.pakicek.monoforecast.data.entities.SettingsBlockEntity
import org.pakicek.monoforecast.data.entities.WeatherBlockEntity

@Dao
interface LogsDao {
    @Insert
    suspend fun insertLog(log: LogFrameEntity): Long

    @Insert
    suspend fun insertLocationBlock(block: LocationBlockEntity)

    @Insert
    suspend fun insertWeatherBlock(block: WeatherBlockEntity)

    @Insert
    suspend fun insertSettings(settings: SettingsBlockEntity)

    @Insert
    suspend fun insertFile(file: FileEntity)

    @Update
    suspend fun updateFile(file: FileEntity)

    @Query("SELECT * FROM files ORDER BY id DESC LIMIT 1")
    suspend fun getLastFile(): FileEntity?

    @Query("SELECT * FROM files ORDER BY id DESC")
    fun getAllFilesFlow(): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE id = :fileId LIMIT 1")
    suspend fun getFileById(fileId: Long): FileEntity?

    @Transaction
    @Query("""
        SELECT * FROM logs 
        WHERE timestamp >= :start 
        AND (:end IS NULL OR timestamp <= :end) 
        ORDER BY timestamp ASC
    """)
    fun getLogsByTimeRange(start: Long, end: Long?): Flow<List<LogWithDetails>>

    @Transaction
    suspend fun insertLocationLog(
        log: LogFrameEntity,
        location: LocationBlockEntity
    ) {
        val id = insertLog(log)
        val locationWithId = location.copy(logId = id)
        insertLocationBlock(locationWithId)
    }

    @Transaction
    suspend fun insertWeatherLog(log: LogFrameEntity, weather: WeatherBlockEntity) {
        val id = insertLog(log)
        val blockWithId = weather.copy(logId = id)
        insertWeatherBlock(blockWithId)
    }

    @Transaction
    suspend fun insertLogWithSettings(log: LogFrameEntity, settings: SettingsBlockEntity) {
        val id = insertLog(log)
        settings.logId = id
        insertSettings(settings)
    }

    @Transaction
    suspend fun clearAllData() {
        clearSettingsTable()
        clearLogsTable()
        clearFilesTable()
    }

    @Query("DELETE FROM logs")
    suspend fun clearLogsTable()

    @Query("DELETE FROM files")
    suspend fun clearFilesTable()

    @Query("DELETE FROM settings")
    suspend fun clearSettingsTable()
}