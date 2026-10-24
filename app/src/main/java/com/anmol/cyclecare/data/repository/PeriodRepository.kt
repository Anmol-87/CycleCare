package com.anmol.cyclecare.data.repository

import com.anmol.cyclecare.data.local.PeriodDao
import com.anmol.cyclecare.data.local.PeriodEntity
import kotlinx.coroutines.flow.Flow

class PeriodRepository(
    private val periodDao: PeriodDao
) {

    // Save a period record
    suspend fun insertPeriod(period: PeriodEntity): Long {
        return periodDao.insertPeriod(period)
    }

    // Observe all saved period records
    fun getAllPeriods(): Flow<List<PeriodEntity>> {
        return periodDao.getAllPeriods()
    }

    // Retrieve one period record
    suspend fun getPeriodById(id: Int): PeriodEntity? {
        return periodDao.getPeriodById(id)
    }

    // Delete a particular period record
    suspend fun deletePeriod(period: PeriodEntity) {
        periodDao.deletePeriod(period)
    }

    // Delete all period records
    suspend fun deleteAllPeriods() {
        periodDao.deleteAllPeriods()
    }
}