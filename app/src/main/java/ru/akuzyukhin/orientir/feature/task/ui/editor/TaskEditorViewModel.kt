package ru.akuzyukhin.orientir.feature.task.ui.editor

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.akuzyukhin.orientir.core.ui.toUserMessage
import ru.akuzyukhin.orientir.feature.task.domain.model.Importance
import ru.akuzyukhin.orientir.feature.task.domain.model.RecurrencePattern
import ru.akuzyukhin.orientir.feature.task.domain.model.TaskType
import ru.akuzyukhin.orientir.feature.task.domain.repository.TasksRepository
import ru.akuzyukhin.orientir.feature.task.domain.util.RecurrenceRuleParser
import ru.akuzyukhin.orientir.navigation.HomeTabRoutes
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class TaskEditorViewModel @Inject constructor(
    private val tasksRepository: TasksRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val wardId: Long = checkNotNull(savedStateHandle[HomeTabRoutes.WARD_ID_ARG])
    private val scheduleId: Long = checkNotNull(savedStateHandle[HomeTabRoutes.SCHEDULE_ID_ARG])
    private val taskId: Long = checkNotNull(savedStateHandle[HomeTabRoutes.TASK_ID_ARG])

    @RequiresApi(Build.VERSION_CODES.O)
    private val _uiState = MutableStateFlow(TaskEditorUiState(isEditMode = taskId != -1L))
    @RequiresApi(Build.VERSION_CODES.O)
    val uiState: StateFlow<TaskEditorUiState> = _uiState.asStateFlow()

    private val _events = Channel<TaskEditorUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        if (taskId != -1L) loadTask()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadTask() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            tasksRepository.getTask(wardId, scheduleId, taskId)
                .onSuccess { task ->
                    val pattern = RecurrenceRuleParser.parse(task.rrule)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            name = task.name,
                            type = task.type,
                            importance = task.importance,
                            scheduledTime = task.scheduledTime,
                            windowMinutes = task.windowMinutes,
                            patternKind = when (pattern) {
                                is RecurrencePattern.Daily -> PatternKind.DAILY
                                is RecurrencePattern.Weekly -> PatternKind.WEEKLY
                                is RecurrencePattern.EveryNDays -> PatternKind.EVERY_N_DAYS
                                is RecurrencePattern.Once -> PatternKind.ONCE
                                is RecurrencePattern.Custom -> PatternKind.DAILY
                            },
                            weeklyDays = (pattern as? RecurrencePattern.Weekly)?.days ?: it.weeklyDays,
                            intervalDays = (pattern as? RecurrencePattern.EveryNDays)?.interval
                                ?: it.intervalDays,
                            onceDate = (pattern as? RecurrencePattern.Once)?.date ?: it.onceDate,
                            customRrule = (pattern as? RecurrencePattern.Custom)?.rrule
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.toUserMessage())
                    }
                }
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, nameError = null, errorMessage = null) }
    }

    fun onTypeChange(type: TaskType) {
        _uiState.update { it.copy(type = type) }
    }

    fun onImportanceChange(importance: Importance) {
        _uiState.update { it.copy(importance = importance) }
    }

    fun onTimeChange(time: LocalTime) {
        _uiState.update { it.copy(scheduledTime = time) }
    }

    fun onWindowChange(value: String) {
        val parsed = value.toIntOrNull()
        val error = when {
            value.isBlank() -> null
            parsed == null -> "Должно быть числом"
            parsed < 1 -> "Не меньше 1"
            parsed > 1440 -> "Не больше 1440 минут (24 ч)"
            else -> null
        }
        _uiState.update {
            it.copy(
                windowMinutes = parsed ?: it.windowMinutes,
                windowError = error
            )
        }
    }

    fun onPatternKindChange(kind: PatternKind) {
        _uiState.update { it.copy(patternKind = kind) }
    }

    fun onWeeklyDayToggle(day: DayOfWeek) {
        _uiState.update {
            val newDays = if (day in it.weeklyDays) it.weeklyDays - day else it.weeklyDays + day
            it.copy(weeklyDays = if (newDays.isEmpty()) it.weeklyDays else newDays)
        }
    }

    fun onIntervalChange(value: String) {
        val parsed = value.toIntOrNull()
        val error = when {
            value.isBlank() -> null
            parsed == null -> "Должно быть числом"
            parsed < 2 -> "Не меньше 2 (для 1 используйте «каждый день»)"
            parsed > 365 -> "Не больше 365"
            else -> null
        }
        _uiState.update {
            it.copy(
                intervalDays = parsed ?: it.intervalDays,
                intervalError = error
            )
        }
    }

    fun onOnceDateChange(date: LocalDate) {
        _uiState.update { it.copy(onceDate = date) }
    }

    fun onSave() {
        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Название обязательно") }
            return
        }

        val pattern = when (state.patternKind) {
            PatternKind.DAILY -> RecurrencePattern.Daily
            PatternKind.WEEKLY -> RecurrencePattern.Weekly(state.weeklyDays)
            PatternKind.EVERY_N_DAYS -> {
                if (state.intervalDays < 2) {
                    _uiState.update { it.copy(intervalError = "Не меньше 2") }
                    return
                }
                RecurrencePattern.EveryNDays(state.intervalDays)
            }
            PatternKind.ONCE -> RecurrencePattern.Once(state.onceDate)
        }
        val rrule = RecurrenceRuleParser.build(pattern, state.scheduledTime)

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            val result = if (state.isEditMode) {
                tasksRepository.updateTask(
                    wardId = wardId,
                    scheduleId = scheduleId,
                    taskId = taskId,
                    name = state.name.trim(),
                    type = state.type,
                    importance = state.importance,
                    rrule = rrule,
                    scheduledTime = state.scheduledTime,
                    windowMinutes = state.windowMinutes
                )
            } else {
                tasksRepository.createTask(
                    wardId = wardId,
                    scheduleId = scheduleId,
                    name = state.name.trim(),
                    type = state.type,
                    importance = state.importance,
                    rrule = rrule,
                    scheduledTime = state.scheduledTime,
                    windowMinutes = state.windowMinutes
                )
            }

            result
                .onSuccess { _events.send(TaskEditorUiEvent.NavigateBackWithSuccess) }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isSaving = false, errorMessage = error.toUserMessage())
                    }
                }
        }
    }
}