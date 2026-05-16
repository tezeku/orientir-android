package ru.akuzyukhin.orientir.feature.schedule.ui.detail

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
import ru.akuzyukhin.orientir.feature.schedule.domain.repository.SchedulesRepository
import ru.akuzyukhin.orientir.feature.task.domain.model.Task
import ru.akuzyukhin.orientir.feature.task.domain.repository.TasksRepository
import ru.akuzyukhin.orientir.navigation.HomeTabRoutes
import javax.inject.Inject

private const val REFRESH_MIN_DURATION_MS = 500L

@HiltViewModel
class ScheduleDetailViewModel @Inject constructor(
    private val schedulesRepository: SchedulesRepository,
    private val tasksRepository: TasksRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val wardId: Long = checkNotNull(savedStateHandle[HomeTabRoutes.WARD_ID_ARG]) {
        "wardId is required"
    }
    private val scheduleId: Long = checkNotNull(savedStateHandle[HomeTabRoutes.SCHEDULE_ID_ARG]) {
        "scheduleId is required"
    }

    private val _uiState = MutableStateFlow(ScheduleDetailUiState())
    val uiState: StateFlow<ScheduleDetailUiState> = _uiState.asStateFlow()

    private val _events = Channel<ScheduleDetailUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            fetchAll().also { errorOrNull ->
                _uiState.update { it.copy(isLoading = false, errorMessage = errorOrNull) }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val startTime = System.currentTimeMillis()
            val error = fetchAll()
            val elapsed = System.currentTimeMillis() - startTime
            if (elapsed < REFRESH_MIN_DURATION_MS) {
                delay(REFRESH_MIN_DURATION_MS - elapsed)
            }
            _uiState.update { it.copy(isRefreshing = false, errorMessage = error) }
        }
    }

    fun retry() = load()

    private suspend fun fetchAll(): String? {
        val scheduleResult = schedulesRepository.getScheduleByWard(wardId, scheduleId)
        val tasksResult = tasksRepository.getTasksBySchedule(wardId, scheduleId)

        val schedule = scheduleResult.getOrElse { return it.toUserMessage() }
        val tasks = tasksResult.getOrElse { return it.toUserMessage() }

        _uiState.update { it.copy(schedule = schedule, tasks = tasks) }
        return null
    }

    fun onAddTaskClick() {
        viewModelScope.launch { _events.send(ScheduleDetailUiEvent.NavigateToCreateTask) }
    }

    fun onTaskClick(task: Task) {
        viewModelScope.launch { _events.send(ScheduleDetailUiEvent.NavigateToEditTask(task.id)) }
    }

    fun onDeleteClick(task: Task) {
        _uiState.update { it.copy(taskToDelete = task) }
    }

    fun onDeleteCancel() {
        _uiState.update { it.copy(taskToDelete = null) }
    }

    fun onDeleteConfirm() {
        val task = _uiState.value.taskToDelete ?: return

        viewModelScope.launch {
            tasksRepository.deleteTask(wardId, scheduleId, task.id)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            tasks = it.tasks.filterNot { t -> t.id == task.id },
                            taskToDelete = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            taskToDelete = null,
                            errorMessage = error.toUserMessage()
                        )
                    }
                }
        }
    }
}