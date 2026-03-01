package org.pakicek.monoforecast.domain.repositories

import org.pakicek.monoforecast.domain.model.dao.LogsDao
import org.pakicek.monoforecast.domain.model.dto.enums.LogType
import org.pakicek.monoforecast.domain.model.dto.logs.LogFrameEntity

class LogsRepository private constructor(private val dao: LogsDao) {
    suspend fun insertLog(type: LogType) {
        dao.insertLog(LogFrameEntity(type = type, timestamp = System.currentTimeMillis()))
    }

    suspend fun getAllLogs(): List<LogFrameEntity> {
        return dao.getAllLogs()
    }

    suspend fun getLogsByType(type: LogType): List<LogFrameEntity> {
        return dao.getLogsByType(type)
    }

    suspend fun clearLogs() {
        dao.clearLogs()
    }

    companion object {
        @Volatile
        private var INSTANCE: LogsRepository? = null

        fun getInstance(dao: LogsDao): LogsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LogsRepository(dao).also { INSTANCE = it }
            }
        }
    }
}