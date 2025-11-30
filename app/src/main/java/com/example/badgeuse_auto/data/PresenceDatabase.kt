package com.example.badgeuse_auto.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PresenceEntry::class,
        WorkLocationEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class PresenceDatabase : RoomDatabase() {

    abstract fun presenceDao(): PresenceDao

    abstract fun workLocationDao(): WorkLocationDao

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
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Migration ajout de la table WorkLocation
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS work_location (
                        id INTEGER PRIMARY KEY,
                        name TEXT NOT NULL,
                        latitude REAL NOT NULL,
                        longitude REAL NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
