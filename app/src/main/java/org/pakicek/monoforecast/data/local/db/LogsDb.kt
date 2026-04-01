package org.pakicek.monoforecast.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.pakicek.monoforecast.data.local.converter.LogTypeConverter
import org.pakicek.monoforecast.data.local.dao.LogsDao
import org.pakicek.monoforecast.data.local.entity.DeviceMetricsBlockEntity
import org.pakicek.monoforecast.data.local.entity.FileEntity
import org.pakicek.monoforecast.data.local.entity.LocationBlockEntity
import org.pakicek.monoforecast.data.local.entity.LogFrameEntity
import org.pakicek.monoforecast.data.local.entity.SettingsBlockEntity
import org.pakicek.monoforecast.data.local.entity.WeatherBlockEntity

@Database(
    entities = [
        LogFrameEntity::class,
        SettingsBlockEntity::class,
        DeviceMetricsBlockEntity::class,
        LocationBlockEntity::class,
        WeatherBlockEntity::class,
        FileEntity::class
    ],
    version = 3,
    exportSchema = false
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
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}