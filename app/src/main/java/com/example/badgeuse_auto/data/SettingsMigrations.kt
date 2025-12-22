package com.example.badgeuse_auto.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL(
            """
            ALTER TABLE settings ADD COLUMN lunchBreakEnabled INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )

        db.execSQL(
            """
            ALTER TABLE settings ADD COLUMN lunchBreakOutside INTEGER NOT NULL DEFAULT 1
            """.trimIndent()
        )

        db.execSQL(
            """
            ALTER TABLE settings ADD COLUMN lunchBreakDurationMin INTEGER NOT NULL DEFAULT 60
            """.trimIndent()
        )
    }
}
