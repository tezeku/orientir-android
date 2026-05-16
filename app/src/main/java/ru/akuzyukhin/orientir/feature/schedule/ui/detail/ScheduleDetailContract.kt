package ru.akuzyukhin.orientir.feature.schedule.ui.detail

import ru.akuzyukhin.orientir.core.ui.UiEvent
import ru.akuzyukhin.orientir.feature.schedule.domain.model.Schedule
import ru.akuzyukhin.orientir.feature.task.domain.model.Task

data class ScheduleDetailUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val schedule: Schedule? = null,
    val tasks: List<Task> = emptyList(),
    val errorMessage: String? = null,
    val taskToDelete: Task? = null
)

sealed interface ScheduleDetailUiEvent : UiEvent {
    data object NavigateToCreateTask : ScheduleDetailUiEvent
    data class NavigateToEditTask(val taskId: Long) : ScheduleDetailUiEvent
}
