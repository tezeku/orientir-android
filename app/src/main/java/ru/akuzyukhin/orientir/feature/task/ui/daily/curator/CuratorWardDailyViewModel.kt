package ru.akuzyukhin.orientir.feature.task.ui.daily.curator

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.akuzyukhin.orientir.core.ui.toUserMessage
import ru.akuzyukhin.orientir.feature.task.domain.repository.TasksRepository
import ru.akuzyukhin.orientir.navigation.HomeTabRoutes
import java.time.LocalDate
import javax.inject.Inject
import kotlin.onFailure

private const val REFRESH_MIN_DURATION_MS = 500L

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class CuratorWardDailyViewModel @Inject constructor(
    private val tasksRepository: TasksRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val wardId: Long = checkNotNull(savedStateHandle[HomeTabRoutes.WARD_ID_ARG])

    @RequiresApi(Build.VERSION_CODES.O)
    private val _uiState = MutableStateFlow(CuratorWardDailyUiState())
    @RequiresApi(Build.VERSION_CODES.O)
    val uiState: StateFlow<CuratorWardDailyUiState> = _uiState.asStateFlow()

    init {
        load(LocalDate.now())
    }

    private fun load(date: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, date = date, errorMessage = null) }
            tasksRepository.getDailyTasksForWard(wardId, date)
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
            val result = tasksRepository.getDailyTasksForWard(wardId, _uiState.value.date)
            val elapsed = System.currentTimeMillis() - startTime
            if (elapsed < REFRESH_MIN_DURATION_MS) delay(REFRESH_MIN_DURATION_MS - elapsed)
            result
                .onSuccess { tasks ->
                    _uiState.update { it.copy(isRefreshing = false, tasks = tasks) }
                }
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
}