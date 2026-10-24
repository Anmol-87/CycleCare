package com.anmol.cyclecare.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anmol.cyclecare.data.local.PeriodEntity
import com.anmol.cyclecare.data.repository.PeriodRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class PeriodViewModel(
    private val repository: PeriodRepository
) : ViewModel() {

    // Observe all recorded periods.
    val periods: StateFlow<List<PeriodEntity>> =
        repository.getAllPeriods().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Save a period start date if it has not already been recorded.
    fun addPeriod(startDate: String) {
        viewModelScope.launch {
            val alreadyExists = repository.getAllPeriods()
                .first()
                .any { it.startDate == startDate }

            if (!alreadyExists) {
                repository.insertPeriod(
                    PeriodEntity(startDate = startDate)
                )
            }
        }
    }

    // Update the end date of an existing period.
    // Returns false if the dates are invalid or the record is missing.
    suspend fun updatePeriodEndDate(
        periodId: Int,
        endDate: String
    ): Boolean {

        val end = runCatching {
            LocalDate.parse(endDate)
        }.getOrNull() ?: return false

        val existingPeriod = repository.getPeriodById(periodId)
            ?: return false

        val start = runCatching {
            LocalDate.parse(existingPeriod.startDate)
        }.getOrNull() ?: return false

        // The end date cannot be before the start date
        // or later than today.
        if (end.isBefore(start) || end.isAfter(LocalDate.now())) {
            return false
        }

        // Update the existing database record.
        repository.insertPeriod(
            existingPeriod.copy(endDate = endDate)
        )

        return true
    }

    // Save the end date from the UI.
    // This function launches a coroutine so the UI
    // does not need to call a suspend function directly.
    fun savePeriodEndDate(
        periodId: Int,
        endDate: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val success: Boolean = updatePeriodEndDate(
                periodId = periodId,
                endDate = endDate
            )

            onResult(success)
        }
    }

    // Retrieve the most recently recorded period.
    val latestPeriodDate: StateFlow<String?> =
        periods.map { records ->
            records.firstOrNull()?.startDate
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Delete a particular period record.
    fun deletePeriod(period: PeriodEntity) {
        viewModelScope.launch {
            repository.deletePeriod(period)
        }
    }

    // Migrate the date saved by the older version of the app.
    fun migrateLegacyDate(startDate: String) {
        viewModelScope.launch {
            val existingRecords = repository.getAllPeriods().first()

            if (existingRecords.none { it.startDate == startDate }) {
                repository.insertPeriod(
                    PeriodEntity(startDate = startDate)
                )
            }
        }
    }
}