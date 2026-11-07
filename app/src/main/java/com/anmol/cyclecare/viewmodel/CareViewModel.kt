package com.anmol.cyclecare.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anmol.cyclecare.data.local.CareResponseEntity
import com.anmol.cyclecare.data.local.CycleCareDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class CareViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val careResponseDao =
        CycleCareDatabase.getDatabase(application)
            .careResponseDao()

    // Observe saved water, food, and fruit responses.
    val careResponses: StateFlow<List<CareResponseEntity>> =
        careResponseDao.getAllResponses().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}