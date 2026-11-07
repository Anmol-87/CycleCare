package com.anmol.cyclecare.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CareResponseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveResponse(response: CareResponseEntity)

    @Query(
        """
        SELECT * FROM care_responses
        ORDER BY answeredAt DESC
        """
    )
    fun getAllResponses(): Flow<List<CareResponseEntity>>

    @Query(
        """
        SELECT * FROM care_responses
        WHERE reminderId = :reminderId
        LIMIT 1
        """
    )
    suspend fun getResponseForReminder(
        reminderId: String
    ): CareResponseEntity?

    @Query(
        """
        SELECT * FROM care_responses
        WHERE answeredAt >= :startTime
          AND answeredAt < :endTime
        ORDER BY answeredAt DESC
        """
    )
    fun getResponsesForDay(
        startTime: Long,
        endTime: Long
    ): Flow<List<CareResponseEntity>>
}