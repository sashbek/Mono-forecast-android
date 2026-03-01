package org.pakicek.monoforecast.domain.model.dto.logs

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey

@Entity(
    tableName = "settings",
    foreignKeys = [
        ForeignKey(
            entity = LogFrameEntity::class,
            parentColumns = ["id"],
            childColumns = ["logId"],
            onDelete = CASCADE
        )
    ]
)
data class SettingsBlockEntity(
    @PrimaryKey
    var logId: Long,
    val setting: String,
    val value: String
)
