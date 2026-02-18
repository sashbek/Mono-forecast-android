package org.pakicek.monoforecast.domain.model.dto.logs

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = LogFrameEntity::class,
            parentColumns = ["id"],
            childColumns = ["logId"],
            onDelete = CASCADE,
        )
    ]
)
data class WeatherBlockEntity(
    @PrimaryKey
    val logId: Long,

    val tempC: Double,
    val windSpeedMs: Double,
    val rainMm: Double,
    val visibilityMeters: Int
)
