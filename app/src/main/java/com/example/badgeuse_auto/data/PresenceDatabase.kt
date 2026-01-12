package com.example.badgeuse_auto.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PresenceEntity::class,
        WorkLocationEntity::class,
        SettingsEntity::class
    ],
    version = 24,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class PresenceDatabase : RoomDatabase() {

    abstract fun presenceDao(): PresenceDao
    abstract fun workLocationDao(): WorkLocationDao
    abstract fun settingsDao(): SettingsDao

    companion object {

        @Volatile
        private var INSTANCE: PresenceDatabase? = null

        fun getDatabase(context: Context): PresenceDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    PresenceDatabase::class.java,
                    "presence_db"
                )
                    //.fallbackToDestructiveMigration() // 🔥 volontaire (DEV)
                    .addMigrations(*ALL_MIGRATIONS) //❌ PAS maintenant
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
