package org.pakicek.monoforecast.domain.model.dto.logs

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "files")
data class FileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val start: Long,
    var end: Long? = null
)
