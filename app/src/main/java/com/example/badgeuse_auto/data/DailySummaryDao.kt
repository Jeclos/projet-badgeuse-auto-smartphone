package com.example.badgeuse_auto.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailySummaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(summary: DailyWorkSummary)

    @Query("SELECT * FROM daily_work_summary ORDER BY dayStart DESC")
    fun getAllSummaries(): Flow<List<DailyWorkSummary>>

    @Query("SELECT * FROM daily_work_summary WHERE dayStart BETWEEN :from AND :to ORDER BY dayStart ASC")
    fun getSummariesBetween(from: Long, to: Long): Flow<List<DailyWorkSummary>>

    @Query("SELECT * FROM daily_work_summary WHERE dayStart BETWEEN :from AND :to ORDER BY dayStart ASC")
    suspend fun getSummariesBetweenList(from: Long, to: Long): List<DailyWorkSummary>


}
