package ru.akuzyukhin.orientir.feature.task.ui.daily

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ru.akuzyukhin.orientir.feature.auth.domain.model.Role
import ru.akuzyukhin.orientir.feature.schedule.ui.curator_list.CuratorScheduleListContent

/** Точка входа для таба «Расписание» */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScheduleTabRouterScreen(
    onNavigateToWardSchedules: (Long) -> Unit
) {
    val viewModel: MyDailyTasksViewModel = hiltViewModel()
    val role by viewModel.role.collectAsState()

    when (role) {
        Role.WARD -> MyDailyTasksScreen(viewModel = viewModel)
        Role.CURATOR -> CuratorScheduleListContent(onWardClick = onNavigateToWardSchedules)
        null -> Unit
    }
}