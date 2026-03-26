package org.pakicek.monoforecast.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = LogFrameEntity::class,
            parentColumns = ["id"],
            childColumns = ["logId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LocationBlockEntity(
    @PrimaryKey
    val logId: Long,
    var latitude: Double,
    var longitude: Double
)