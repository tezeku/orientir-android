package ru.akuzyukhin.orientir.feature.task.ui.daily

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.akuzyukhin.orientir.core.data.storage.TokenStorage
import ru.akuzyukhin.orientir.core.ui.toUserMessage
import ru.akuzyukhin.orientir.feature.monitoring.domain.repository.MonitoringRepository
import ru.akuzyukhin.orientir.feature.task.domain.model.DailyTask
import ru.akuzyukhin.orientir.feature.task.domain.repository.TasksRepository
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.stateIn
import ru.akuzyukhin.orientir.feature.auth.domain.model.Role

private const val REFRESH_MIN_DURATION_MS = 500L

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class MyDailyTasksViewModel @Inject constructor(
    private val tasksRepository: TasksRepository,
    private val monitoringRepository: MonitoringRepository,
    tokenStorage: TokenStorage
) : ViewModel() {

    val role: StateFlow<Role?> = tokenStorage.roleEnumFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @RequiresApi(Build.VERSION_CODES.O)
    private val _uiState = MutableStateFlow(MyDailyTasksUiState())
    val uiState: StateFlow<MyDailyTasksUiState> = _uiState.asStateFlow()

    init {
        load(LocalDate.now())
    }

    private fun load(date: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, date = date, errorMessage = null) }
            tasksRepository.getMyDailyTasks(date)
                .onSuccess { tasks ->
                    _uiState.update { it.copy(isLoading = false, tasks = tasks) }
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
            val result = tasksRepository.getMyDailyTasks(_uiState.value.date)
            val elapsed = System.currentTimeMillis() - startTime
            if (elapsed < REFRESH_MIN_DURATION_MS) delay(REFRESH_MIN_DURATION_MS - elapsed)
            result
                .onSuccess { tasks -> _uiState.update { it.copy(isRefreshing = false, tasks = tasks) } }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isRefreshing = false, errorMessage = error.toUserMessage())
                    }
                }
        }
    }

    fun retry() = load(_uiState.value.date)

    fun onPreviousDay() = load(_uiState.value.date.minusDays(1))
    fun onNextDay() = load(_uiState.value.date.plusDays(1))
    fun onGoToToday() = load(LocalDate.now())

    fun onTaskClick(task: DailyTask) {
        _uiState.update { it.copy(selectedTask = task) }
    }

    fun onDismissActions() {
        _uiState.update { it.copy(selectedTask = null) }
    }

    fun onComplete(task: DailyTask) {
        executeAction(task.taskExecutionId) {
            monitoringRepository.complete(task.taskExecutionId)
        }
    }

    fun onSkip(task: DailyTask) {
        executeAction(task.taskExecutionId) {
            monitoringRepository.skip(task.taskExecutionId)
        }
    }

    fun onBlockClick(task: DailyTask) {
        _uiState.update {
            it.copy(blockingTask = task, blockComment = "", selectedTask = null)
        }
    }

    fun onBlockCommentChange(value: String) {
        _uiState.update { it.copy(blockComment = value) }
    }

    fun onBlockConfirm() {
        val task = _uiState.value.blockingTask ?: return
        val comment = _uiState.value.blockComment.trim().ifBlank { null }
        _uiState.update { it.copy(blockingTask = null, blockComment = "") }
        executeAction(task.taskExecutionId) {
            monitoringRepository.block(task.taskExecutionId, comment)
        }
    }

    fun onBlockCancel() {
        _uiState.update { it.copy(blockingTask = null, blockComment = "") }
    }

    private fun executeAction(
        taskExecutionId: Long,
        action: suspend () -> Result<ru.akuzyukhin.orientir.feature.monitoring.domain.model.TaskExecution>
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(taskInProgress = taskExecutionId, selectedTask = null) }

            action()
                .onSuccess { execution ->
                    _uiState.update { current ->
                        current.copy(
                            taskInProgress = null,
                            tasks = current.tasks.map { task ->
                                if (task.taskExecutionId == execution.id) {
                                    task.copy(
                                        status = execution.status,
                                        executionTime = execution.executionTime,
                                        deviationMinutes = execution.deviationMinutes,
                                        isWithinWindow = execution.isWithinWindow
                                    )
                                } else task
                            }
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(taskInProgress = null, errorMessage = error.toUserMessage())
                    }
                }
        }
    }
}