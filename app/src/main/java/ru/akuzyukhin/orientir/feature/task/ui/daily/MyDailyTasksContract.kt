package ru.akuzyukhin.orientir.feature.task.ui.daily

import android.os.Build
import androidx.annotation.RequiresApi
import ru.akuzyukhin.orientir.feature.task.domain.model.DailyTask
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
data class MyDailyTasksUiState constructor(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val date: LocalDate = LocalDate.now(),
    val tasks: List<DailyTask> = emptyList(),
    val errorMessage: String? = null,

    val selectedTask: DailyTask? = null,
    val taskInProgress: Long? = null,
    val blockingTask: DailyTask? = null,
    val blockComment: String = ""
)
