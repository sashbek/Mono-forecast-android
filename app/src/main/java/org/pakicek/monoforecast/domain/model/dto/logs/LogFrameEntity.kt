package org.pakicek.monoforecast.domain.model.dto.logs

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.pakicek.monoforecast.domain.model.dto.enums.LogType

@Entity(tableName = "logs")
data class LogFrameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val timestamp : Long = System.currentTimeMillis(),

    val type : LogType,
)
