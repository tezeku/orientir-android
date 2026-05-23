package ru.akuzyukhin.orientir.feature.task.ui.daily.curator

import android.os.Build
import androidx.annotation.RequiresApi
import ru.akuzyukhin.orientir.feature.task.domain.model.DailyTask
import ru.akuzyukhin.orientir.feature.task.ui.daily.TaskStatusFilter
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
data class CuratorWardDailyUiState constructor(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val date: LocalDate = LocalDate.now(),
    val tasks: List<DailyTask> = emptyList(),
    val errorMessage: String? = null,
    val statusFilter: TaskStatusFilter = TaskStatusFilter.ALL,
    val searchQuery: String = ""
)