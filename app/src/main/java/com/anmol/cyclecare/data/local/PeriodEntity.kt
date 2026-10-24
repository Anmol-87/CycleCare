package com.anmol.cyclecare.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "period_records")
data class PeriodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val startDate: String,
    val endDate: String? = null,
    val symptoms: String = "",
    val notes: String = ""
)