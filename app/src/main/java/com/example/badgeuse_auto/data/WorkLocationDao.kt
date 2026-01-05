package com.example.badgeuse_auto.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkLocationDao {

    @Insert
    suspend fun insert(location: WorkLocationEntity): Long

    //UPDATE
    @Update
    suspend fun update(location: WorkLocationEntity)
    @Delete
    suspend fun delete(location: WorkLocationEntity)

    @Query("""
        SELECT * FROM work_locations
        WHERE isActive = 1
        ORDER BY name ASC
    """)
    fun getActiveLocations(): Flow<List<WorkLocationEntity>>

    @Query("""
        SELECT * FROM work_locations
        ORDER BY name ASC
    """)
    suspend fun getAllLocations(): List<WorkLocationEntity>

    @Query("""
        SELECT * FROM work_locations
        WHERE id = :id
        LIMIT 1
    """)

    suspend fun getById(id: Long): WorkLocationEntity?
    @Query("SELECT * FROM work_locations WHERE isActive = 1")
    suspend fun getActiveLocationsOnce(): List<WorkLocationEntity>

    @Query("""
    SELECT * FROM work_locations
    ORDER BY name ASC
""")
    fun getAllLocationsFlow(): Flow<List<WorkLocationEntity>>

    @Query("""
    SELECT * FROM work_locations
    WHERE geofenceUid = :uid
    LIMIT 1
""")
    suspend fun getByGeofenceUid(uid: String): WorkLocationEntity?
}
