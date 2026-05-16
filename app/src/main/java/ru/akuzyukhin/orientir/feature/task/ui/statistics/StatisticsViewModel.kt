package ru.akuzyukhin.orientir.feature.task.ui.statistics

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.akuzyukhin.orientir.core.data.storage.TokenStorage
import ru.akuzyukhin.orientir.core.ui.toUserMessage
import ru.akuzyukhin.orientir.feature.auth.domain.model.Role
import ru.akuzyukhin.orientir.feature.task.domain.repository.TasksRepository
import java.time.LocalDate
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val tasksRepository: TasksRepository,
    tokenStorage: TokenStorage
) : ViewModel() {

    val role: StateFlow<Role?> = tokenStorage.roleFlow
        .map { it?.let { Role.fromString(it) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            role.collect { current ->
                if (current == Role.WARD) load(_uiState.value.period)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onPeriodChange(period: StatisticsPeriod) {
        if (period == _uiState.value.period) return
        load(period)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun retry() = load(_uiState.value.period)

    @RequiresApi(Build.VERSION_CODES.O)
    private fun load(period: StatisticsPeriod) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, period = period, errorMessage = null) }

            val today = LocalDate.now()
            val dates = (0 until period.days).map { today.minusDays(it.toLong()) }

            try {
                val byStatus = coroutineScope {
                    dates
                        .map { date -> async { tasksRepository.getMyDailyTasks(date) } }
                        .awaitAll()
                }
                    .flatMap { it.getOrNull().orEmpty() }
                    .groupBy { it.status }
                    .mapValues { it.value.size }

                val total = byStatus.values.sum()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        counters = StatisticsCounters(total = total, byStatus = byStatus)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.toUserMessage())
                }
            }
        }
    }
}