package com.anmol.cyclecare.viewmodel
import kotlinx.coroutines.flow.first
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anmol.cyclecare.data.local.PeriodEntity
import com.anmol.cyclecare.data.repository.PeriodRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PeriodViewModel(
    private val repository: PeriodRepository
) : ViewModel() {

    val periods: StateFlow<List<PeriodEntity>> =
        repository.getAllPeriods().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Save a date only if it has not already been recorded.
    fun addPeriod(startDate: String) {
        viewModelScope.launch {
            val alreadyExists = periods.value.any {
                it.startDate == startDate
            }

            if (!alreadyExists) {
                repository.insertPeriod(
                    PeriodEntity(startDate = startDate)
                )
            }
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

    fun deletePeriod(period: PeriodEntity) {
        viewModelScope.launch {
            repository.deletePeriod(period)
        }
    }
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