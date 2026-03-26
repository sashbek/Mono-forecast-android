package org.pakicek.monoforecast.data.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.pakicek.monoforecast.data.entities.DeviceMetricsBlockEntity
import org.pakicek.monoforecast.data.entities.FileEntity
import org.pakicek.monoforecast.data.entities.LocationBlockEntity
import org.pakicek.monoforecast.data.entities.LogFrameEntity
import org.pakicek.monoforecast.data.entities.SettingsBlockEntity
import org.pakicek.monoforecast.data.entities.WeatherBlockEntity

@Database(
    entities = [
        LogFrameEntity::class,
        SettingsBlockEntity::class,
        DeviceMetricsBlockEntity::class,
        LocationBlockEntity::class,
        WeatherBlockEntity::class,
        FileEntity::class
    ],
    version = 2
)
@TypeConverters(LogTypeConverter::class)
abstract class LogsDb : RoomDatabase() {

    abstract fun logsDao(): LogsDao

    companion object {
        @Volatile
        private var INSTANCE: LogsDb? = null

        fun getInstance(context: Context): LogsDb {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    LogsDb::class.java,
                    "logs_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}