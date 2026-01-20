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
   MIGRATION 24 → 25
   (AJOUT CHAMPS PAUSE DÉJEUNER)
   ========================================================= */

val MIGRATION_24_25 = object : Migration(24, 25) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("""
            ALTER TABLE presences
            ADD COLUMN timestamp INTEGER NOT NULL DEFAULT 0
        """.trimIndent())

        db.execSQL("""
            ALTER TABLE presences
            ADD COLUMN isEnter INTEGER NOT NULL DEFAULT 0
        """.trimIndent())

        db.execSQL("""
            ALTER TABLE presences
            ADD COLUMN isExit INTEGER NOT NULL DEFAULT 0
        """.trimIndent())

        db.execSQL("""
            ALTER TABLE presences
            ADD COLUMN minutesOfDay INTEGER NOT NULL DEFAULT 0
        """.trimIndent())
    }
}

val MIGRATION_25_26 = object : Migration(25, 26) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("ALTER TABLE settings ADD COLUMN lunchEnabled INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE settings ADD COLUMN lunchWindowStartHour INTEGER NOT NULL DEFAULT 12")
        db.execSQL("ALTER TABLE settings ADD COLUMN lunchWindowStartMinute INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE settings ADD COLUMN lunchWindowEndHour INTEGER NOT NULL DEFAULT 14")
        db.execSQL("ALTER TABLE settings ADD COLUMN lunchWindowEndMinute INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE settings ADD COLUMN lunchDefaultDurationMin INTEGER NOT NULL DEFAULT 60")
        db.execSQL("ALTER TABLE settings ADD COLUMN lunchMinDurationMin INTEGER NOT NULL DEFAULT 30")
    }
}


/* =========================================================
   REGISTRE CENTRAL
   ========================================================= */

val ALL_MIGRATIONS = arrayOf(
    MIGRATION_22_23,
    MIGRATION_23_24,
    MIGRATION_24_25,
    MIGRATION_25_26
)
