package ru.akuzyukhin.orientir.feature.task.ui.editor

import android.os.Build
import androidx.annotation.RequiresApi
import ru.akuzyukhin.orientir.core.ui.UiEvent
import ru.akuzyukhin.orientir.feature.task.domain.model.Importance
import ru.akuzyukhin.orientir.feature.task.domain.model.TaskType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@RequiresApi(Build.VERSION_CODES.O)
data class TaskEditorUiState constructor(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false,

    val name: String = "",
    val type: TaskType = TaskType.MEDICATION,
    val importance: Importance = Importance.MEDIUM,
    val scheduledTime: LocalTime = LocalTime.of(9, 0),
    val windowMinutes: Int = 30,

    val patternKind: PatternKind = PatternKind.DAILY,
    val weeklyDays: Set<DayOfWeek> = setOf(DayOfWeek.MONDAY),
    val intervalDays: Int = 2,
    val onceDate: LocalDate = LocalDate.now(),
    val customRrule: String? = null,
    val rruleStartDate: LocalDate? = null,

    val nameError: String? = null,
    val windowError: String? = null,
    val intervalError: String? = null,
    val errorMessage: String? = null
) {
    val isCustomMode: Boolean get() = customRrule != null
    val canSubmit: Boolean
        get() = !isSaving && !isLoading && name.isNotBlank() && nameError == null &&
                windowError == null && intervalError == null && !isCustomMode
}

enum class PatternKind {
    DAILY,
    WEEKLY,
    EVERY_N_DAYS,
    ONCE
}

sealed interface TaskEditorUiEvent : UiEvent {
    data object NavigateBackWithSuccess : TaskEditorUiEvent
}