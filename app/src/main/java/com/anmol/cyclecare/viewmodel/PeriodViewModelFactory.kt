package com.anmol.cyclecare.viewmodel
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anmol.cyclecare.data.repository.PeriodRepository
class PeriodViewModelFactory(
    private val repository: PeriodRepository,
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PeriodViewModel::class.java)) {
            return PeriodViewModel(
                repository = repository,
                application = application
            ) as T
        }
        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}