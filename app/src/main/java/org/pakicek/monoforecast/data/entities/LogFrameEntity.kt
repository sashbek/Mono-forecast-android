package org.pakicek.monoforecast.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.pakicek.monoforecast.domain.model.dto.enums.LogType

@Entity(tableName = "logs")
data class LogFrameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: LogType,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)