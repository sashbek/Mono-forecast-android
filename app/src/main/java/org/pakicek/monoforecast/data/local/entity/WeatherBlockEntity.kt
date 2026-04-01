package org.pakicek.monoforecast.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "weather_blocks",
    foreignKeys = [
        ForeignKey(
            entity = LogFrameEntity::class,
            parentColumns = ["id"],
            childColumns = ["logId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WeatherBlockEntity(
    @PrimaryKey
    val logId: Long,
    val tempC: Double,
    val visibilityMeters: Int,
    val windSpeedMs: Double,
    val windDir: Int,
    val rainMm: Double
)