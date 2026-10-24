package com.anmol.cyclecare.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PeriodDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeriod(period: PeriodEntity): Long

    @Query("SELECT * FROM period_records ORDER BY startDate DESC")
    fun getAllPeriods(): Flow<List<PeriodEntity>>

    @Query("SELECT * FROM period_records WHERE id = :id LIMIT 1")
    suspend fun getPeriodById(id: Int): PeriodEntity?

    @Delete
    suspend fun deletePeriod(period: PeriodEntity)

    @Query("DELETE FROM period_records")
    suspend fun deleteAllPeriods()
}