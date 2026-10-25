package com.anmol.cyclecare.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anmol.cyclecare.data.local.PeriodEntity
import com.anmol.cyclecare.data.repository.PeriodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class PeriodViewModel(
    private val repository: PeriodRepository,
    application: Application
) : AndroidViewModel(application) {

    private val preferences = application.getSharedPreferences(
        "cyclecare_preferences",
        Application.MODE_PRIVATE
    )

    companion object {
        private const val KEY_CYCLE_LENGTH = "cycle_length"
        private const val DEFAULT_CYCLE_LENGTH = 28
        private const val MIN_CYCLE_LENGTH = 15
        private const val MAX_CYCLE_LENGTH = 60
    }

    // Save symptoms and personal notes for a period.
    fun updatePeriodDetails(
        periodId: Int,
        symptoms: String,
        notes: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val existingPeriod = repository.getPeriodById(periodId)

            if (existingPeriod == null) {
                onResult(false)
                return@launch
            }

            repository.insertPeriod(
                existingPeriod.copy(
                    symptoms = symptoms,
                    notes = notes
                )
            )

            onResult(true)
        }
    }

    // Observe all recorded periods.
    val periods: StateFlow<List<PeriodEntity>> =
        repository.getAllPeriods().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // The user's selected cycle length.
    // Updates immediately whenever the user changes the value.
    private val _selectedCycleLength = MutableStateFlow(
        preferences.getInt(
            KEY_CYCLE_LENGTH,
            DEFAULT_CYCLE_LENGTH
        ).coerceIn(MIN_CYCLE_LENGTH, MAX_CYCLE_LENGTH)
    )

    val selectedCycleLength: StateFlow<Int> =
        _selectedCycleLength.asStateFlow()

    // Update and save the user's preferred cycle length.
    fun setCycleLength(days: Int) {
        if (days !in MIN_CYCLE_LENGTH..MAX_CYCLE_LENGTH) {
            return
        }

        preferences.edit()
            .putInt(KEY_CYCLE_LENGTH, days)
            .apply()

        _selectedCycleLength.value = days
    }

    // Average length of recorded cycles.
    // Only valid intervals between consecutive period start dates
    // are included in the calculation.
    val averageCycleLength: StateFlow<Int?> =
        periods.map { records ->
            val dates = records.mapNotNull { record ->
                runCatching {
                    LocalDate.parse(record.startDate)
                }.getOrNull()
            }.distinct().sorted()

            val intervals = dates.zipWithNext { first, second ->
                ChronoUnit.DAYS.between(first, second).toInt()
            }.filter {
                it in MIN_CYCLE_LENGTH..MAX_CYCLE_LENGTH
            }

            if (intervals.isEmpty()) {
                null
            } else {
                intervals.average().toInt()
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // The latest recorded period start date.
    val latestPeriodDate: StateFlow<String?> =
        periods.map { records ->
            records.firstOrNull()?.startDate
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Predict the next period using the selected cycle length.
    // The prediction updates when either the latest period date
    // or the selected cycle length changes.
    val nextPredictedPeriodDate: StateFlow<LocalDate?> =
        combine(
            latestPeriodDate,
            selectedCycleLength
        ) { latestDate, cycleLength ->
            val startDate = latestDate?.let {
                runCatching {
                    LocalDate.parse(it)
                }.getOrNull()
            }

            startDate?.plusDays(cycleLength.toLong())
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Save a period start date if it has not already been recorded.
    fun addPeriod(startDate: String) {
        viewModelScope.launch {
            val parsedDate = runCatching {
                LocalDate.parse(startDate)
            }.getOrNull() ?: return@launch

            if (parsedDate.isAfter(LocalDate.now())) {
                return@launch
            }

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

        if (end.isBefore(start) || end.isAfter(LocalDate.now())) {
            return false
        }

        repository.insertPeriod(
            existingPeriod.copy(endDate = endDate)
        )

        return true
    }

    // Save the end date from the UI.
    fun savePeriodEndDate(
        periodId: Int,
        endDate: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            onResult(
                updatePeriodEndDate(
                    periodId = periodId,
                    endDate = endDate
                )
            )
        }
    }

    // Delete a particular period record.
    fun deletePeriod(period: PeriodEntity) {
        viewModelScope.launch {
            repository.deletePeriod(period)
        }
    }

    // Migrate a date saved by the older version of the app.
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