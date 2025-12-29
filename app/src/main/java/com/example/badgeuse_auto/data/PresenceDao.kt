package com.example.badgeuse_auto.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PresenceDao {

    // 🔄 Toutes les présences
    @Query("""
        SELECT * FROM presences
        ORDER BY enterTime DESC
    """)
    fun getAllPresences(): Flow<List<PresenceEntity>>

    // ▶️ Présence en cours (non clôturée)
    @Query("""
        SELECT * FROM presences
        WHERE exitTime IS NULL
        ORDER BY enterTime DESC
        LIMIT 1
    """)
    suspend fun getCurrentPresence(): PresenceEntity?

    // 📆 Présences entre deux dates
    @Query("""
        SELECT * FROM presences
        WHERE enterTime BETWEEN :from AND :to
        ORDER BY enterTime ASC
    """)
    suspend fun getPresencesBetween(
        from: Long,
        to: Long
    ): List<PresenceEntity>

    // ➕ INSERT
    @Insert
    suspend fun insert(presence: PresenceEntity): Long

    // ✏️ UPDATE
    @Update
    suspend fun update(presence: PresenceEntity)

    // ❌ DELETE
    @Delete
    suspend fun delete(presence: PresenceEntity)
}
