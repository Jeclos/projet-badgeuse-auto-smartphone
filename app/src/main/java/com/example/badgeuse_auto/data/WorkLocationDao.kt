package com.example.badgeuse_auto.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WorkLocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLocation(location: WorkLocationEntity)

    @Query("SELECT * FROM work_location WHERE id = 1 LIMIT 1")
    suspend fun getLocation(): WorkLocationEntity?

    @Query("DELETE FROM work_location")
    suspend fun deleteLocation()
}







