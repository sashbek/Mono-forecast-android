package org.pakicek.monoforecast.data.entities

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
data class DeviceMetricsBlockEntity(
    @PrimaryKey
    val logId: Long
)
