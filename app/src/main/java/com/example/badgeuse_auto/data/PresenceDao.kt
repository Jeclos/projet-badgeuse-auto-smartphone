package com.example.badgeuse_auto.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import androidx.room.Update
import androidx.room.Delete


@Dao
interface PresenceDao {

    @Insert
    suspend fun insert(entry: PresenceEntry)

    @Query("SELECT * FROM presence_table ORDER BY timestamp DESC")
    fun getAllPresences(): Flow<List<PresenceEntry>>

    @Query("SELECT * FROM presence_table WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp ASC")
    fun getBetween(from: Long, to: Long): Flow<List<PresenceEntry>>

    // NEW: suspend version returning a list (useful for recomputation)
    @Query("SELECT * FROM presence_table WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp ASC")
    suspend fun getBetweenList(from: Long, to: Long): List<PresenceEntry>

    // ⭐ NOUVEAU — obtenir la dernière entrée
    @Query("SELECT * FROM presence_table ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLast(): PresenceEntry?

    @Update
    suspend fun update(entry: PresenceEntry)

    @Delete
    suspend fun delete(entry: PresenceEntry)

}
