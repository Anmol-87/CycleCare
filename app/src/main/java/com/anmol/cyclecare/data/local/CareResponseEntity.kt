package com.anmol.cyclecare.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "care_responses",
    indices = [
        Index(value = ["reminderId"], unique = true)
    ]
)
data class CareResponseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // WATER, FOOD, or FRUITS
    val reminderType: String,

    // YES or NO
    val response: String,

    // Date and time when she answered
    val answeredAt: Long,

    // Unique identifier for this scheduled reminder
    val reminderId: String
)