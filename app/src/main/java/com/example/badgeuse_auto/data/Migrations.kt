package com.example.badgeuse_auto.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/* =========================================================
   MIGRATION 22 → 23
   (EXISTANTE – NE PAS MODIFIER)
   ========================================================= */

val MIGRATION_22_23 = object : Migration(22, 23) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("""
            CREATE TABLE presences_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                workLocationId INTEGER NOT NULL,
                enterTime INTEGER NOT NULL,
                exitTime INTEGER,
                enterType TEXT NOT NULL,
                exitType TEXT,
                lastDepotExitTime INTEGER,
                locked INTEGER NOT NULL,
                isPending INTEGER NOT NULL,
                pendingEnterAt INTEGER,
                isExitPending INTEGER NOT NULL,
                pendingExitAt INTEGER
            )
        """.trimIndent())

        db.execSQL("""
            INSERT INTO presences_new (
                id,
                workLocationId,
                enterTime,
                exitTime,
                enterType,
                exitType,
                lastDepotExitTime,
                locked,
                isPending,
                pendingEnterAt,
                isExitPending,
                pendingExitAt
            )
            SELECT
                id,
                workLocationId,
                enterTime,
                exitTime,
                enterType,
                exitType,
                lastDepotExitTime,
                locked,
                isPending,
                pendingEnterAt,
                0 AS isExitPending,
                pendingExitAt
            FROM presences
        """.trimIndent())

        db.execSQL("DROP TABLE presences")
        db.execSQL("ALTER TABLE presences_new RENAME TO presences")
    }
}

/* =========================================================
   MIGRATION 23 → 24
   (AJOUT PENDING ENTER DANS SETTINGS)
   ========================================================= */

val MIGRATION_23_24 = object : Migration(23, 24) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("""
            ALTER TABLE settings
            ADD COLUMN pendingEnterUid TEXT
        """.trimIndent())

        db.execSQL("""
            ALTER TABLE settings
            ADD COLUMN pendingEnterToken INTEGER
        """.trimIndent())
    }
}

/* =========================================================
   REGISTRE CENTRAL
   ========================================================= */

val ALL_MIGRATIONS = arrayOf(
    MIGRATION_22_23,
    MIGRATION_23_24
)
