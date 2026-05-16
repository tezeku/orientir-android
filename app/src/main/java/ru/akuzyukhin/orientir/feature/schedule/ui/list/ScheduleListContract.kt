package ru.akuzyukhin.orientir.feature.schedule.ui.list

import ru.akuzyukhin.orientir.core.ui.UiEvent
import ru.akuzyukhin.orientir.feature.schedule.domain.model.Schedule

/** Состояние диалога создания/редактирования расписания */
data class ScheduleDialogState(
    val isOpen: Boolean = false,
    val editingSchedule: Schedule? = null,
    val nameInput: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

/** UI-состояние экрана списка расписаний */
data class SchedulesListUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val schedules: List<Schedule> = emptyList(),
    val errorMessage: String? = null,
    val dialog: ScheduleDialogState = ScheduleDialogState(),
    val scheduleToDelete: Schedule? = null
)

sealed interface SchedulesListUiEvent : UiEvent {
    data class NavigateToScheduleDetail(val scheduleId: Long) : SchedulesListUiEvent
}
