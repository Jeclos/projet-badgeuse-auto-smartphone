package com.example.badgeuse_auto.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface SettingsDao {

    @Query("SELECT * FROM settings LIMIT 1")
    suspend fun getSettings(): SettingsEntity?

    @Query("SELECT * FROM settings LIMIT 1")
    fun getSettingsFlow(): Flow<SettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: SettingsEntity)

    @Query("UPDATE settings SET appStyle = :style WHERE id = 1")
    suspend fun updateAppStyle(style: String)

    @Query("UPDATE settings SET themeMode = :mode WHERE id = 1")
    suspend fun updateThemeMode(mode: ThemeMode)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: SettingsEntity)
}

