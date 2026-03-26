package org.pakicek.monoforecast.data.entities

import androidx.room.Embedded
import androidx.room.Relation

data class LogWithDetails(
    @Embedded val log: LogFrameEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "logId"
    )
    val location: LocationBlockEntity? = null,

    @Relation(
        parentColumn = "id",
        entityColumn = "logId"
    )
    val weather: WeatherBlockEntity? = null,

    @Relation(
        parentColumn = "id",
        entityColumn = "logId"
    )
    val settings: SettingsBlockEntity? = null
)