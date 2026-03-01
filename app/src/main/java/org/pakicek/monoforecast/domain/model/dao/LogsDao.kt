package org.pakicek.monoforecast.domain.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import org.pakicek.monoforecast.domain.model.dto.enums.LogType
import org.pakicek.monoforecast.domain.model.dto.logs.LogFrameEntity

@Dao
interface LogsDao {
    @Insert
    suspend fun insertLog(log: LogFrameEntity)

    @Query("SELECT * FROM logs ORDER BY timestamp DESC")
    suspend fun getAllLogs(): List<LogFrameEntity>

    @Query("SELECT * FROM logs WHERE type = :type ORDER BY timestamp DESC")
    suspend fun getLogsByType(type: LogType): List<LogFrameEntity>

    @Query("DELETE FROM logs")
    suspend fun clearLogs()
}