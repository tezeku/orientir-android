package ru.akuzyukhin.orientir.feature.schedule.ui.detail

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import ru.akuzyukhin.orientir.core.ui.CollectAsEffect
import ru.akuzyukhin.orientir.feature.task.domain.model.Importance
import ru.akuzyukhin.orientir.feature.task.domain.model.Task
import ru.akuzyukhin.orientir.feature.task.domain.model.TaskType
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun ScheduleDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreateTask: () -> Unit,
    onNavigateToEditTask: (Long) -> Unit,
    viewModel: ScheduleDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.LaunchedEffect(lifecycleOwner.lifecycle) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.RESUMED) {
            if (viewModel.uiState.value.schedule != null) {
                viewModel.refresh()
            }
        }
    }

    CollectAsEffect(viewModel.events) { event ->
        when (event) {
            ScheduleDetailUiEvent.NavigateToCreateTask -> onNavigateToCreateTask()
            is ScheduleDetailUiEvent.NavigateToEditTask -> onNavigateToEditTask(event.taskId)
        }
    }

    ScheduleDetailContent(
        state = state,
        onBackClick = onNavigateBack,
        onAddTaskClick = viewModel::onAddTaskClick,
        onTaskClick = viewModel::onTaskClick,
        onDeleteClick = viewModel::onDeleteClick,
        onRefresh = viewModel::refresh,
        onRetry = viewModel::retry,
        onDeleteConfirm = viewModel::onDeleteConfirm,
        onDeleteCancel = viewModel::onDeleteCancel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleDetailContent(
    state: ScheduleDetailUiState,
    onBackClick: () -> Unit,
    onAddTaskClick: () -> Unit,
    onTaskClick: (Task) -> Unit,
    onDeleteClick: (Task) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteCancel: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.schedule?.name ?: "Расписание",
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.schedule != null) {
                ExtendedFloatingActionButton(
                    onClick = onAddTaskClick,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Задача") }
                )
            }
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingState(padding)
            state.errorMessage != null && state.schedule == null ->
                ErrorState(padding, state.errorMessage, onRetry)
            state.tasks.isEmpty() -> EmptyState(padding)
            else -> ListState(
                padding = padding,
                state = state,
                onTaskClick = onTaskClick,
                onDeleteClick = onDeleteClick,
                onRefresh = onRefresh
            )
        }
    }

    if (state.taskToDelete != null) {
        DeleteTaskDialog(
            taskName = state.taskToDelete.name,
            onConfirm = onDeleteConfirm,
            onCancel = onDeleteCancel
        )
    }
}

@Composable
private fun LoadingState(padding: PaddingValues) {
    Box(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(padding: PaddingValues, message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Не удалось загрузить", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry) { Text("Повторить") }
    }
}

@Composable
private fun EmptyState(padding: PaddingValues) {
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Задач в расписании пока нет",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Нажмите + чтобы добавить первую задачу",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListState(
    padding: PaddingValues,
    state: ScheduleDetailUiState,
    onTaskClick: (Task) -> Unit,
    onDeleteClick: (Task) -> Unit,
    onRefresh: () -> Unit
) {
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize().padding(padding)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.tasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onClick = { onTaskClick(task) },
                    onDelete = { onDeleteClick(task) }
                )
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: Task,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TaskTypeIcon(task.type)
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${task.scheduledTime.format(TIME_FORMAT)} · ±${task.windowMinutes} мин",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            ImportanceBadge(task.importance)

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun TaskTypeIcon(type: TaskType) {
    val (icon, label) = when (type) {
        TaskType.MEDICATION -> Icons.Default.MedicalServices to "Лекарство"
        TaskType.PHYSICAL -> Icons.Default.SportsGymnastics to "Физическая активность"
        TaskType.SOCIAL -> Icons.Default.People to "Социальная активность"
        TaskType.OTHER -> Icons.Default.MoreHoriz to "Другое"
    }
    Icon(
        imageVector = icon,
        contentDescription = label,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(28.dp)
    )
}

@Composable
private fun ImportanceBadge(importance: Importance) {
    val (text, color) = when (importance) {
        Importance.LOW -> "LOW" to MaterialTheme.colorScheme.tertiary
        Importance.MEDIUM -> "MED" to MaterialTheme.colorScheme.primary
        Importance.CRITICAL -> "CRIT" to MaterialTheme.colorScheme.error
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun DeleteTaskDialog(
    taskName: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Удалить задачу?") },
        text = { Text("Задача «$taskName» будет удалена безвозвратно.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Удалить")
            }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Отмена") } }
    )
}