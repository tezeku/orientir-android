package ru.akuzyukhin.orientir.feature.schedule.ui.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.akuzyukhin.orientir.core.ui.toUserMessage
import ru.akuzyukhin.orientir.feature.schedule.domain.model.Schedule
import ru.akuzyukhin.orientir.feature.schedule.domain.repository.SchedulesRepository
import ru.akuzyukhin.orientir.navigation.HomeTabRoutes
import javax.inject.Inject

private const val REFRESH_MIN_DURATION_MS = 500L

/** ViewModel списка расписаний подопечного (куратор) */
@HiltViewModel
class SchedulesListViewModel @Inject constructor(
    private val schedulesRepository: SchedulesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val wardId: Long = checkNotNull(savedStateHandle[HomeTabRoutes.WARD_ID_ARG]) {
        "wardId is required"
    }

    private val _uiState = MutableStateFlow(SchedulesListUiState())
    val uiState: StateFlow<SchedulesListUiState> = _uiState.asStateFlow()

    private val _events = Channel<SchedulesListUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadSchedules()
    }

    private fun loadSchedules() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            schedulesRepository.getSchedulesByWard(wardId)
                .onSuccess { schedules ->
                    _uiState.update {
                        it.copy(isLoading = false, schedules = schedules, errorMessage = null)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.toUserMessage())
                    }
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val startTime = System.currentTimeMillis()

            val result = schedulesRepository.getSchedulesByWard(wardId)

            val elapsed = System.currentTimeMillis() - startTime
            if (elapsed < REFRESH_MIN_DURATION_MS) {
                delay(REFRESH_MIN_DURATION_MS - elapsed)
            }

            result
                .onSuccess { schedules ->
                    _uiState.update {
                        it.copy(isRefreshing = false, schedules = schedules, errorMessage = null)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isRefreshing = false, errorMessage = error.toUserMessage())
                    }
                }
        }
    }

    fun retry() = loadSchedules()

    fun onScheduleClick(schedule: Schedule) {
        viewModelScope.launch {
            _events.send(SchedulesListUiEvent.NavigateToScheduleDetail(schedule.id))
        }
    }

    fun onOpenCreateDialog() {
        _uiState.update {
            it.copy(dialog = ScheduleDialogState(isOpen = true, editingSchedule = null))
        }
    }

    fun onOpenEditDialog(schedule: Schedule) {
        _uiState.update {
            it.copy(
                dialog = ScheduleDialogState(
                    isOpen = true,
                    editingSchedule = schedule,
                    nameInput = schedule.name
                )
            )
        }
    }

    fun onDialogNameChanged(value: String) {
        _uiState.update {
            it.copy(dialog = it.dialog.copy(nameInput = value, errorMessage = null))
        }
    }

    fun onDialogDismiss() {
        _uiState.update { it.copy(dialog = ScheduleDialogState()) }
    }

    fun onDialogConfirm() {
        val state = _uiState.value.dialog
        val name = state.nameInput.trim()

        if (name.isBlank()) {
            _uiState.update {
                it.copy(dialog = it.dialog.copy(errorMessage = "Введите название"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(dialog = it.dialog.copy(isSubmitting = true, errorMessage = null))
            }

            val result = if (state.editingSchedule != null) {
                schedulesRepository.updateScheduleForWard(wardId, state.editingSchedule.id, name)
            } else {
                schedulesRepository.createScheduleForWard(wardId, name)
            }

            result
                .onSuccess { saved ->
                    _uiState.update { current ->
                        val updatedList = if (state.editingSchedule != null) {
                            current.schedules.map { if (it.id == saved.id) saved else it }
                        } else {
                            current.schedules + saved
                        }
                        current.copy(
                            schedules = updatedList,
                            dialog = ScheduleDialogState()
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            dialog = it.dialog.copy(
                                isSubmitting = false,
                                errorMessage = error.toUserMessage()
                            )
                        )
                    }
                }
        }
    }

    fun onDeleteClick(schedule: Schedule) {
        _uiState.update { it.copy(scheduleToDelete = schedule) }
    }

    fun onDeleteCancel() {
        _uiState.update { it.copy(scheduleToDelete = null) }
    }

    fun onStatisticsClick() {
        viewModelScope.launch { _events.send(SchedulesListUiEvent.NavigateToStatistics(wardId)) }
    }

    fun onThresholdsClick() {
        viewModelScope.launch { _events.send(SchedulesListUiEvent.NavigateToThresholds(wardId)) }
    }

    fun onDeleteConfirm() {
        val schedule = _uiState.value.scheduleToDelete ?: return

        viewModelScope.launch {
            schedulesRepository.deleteScheduleForWard(wardId, schedule.id)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            schedules = it.schedules.filterNot { s -> s.id == schedule.id },
                            scheduleToDelete = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            scheduleToDelete = null,
                            errorMessage = error.toUserMessage()
                        )
                    }
                }
        }
    }
}