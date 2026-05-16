package ru.akuzyukhin.orientir.feature.schedule.ui.list

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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import ru.akuzyukhin.orientir.core.ui.CollectAsEffect
import ru.akuzyukhin.orientir.feature.schedule.domain.model.Schedule

@Composable
fun SchedulesListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToScheduleDetail: (Long) -> Unit,
    onNavigateToDaily: () -> Unit,
    viewModel: SchedulesListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CollectAsEffect(viewModel.events) { event ->
        when (event) {
            is SchedulesListUiEvent.NavigateToScheduleDetail ->
                onNavigateToScheduleDetail(event.scheduleId)
        }
    }

    SchedulesListContent(
        state = state,
        onBackClick = onNavigateBack,
        onScheduleClick = viewModel::onScheduleClick,
        onEditClick = viewModel::onOpenEditDialog,
        onDeleteClick = viewModel::onDeleteClick,
        onAddClick = viewModel::onOpenCreateDialog,
        onRefresh = viewModel::refresh,
        onRetry = viewModel::retry,
        onDialogNameChange = viewModel::onDialogNameChanged,
        onDialogConfirm = viewModel::onDialogConfirm,
        onDialogDismiss = viewModel::onDialogDismiss,
        onDeleteConfirm = viewModel::onDeleteConfirm,
        onDeleteCancel = viewModel::onDeleteCancel,
        onNavigateToDaily = onNavigateToDaily
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SchedulesListContent(
    state: SchedulesListUiState,
    onBackClick: () -> Unit,
    onScheduleClick: (Schedule) -> Unit,
    onEditClick: (Schedule) -> Unit,
    onDeleteClick: (Schedule) -> Unit,
    onAddClick: () -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onDialogNameChange: (String) -> Unit,
    onDialogConfirm: () -> Unit,
    onDialogDismiss: () -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteCancel: () -> Unit,
    onNavigateToDaily: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Расписания подопечного") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToDaily) {
                        Icon(Icons.Default.Today, contentDescription = "Просмотр дня")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Расписание") }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingState(padding)
            state.errorMessage != null && state.schedules.isEmpty() ->
                ErrorState(padding, state.errorMessage, onRetry)
            state.schedules.isEmpty() -> EmptyState(padding)
            else -> ListState(
                padding = padding,
                state = state,
                onScheduleClick = onScheduleClick,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
                onRefresh = onRefresh
            )
        }
    }

    if (state.dialog.isOpen) {
        ScheduleEditDialog(
            state = state.dialog,
            onNameChange = onDialogNameChange,
            onConfirm = onDialogConfirm,
            onDismiss = onDialogDismiss
        )
    }

    if (state.scheduleToDelete != null) {
        DeleteConfirmDialog(
            scheduleName = state.scheduleToDelete.name,
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
        Text(
            text = "Не удалось загрузить расписания",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Расписаний пока нет",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Нажмите + чтобы создать первое расписание",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListState(
    padding: PaddingValues,
    state: SchedulesListUiState,
    onScheduleClick: (Schedule) -> Unit,
    onEditClick: (Schedule) -> Unit,
    onDeleteClick: (Schedule) -> Unit,
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
            items(state.schedules, key = { it.id }) { schedule ->
                ScheduleCard(
                    schedule = schedule,
                    onClick = { onScheduleClick(schedule) },
                    onEdit = { onEditClick(schedule) },
                    onDelete = { onDeleteClick(schedule) }
                )
            }
        }
    }
}

@Composable
private fun ScheduleCard(
    schedule: Schedule,
    onClick: () -> Unit,
    onEdit: () -> Unit,
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
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = schedule.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Редактировать",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
private fun ScheduleEditDialog(
    state: ScheduleDialogState,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!state.isSubmitting) onDismiss() },
        title = {
            Text(
                if (state.editingSchedule != null) "Редактирование расписания"
                else "Новое расписание"
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = state.nameInput,
                    onValueChange = onNameChange,
                    label = { Text("Название") },
                    placeholder = { Text("Например: Утренние процедуры") },
                    singleLine = true,
                    isError = state.errorMessage != null,
                    supportingText = state.errorMessage?.let { { Text(it) } },
                    enabled = !state.isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !state.isSubmitting && state.nameInput.isNotBlank()
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (state.editingSchedule != null) "Сохранить" else "Создать")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !state.isSubmitting) {
                Text("Отмена")
            }
        }
    )
}

@Composable
private fun DeleteConfirmDialog(
    scheduleName: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Удалить расписание?") },
        text = {
            Text("Расписание «$scheduleName» и все его задачи будут удалены безвозвратно.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Удалить")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Отмена") }
        }
    )
}