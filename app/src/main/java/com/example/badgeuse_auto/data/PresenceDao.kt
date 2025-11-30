package com.example.badgeuse_auto.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PresenceDao {
    @Insert
    suspend fun insert(entry: PresenceEntry)

    @Query("SELECT * FROM presence_table ORDER BY timestamp DESC")
    fun getAllPresences(): Flow<List<PresenceEntry>>

    @Query("SELECT * FROM presence_table WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp ASC")
    fun getBetween(from: Long, to: Long): Flow<List<PresenceEntry>>
}
