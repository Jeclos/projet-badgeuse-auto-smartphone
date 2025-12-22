package com.example.badgeuse_auto.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PresenceEntry::class,
        WorkLocationEntity::class,
        SettingsEntity::class,
        DailyWorkSummary::class
    ],
    version = 7, // 🔴 ON INCRÉMENTE LA VERSION
    exportSchema = false
)
abstract class PresenceDatabase : RoomDatabase() {

    abstract fun presenceDao(): PresenceDao
    abstract fun workLocationDao(): WorkLocationDao
    abstract fun dailySummaryDao(): DailySummaryDao
    abstract fun settingsDao(): SettingsDao // ✅ AJOUT ICI

    companion object {
        @Volatile private var INSTANCE: PresenceDatabase? = null

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

