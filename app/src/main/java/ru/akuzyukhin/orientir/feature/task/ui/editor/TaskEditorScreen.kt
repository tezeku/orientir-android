package ru.akuzyukhin.orientir.feature.task.ui.editor

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.akuzyukhin.orientir.core.ui.CollectAsEffect
import ru.akuzyukhin.orientir.feature.task.domain.model.Importance
import ru.akuzyukhin.orientir.feature.task.domain.model.TaskType
import ru.akuzyukhin.orientir.feature.task.domain.util.toShortRussian
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")
@RequiresApi(Build.VERSION_CODES.O)
private val DATE_FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy", java.util.Locale("ru"))

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TaskEditorScreen(
    onNavigateBack: () -> Unit,
    viewModel: TaskEditorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CollectAsEffect(viewModel.events) { event ->
        when (event) {
            TaskEditorUiEvent.NavigateBackWithSuccess -> onNavigateBack()
        }
    }

    TaskEditorContent(
        state = state,
        onBackClick = onNavigateBack,
        onNameChange = viewModel::onNameChange,
        onTypeChange = viewModel::onTypeChange,
        onImportanceChange = viewModel::onImportanceChange,
        onTimeChange = viewModel::onTimeChange,
        onWindowChange = viewModel::onWindowChange,
        onPatternKindChange = viewModel::onPatternKindChange,
        onWeeklyDayToggle = viewModel::onWeeklyDayToggle,
        onIntervalChange = viewModel::onIntervalChange,
        onOnceDateChange = viewModel::onOnceDateChange,
        onSave = viewModel::onSave
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskEditorContent(
    state: TaskEditorUiState,
    onBackClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onTypeChange: (TaskType) -> Unit,
    onImportanceChange: (Importance) -> Unit,
    onTimeChange: (LocalTime) -> Unit,
    onWindowChange: (String) -> Unit,
    onPatternKindChange: (PatternKind) -> Unit,
    onWeeklyDayToggle: (DayOfWeek) -> Unit,
    onIntervalChange: (String) -> Unit,
    onOnceDateChange: (LocalDate) -> Unit,
    onSave: () -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var windowText by remember(state.windowMinutes) { mutableStateOf(state.windowMinutes.toString()) }
    var intervalText by remember(state.intervalDays) { mutableStateOf(state.intervalDays.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditMode) "Редактирование задачи" else "Новая задача") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (state.isCustomMode) {
                CustomRruleWarning(state.customRrule!!)
            }

            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = { Text("Название задачи") },
                placeholder = { Text("Например: Принять лекарство") },
                singleLine = true,
                isError = state.nameError != null,
                supportingText = state.nameError?.let { { Text(it) } },
                enabled = !state.isCustomMode,
                modifier = Modifier.fillMaxWidth()
            )

            SectionLabel("Тип задачи")
            TaskTypeSelector(state.type, onTypeChange, enabled = !state.isCustomMode)

            SectionLabel("Важность")
            ImportanceSelector(state.importance, onImportanceChange, enabled = !state.isCustomMode)

            SectionLabel("Время выполнения")
            OutlinedButton(
                onClick = { showTimePicker = true },
                enabled = !state.isCustomMode,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AccessTime, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(state.scheduledTime.format(TIME_FORMAT))
            }

            OutlinedTextField(
                value = windowText,
                onValueChange = { input ->
                    windowText = input.filter { it.isDigit() }
                    onWindowChange(windowText)
                },
                label = { Text("Окно выполнения (минут)") },
                supportingText = state.windowError?.let { { Text(it) } }
                    ?: { Text("На сколько минут можно опоздать или выполнить раньше") },
                isError = state.windowError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                enabled = !state.isCustomMode,
                modifier = Modifier.fillMaxWidth()
            )

            SectionLabel("Правило повторения")
            if (!state.isCustomMode) {
                PatternKindSelector(state.patternKind, onPatternKindChange)
                Spacer(Modifier.height(4.dp))
                when (state.patternKind) {
                    PatternKind.DAILY -> {
                        InfoText("Задача будет повторяться каждый день в выбранное время")
                    }
                    PatternKind.WEEKLY -> {
                        WeekdaySelector(state.weeklyDays, onWeeklyDayToggle)
                    }
                    PatternKind.EVERY_N_DAYS -> {
                        OutlinedTextField(
                            value = intervalText,
                            onValueChange = { input ->
                                intervalText = input.filter { it.isDigit() }
                                onIntervalChange(intervalText)
                            },
                            label = { Text("Каждые N дней") },
                            supportingText = state.intervalError?.let { { Text(it) } },
                            isError = state.intervalError != null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    PatternKind.ONCE -> {
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CalendarMonth, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(state.onceDate.format(DATE_FORMAT))
                        }
                    }
                }
            }

            if (state.errorMessage != null) {
                Text(
                    state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onSave,
                enabled = state.canSubmit,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (state.isEditMode) "Сохранить" else "Создать задачу")
                }
            }
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            initial = state.scheduledTime,
            onConfirm = {
                onTimeChange(it)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }

    if (showDatePicker) {
        DatePickerDialogCompose(
            initial = state.onceDate,
            onConfirm = {
                onOnceDateChange(it)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun InfoText(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun CustomRruleWarning(rrule: String) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Расширенное правило повторения",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "У этой задачи задано сложное правило, не поддерживаемое конструктором. " +
                        "Редактирование доступно только через прямой запрос к серверу.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.height(8.dp))
            Text(
                rrule,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskTypeSelector(
    selected: TaskType,
    onChange: (TaskType) -> Unit,
    enabled: Boolean
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TaskType.entries.forEach { type ->
            FilterChip(
                selected = selected == type,
                onClick = { onChange(type) },
                enabled = enabled,
                label = { Text(taskTypeLabel(type)) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportanceSelector(
    selected: Importance,
    onChange: (Importance) -> Unit,
    enabled: Boolean
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        Importance.entries.forEachIndexed { index, importance ->
            SegmentedButton(
                selected = selected == importance,
                onClick = { onChange(importance) },
                enabled = enabled,
                shape = SegmentedButtonDefaults.itemShape(index, Importance.entries.size)
            ) {
                Text(importanceLabel(importance))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PatternKindSelector(
    selected: PatternKind,
    onChange: (PatternKind) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PatternKind.entries.forEach { kind ->
            FilterChip(
                selected = selected == kind,
                onClick = { onChange(kind) },
                label = { Text(patternKindLabel(kind)) }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeekdaySelector(
    selected: Set<DayOfWeek>,
    onToggle: (DayOfWeek) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
        ).forEach { day ->
            FilterChip(
                selected = day in selected,
                onClick = { onToggle(day) },
                label = { Text(day.toShortRussian()) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initial: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initial.hour,
        initialMinute = initial.minute,
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите время") },
        text = { TimePicker(state = timePickerState) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(LocalTime.of(timePickerState.hour, timePickerState.minute))
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialogCompose(
    initial: LocalDate,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initial.atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli()
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val millis = datePickerState.selectedDateMillis ?: return@TextButton
                onConfirm(
                    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                )
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    ) {
        DatePicker(state = datePickerState)
    }
}

private fun taskTypeLabel(type: TaskType): String = when (type) {
    TaskType.MEDICATION -> "Лекарство"
    TaskType.PHYSICAL -> "Физическая активность"
    TaskType.SOCIAL -> "Социальное"
    TaskType.OTHER -> "Другое"
}

private fun importanceLabel(importance: Importance): String = when (importance) {
    Importance.LOW -> "Низкая"
    Importance.MEDIUM -> "Средняя"
    Importance.CRITICAL -> "Критическая"
}

private fun patternKindLabel(kind: PatternKind): String = when (kind) {
    PatternKind.DAILY -> "Каждый день"
    PatternKind.WEEKLY -> "Дни недели"
    PatternKind.EVERY_N_DAYS -> "Каждые N дней"
    PatternKind.ONCE -> "Однократно"
}