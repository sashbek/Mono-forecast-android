package org.pakicek.monoforecast.domain.model.dto.logs

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import java.time.ZonedDateTime
import java.time.Instant
import java.time.ZoneId

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
data class LocationBlockEntity(
    @PrimaryKey
    val logId: Long,

    var latitude: Double,
    var longitude: Double
) {

    constructor(id: Long, nmeaString: String) : this (
        id,
        (nmeaString.split(",")[0]).toDoubleOrNull() ?: 0.0,
        (nmeaString.split(",")[2]).toDoubleOrNull() ?: 0.0) {}
}
