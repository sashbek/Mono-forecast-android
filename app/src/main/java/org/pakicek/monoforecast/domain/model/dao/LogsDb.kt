package org.pakicek.monoforecast.domain.model.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.pakicek.monoforecast.domain.model.dto.converters.LogTypeConverter
import org.pakicek.monoforecast.domain.model.dto.logs.LogFrameEntity
import org.pakicek.monoforecast.domain.model.dao.LogsDao
import org.pakicek.monoforecast.domain.model.dto.logs.DeviceMetricsBlockEntity
import org.pakicek.monoforecast.domain.model.dto.logs.FileEntity
import org.pakicek.monoforecast.domain.model.dto.logs.LocationBlockEntity
import org.pakicek.monoforecast.domain.model.dto.logs.SettingsBlockEntity
import org.pakicek.monoforecast.domain.model.dto.logs.WeatherBlockEntity

@Database(entities = [
    LogFrameEntity::class,
    SettingsBlockEntity::class,
    DeviceMetricsBlockEntity::class,
    LocationBlockEntity::class,
    WeatherBlockEntity::class,
    FileEntity::class],
    version = 1)
@TypeConverters(LogTypeConverter::class)
abstract class LogsDb : RoomDatabase() {

    abstract fun logsDao(): LogsDao

    companion object {
        @Volatile
        private var INSTANCE: LogsDb? = null

        fun getInstance(context: Context): LogsDb {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?:
                    Room.databaseBuilder(
                        context.applicationContext,
                        LogsDb::class.java,
                        "logs_database"
                    )
                    .fallbackToDestructiveMigration(false)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
