package ru.akuzyukhin.orientir.feature.task.ui.daily.curator

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import ru.akuzyukhin.orientir.feature.task.ui.daily.TaskStatusFilter
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.akuzyukhin.orientir.feature.task.domain.model.DailyTask
import ru.akuzyukhin.orientir.feature.task.domain.model.ExecutionStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale.forLanguageTag

@RequiresApi(Build.VERSION_CODES.O)
private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")
@RequiresApi(Build.VERSION_CODES.O)
private val DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, d MMMM", forLanguageTag("ru"))
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuratorWardDailyScreen(
    onNavigateBack: () -> Unit,
    viewModel: CuratorWardDailyViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("День подопечного") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                actions = {
                    if (state.date != LocalDate.now()) {
                        IconButton(onClick = viewModel::onGoToToday) {
                            Icon(Icons.Default.Today, "Сегодня")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            DateSelector(
                date = state.date,
                onPrev = viewModel::onPreviousDay,
                onNext = viewModel::onNextDay
            )

            when {
                state.isLoading -> LoadingBox()
                state.errorMessage != null && state.tasks.isEmpty() ->
                    ErrorBox(state.errorMessage!!, viewModel::retry)
                state.tasks.isEmpty() -> EmptyBox()
                else -> {
                    Spacer(Modifier.height(12.dp))
                    TaskSearchField(
                        query = state.searchQuery,
                        onQueryChange = viewModel::onSearchChange
                    )
                    StatusFilterChips(
                        selected = state.statusFilter,
                        onSelect = viewModel::onFilterChange
                    )
                    val filteredTasks = remember(state.tasks, state.statusFilter, state.searchQuery) {
                        state.tasks
                            .let { tasks ->
                                when (state.statusFilter) {
                                    TaskStatusFilter.ALL -> tasks
                                    TaskStatusFilter.ACTIVE -> tasks.filter {
                                        it.status == ExecutionStatus.PENDING
                                    }
                                    TaskStatusFilter.COMPLETED -> tasks.filter {
                                        it.status == ExecutionStatus.COMPLETED ||
                                                it.status == ExecutionStatus.COMPLETED_LATE
                                    }
                                    TaskStatusFilter.MISSED -> tasks.filter {
                                        it.status == ExecutionStatus.SKIPPED ||
                                                it.status == ExecutionStatus.OVERDUE ||
                                                it.status == ExecutionStatus.BLOCKED
                                    }
                                }
                            }
                            .let { tasks ->
                                if (state.searchQuery.isBlank()) tasks
                                else tasks.filter {
                                    it.taskName.contains(state.searchQuery, ignoreCase = true) ||
                                            it.scheduleName.contains(state.searchQuery, ignoreCase = true)
                                }
                            }
                    }
                    if (filteredTasks.isEmpty()) {
                        FilteredEmptyBox()
                    } else {
                        TaskList(state, filteredTasks, viewModel::refresh)
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DateSelector(date: LocalDate, onPrev: () -> Unit, onNext: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrev) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Предыдущий день")
            }
            Text(
                text = date.format(DATE_FORMAT).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            IconButton(onClick = onNext) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Следующий день")
            }
        }
    }
}

@Composable
private fun LoadingBox() = Box(Modifier.fillMaxSize(), Alignment.Center) {
    CircularProgressIndicator()
}

@Composable
private fun ErrorBox(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Не удалось загрузить", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry) { Text("Повторить") }
    }
}

@Composable
private fun EmptyBox() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.CheckCircle, null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("На этот день задач нет", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun FilteredEmptyBox() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Нет задач с таким статусом",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TaskSearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Поиск задач") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Очистить")
                }
            }
        } else null,
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )
    Spacer(Modifier.height(12.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusFilterChips(selected: TaskStatusFilter, onSelect: (TaskStatusFilter) -> Unit) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TaskStatusFilter.entries.forEach { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelect(filter) },
                label = { Text(filter.label) },
                leadingIcon = if (selected == filter) {
                    {
                        Icon(
                            imageVector = when (filter) {
                                TaskStatusFilter.ALL -> Icons.Default.FilterList
                                TaskStatusFilter.ACTIVE -> Icons.Default.Schedule
                                TaskStatusFilter.COMPLETED -> Icons.Default.CheckCircle
                                TaskStatusFilter.MISSED -> Icons.Default.Cancel
                            },
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else null,
                modifier = Modifier.height(44.dp)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskList(
    state: CuratorWardDailyUiState,
    tasks: List<DailyTask>,
    onRefresh: () -> Unit
) {
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tasks, key = { it.taskExecutionId }) { task ->
                DailyTaskReadOnlyCard(task = task)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DailyTaskReadOnlyCard(task: DailyTask) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = statusBg(task.status))
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            StatusIcon(task.status)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.taskName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${task.scheduledDateTime.toLocalTime().format(TIME_FORMAT)} · " +
                            "${task.scheduleName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = statusLabel(task.status),
                    style = MaterialTheme.typography.labelSmall,
                    color = statusFg(task.status),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun StatusIcon(status: ExecutionStatus) {
    val (icon, tint) = when (status) {
        ExecutionStatus.PENDING -> Icons.Default.Done to MaterialTheme.colorScheme.primary
        ExecutionStatus.COMPLETED -> Icons.Default.CheckCircle to Color(0xFF2E7D32)
        ExecutionStatus.COMPLETED_LATE -> Icons.Default.CheckCircle to Color(0xFFE65100)
        ExecutionStatus.SKIPPED -> Icons.Default.SkipNext to MaterialTheme.colorScheme.onSurfaceVariant
        ExecutionStatus.OVERDUE -> Icons.Default.Warning to MaterialTheme.colorScheme.error
        ExecutionStatus.BLOCKED -> Icons.Default.Block to Color(0xFFC62828)
    }
    Icon(icon, null, tint = tint, modifier = Modifier.size(32.dp))
}

private fun statusBg(status: ExecutionStatus): Color = when (status) {
    ExecutionStatus.PENDING -> Color.Transparent
    ExecutionStatus.COMPLETED -> Color(0xFFE8F5E9)
    ExecutionStatus.COMPLETED_LATE -> Color(0xFFFFF3E0)
    ExecutionStatus.SKIPPED -> Color(0xFFF5F5F5)
    ExecutionStatus.OVERDUE -> Color(0xFFFFEBEE)
    ExecutionStatus.BLOCKED -> Color(0xFFFCE4EC)
}

@Composable
private fun statusFg(status: ExecutionStatus): Color = when (status) {
    ExecutionStatus.PENDING -> MaterialTheme.colorScheme.primary
    ExecutionStatus.COMPLETED -> Color(0xFF2E7D32)
    ExecutionStatus.COMPLETED_LATE -> Color(0xFFE65100)
    ExecutionStatus.SKIPPED -> MaterialTheme.colorScheme.onSurfaceVariant
    ExecutionStatus.OVERDUE -> MaterialTheme.colorScheme.error
    ExecutionStatus.BLOCKED -> Color(0xFFC62828)
}

private fun statusLabel(status: ExecutionStatus): String = when (status) {
    ExecutionStatus.PENDING -> "Ожидает выполнения"
    ExecutionStatus.COMPLETED -> "Выполнено"
    ExecutionStatus.COMPLETED_LATE -> "Выполнено с опозданием"
    ExecutionStatus.SKIPPED -> "Пропущено"
    ExecutionStatus.OVERDUE -> "Просрочено"
    ExecutionStatus.BLOCKED -> "Не удалось выполнить"
}