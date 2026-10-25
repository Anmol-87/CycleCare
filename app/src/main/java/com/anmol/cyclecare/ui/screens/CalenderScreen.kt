package com.anmol.cyclecare.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anmol.cyclecare.data.local.PeriodEntity
import com.anmol.cyclecare.ui.components.PeriodCalendar
import com.anmol.cyclecare.viewmodel.PeriodViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val DEFAULT_PREDICTED_PERIOD_DAYS = 5

private val CalendarPink = Color(0xFFB83D68)
private val CalendarLightPink = Color(0xFFFCE8EF)
private val CalendarPurple = Color(0xFF8660B5)
private val CalendarBackground = Color(0xFFFFF8FA)

private val calendarDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy")

@Composable
fun CalendarScreen(
    periodViewModel: PeriodViewModel
) {
    val periods by periodViewModel.periods.collectAsState()

    val nextPredictedPeriodDate by
    periodViewModel.nextPredictedPeriodDate.collectAsState()

    val selectedCycleLength by
    periodViewModel.selectedCycleLength.collectAsState()

    var selectedDate by remember {
        mutableStateOf<LocalDate?>(null)
    }

    val startDates: Set<LocalDate> = remember(periods) {
        periods.mapNotNull { period ->
            parseCalendarDate(period.startDate)
        }.toSet()
    }

    val endDates: Set<LocalDate> = remember(periods) {
        periods.mapNotNull { period ->
            period.endDate?.let { parseCalendarDate(it) }
        }.toSet()
    }

    val periodDates: Set<LocalDate> = remember(periods) {
        buildSet {
            periods.forEach { period ->
                val start = parseCalendarDate(period.startDate)
                    ?: return@forEach

                val end = period.endDate?.let {
                    parseCalendarDate(it)
                } ?: start

                var date = start

                while (!date.isAfter(end)) {
                    add(date)
                    date = date.plusDays(1)
                }
            }
        }
    }

    // Use the prediction already calculated by the ViewModel.
    val predictedStartDate: LocalDate? = nextPredictedPeriodDate

    // Highlight the five predicted period days.
    val predictedDates: Set<LocalDate> =
        remember(predictedStartDate, periodDates) {
            val startDate: LocalDate? = predictedStartDate

            if (startDate == null) {
                emptySet()
            } else {
                buildSet<LocalDate> {
                    repeat(DEFAULT_PREDICTED_PERIOD_DAYS) { offset ->
                        val date = startDate.plusDays(offset.toLong())

                        if (date !in periodDates) {
                            add(date)
                        }
                    }
                }
            }
        }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CalendarBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Your Calendar 🌸",
                style = MaterialTheme.typography.headlineMedium,
                color = CalendarPink
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Track your period and view your cycle predictions.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )
        }

        item {
            PeriodCalendar(
                periodDates = periodDates,
                startDates = startDates,
                endDates = endDates,
                modifier = Modifier.fillMaxWidth(),
                onDateSelected = { date ->
                    selectedDate = date
                },
                predictedDates = predictedDates
            )
        }

        item {
            CalendarLegend()
        }

        item {
            PredictionCard(
                predictedStartDate = predictedStartDate,
                cycleLength = selectedCycleLength
            )
        }

        item {
            Text(
                text = "Period History",
                style = MaterialTheme.typography.titleLarge,
                color = CalendarPink
            )
        }

        if (periods.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "No periods recorded yet 💗",
                            style = MaterialTheme.typography.titleMedium,
                            color = CalendarPink
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Add a period start date on the Home screen " +
                                    "to begin tracking your cycle.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            items(
                items = periods,
                key = { it.id }
            ) { period ->
                PeriodHistoryCard(
                    period = period,
                    onEdit = {
                        selectedDate = parseCalendarDate(period.startDate)
                    },
                    onDelete = {
                        periodViewModel.deletePeriod(period)
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Cycle predictions are estimates, not guarantees. " +
                        "Your actual period dates may vary.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }

    val dateForEditing = selectedDate

    if (dateForEditing != null) {
        val selectedPeriod = periods.firstOrNull { period ->
            period.startDate == dateForEditing.toString()
        }

        if (selectedPeriod != null) {
            PeriodDetailsEditor(
                period = selectedPeriod,
                onDismiss = {
                    selectedDate = null
                },
                onSaveEndDate = { endDate, onResult ->
                    periodViewModel.savePeriodEndDate(
                        periodId = selectedPeriod.id,
                        endDate = endDate,
                        onResult = onResult
                    )
                },
                onSaveDetails = { symptoms, notes, onResult ->
                    periodViewModel.updatePeriodDetails(
                        periodId = selectedPeriod.id,
                        symptoms = symptoms,
                        notes = notes,
                        onResult = onResult
                    )
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = {
                    selectedDate = null
                },
                title = {
                    Text("No period recorded")
                },
                text = {
                    Text(
                        "This date does not have a saved period record. " +
                                "You can add a period from the Home screen."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            selectedDate = null
                        }
                    ) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
private fun CalendarLegend() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LegendRow(
                color = CalendarPink,
                label = "Period start date"
            )

            LegendRow(
                color = CalendarLightPink,
                label = "Recorded period days"
            )

            LegendRow(
                color = CalendarPurple,
                label = "Period end date"
            )

            LegendRow(
                color = Color(0xFFE9E0FA),
                label = "Predicted period days"
            )
        }
    }
}

@Composable
private fun LegendRow(
    color: Color,
    label: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Spacer(
            modifier = Modifier
                .height(18.dp)
                .fillMaxWidth(0.04f)
                .background(
                    color = color,
                    shape = RoundedCornerShape(5.dp)
                )
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun PredictionCard(
    predictedStartDate: LocalDate?,
    cycleLength: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF2ECFC)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Next Period Prediction 🌷",
                style = MaterialTheme.typography.titleMedium,
                color = CalendarPurple
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (predictedStartDate == null) {
                Text(
                    text = "Record a period start date to see your prediction.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = predictedStartDate.format(calendarDateFormatter),
                    style = MaterialTheme.typography.headlineSmall,
                    color = CalendarPurple
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Based on your selected $cycleLength-day cycle.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "This is an estimate. Your cycle may vary.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
private fun PeriodHistoryCard(
    period: PeriodEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirmation by remember {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Period started: ${
                    parseCalendarDate(period.startDate)
                        ?.format(calendarDateFormatter)
                        ?: period.startDate
                }",
                style = MaterialTheme.typography.titleMedium,
                color = CalendarPink
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (period.endDate.isNullOrBlank()) {
                    "End date: Not recorded"
                } else {
                    "End date: ${
                        parseCalendarDate(period.endDate)
                            ?.format(calendarDateFormatter)
                            ?: period.endDate
                    }"
                },
                style = MaterialTheme.typography.bodyMedium
            )

            if (period.symptoms.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Symptoms: ${period.symptoms}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (period.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Notes: ${period.notes}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Edit")
                }

                Button(
                    onClick = {
                        showDeleteConfirmation = true
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CalendarPink
                    )
                ) {
                    Text("Delete")
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmation = false
            },
            title = {
                Text("Delete period record?")
            },
            text = {
                Text(
                    "This will permanently delete this period record " +
                            "and its saved symptoms and notes."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    }
                ) {
                    Text(
                        "Delete",
                        color = CalendarPink
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodDetailsEditor(
    period: PeriodEntity,
    onDismiss: () -> Unit,
    onSaveEndDate: (
        endDate: String,
        onResult: (Boolean) -> Unit
    ) -> Unit,
    onSaveDetails: (
        symptoms: String,
        notes: String,
        onResult: (Boolean) -> Unit
    ) -> Unit
) {
    var symptoms by remember(period.id) {
        mutableStateOf(period.symptoms)
    }

    var notes by remember(period.id) {
        mutableStateOf(period.notes)
    }

    var endDate by remember(period.id) {
        mutableStateOf(period.endDate ?: "")
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    var isSaving by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    AlertDialog(
        onDismissRequest = {
            if (!isSaving) onDismiss()
        },
        title = {
            Text(
                text = "Edit Period Details",
                color = CalendarPink
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Start date: ${
                            parseCalendarDate(period.startDate)
                                ?.format(calendarDateFormatter)
                                ?: period.startDate
                        }",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                item {
                    Text(
                        text = "Period end date",
                        style = MaterialTheme.typography.titleSmall
                    )

                    OutlinedButton(
                        onClick = {
                            showDatePicker = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (endDate.isBlank()) {
                                "Select end date"
                            } else {
                                parseCalendarDate(endDate)
                                    ?.format(calendarDateFormatter)
                                    ?: endDate
                            }
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = symptoms,
                        onValueChange = {
                            symptoms = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Symptoms")
                        },
                        placeholder = {
                            Text("e.g. cramps, headache, tiredness")
                        },
                        minLines = 2
                    )
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = {
                            notes = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Personal notes")
                        },
                        placeholder = {
                            Text("How are you feeling?")
                        },
                        minLines = 3
                    )
                }

                if (errorMessage != null) {
                    item {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = {
                    isSaving = true
                    errorMessage = null

                    val originalEndDate = period.endDate

                    fun saveDetailsAfterEndDate() {
                        onSaveDetails(symptoms, notes) { detailsSaved ->
                            isSaving = false

                            if (detailsSaved) {
                                onDismiss()
                            } else {
                                errorMessage =
                                    "Could not save the period details."
                            }
                        }
                    }

                    if (endDate.isBlank()) {
                        if (originalEndDate == null) {
                            saveDetailsAfterEndDate()
                        } else {
                            onSaveEndDate("") { endSaved ->
                                if (endSaved) {
                                    saveDetailsAfterEndDate()
                                } else {
                                    isSaving = false
                                    errorMessage =
                                        "Could not update the end date."
                                }
                            }
                        }
                    } else if (endDate == originalEndDate) {
                        saveDetailsAfterEndDate()
                    } else {
                        onSaveEndDate(endDate) { endSaved ->
                            if (endSaved) {
                                saveDetailsAfterEndDate()
                            } else {
                                isSaving = false
                                errorMessage =
                                    "Choose a valid end date on or after " +
                                            "the start date, and not in the future."
                            }
                        }
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isSaving,
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )

    if (showDatePicker) {
        val initialMillis = remember(endDate, period.startDate) {
            val selected = parseCalendarDate(endDate)
                ?: parseCalendarDate(period.startDate)
                ?: LocalDate.now()

            selected.atStartOfDay(ZoneId.of("UTC"))
                .toInstant()
                .toEpochMilli()
        }

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis
        )

        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis =
                            datePickerState.selectedDateMillis

                        if (millis != null) {
                            endDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                                .toString()
                        }

                        showDatePicker = false
                    }
                ) {
                    Text("Select")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState
            )
        }
    }
}

private fun parseCalendarDate(value: String?): LocalDate? {
    if (value.isNullOrBlank()) {
        return null
    }

    return runCatching {
        LocalDate.parse(value)
    }.getOrNull()
}