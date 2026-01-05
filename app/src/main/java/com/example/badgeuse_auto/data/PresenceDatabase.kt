package com.example.badgeuse_auto.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        PresenceEntity::class,
        WorkLocationEntity::class,
        SettingsEntity::class
    ],
    version = 19,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PresenceDatabase : RoomDatabase() {

    abstract fun presenceDao(): PresenceDao
    abstract fun workLocationDao(): WorkLocationDao

    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: PresenceDatabase? = null

        fun getDatabase(context: Context): PresenceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PresenceDatabase::class.java,
                    "presence_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
