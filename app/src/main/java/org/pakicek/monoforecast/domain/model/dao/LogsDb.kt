package org.pakicek.monoforecast.domain.model.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.pakicek.monoforecast.domain.model.dto.converters.LogTypeConverter
import org.pakicek.monoforecast.domain.model.dto.logs.LogFrameEntity
import org.pakicek.monoforecast.domain.model.dao.LogsDao

@Database(entities = [LogFrameEntity::class], version = 1)
@TypeConverters(LogTypeConverter::class)
abstract class LogsDb : RoomDatabase() {
    abstract fun logsRepository(): LogsDao

    companion object {
        @Volatile
        private var INSTANCE: LogsDb? = null

        fun getInstance(context: Context): LogsDb {
            if (INSTANCE != null) {
                return INSTANCE!!
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LogsDb::class.java,
                    "logs_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()

                INSTANCE = instance
                return instance
            }
        }
    }
}
