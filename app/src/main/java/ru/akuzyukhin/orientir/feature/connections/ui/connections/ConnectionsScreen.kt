package ru.akuzyukhin.orientir.feature.connections.ui.connections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import ru.akuzyukhin.orientir.core.ui.CollectAsEffect
import ru.akuzyukhin.orientir.feature.auth.domain.model.Role
import ru.akuzyukhin.orientir.feature.connections.domain.model.CuratorSummary
import ru.akuzyukhin.orientir.feature.connections.domain.model.WardSummary
import ru.akuzyukhin.orientir.ui.theme.OrientirTheme

@Composable
fun ConnectionsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddWard: () -> Unit,
    onNavigateToWardDetail: (Long) -> Unit,
    viewModel: ConnectionsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.LaunchedEffect(lifecycleOwner.lifecycle) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.RESUMED) {
            if (viewModel.uiState.value.role != null) {
                viewModel.refresh()
            }
        }
    }

    CollectAsEffect(viewModel.events) { event ->
        when (event) {
            ConnectionsUiEvent.NavigateToAddWard -> onNavigateToAddWard()
            is ConnectionsUiEvent.NavigateToWardDetails -> onNavigateToWardDetail(event.wardId)
        }
    }

    ConnectionsContent(
        state = state,
        onBackClick = onNavigateBack,
        onRefresh = viewModel::refresh,
        onRetry = viewModel::retry,
        onAddWardClick = viewModel::onAddWardClick,
        onWardClick = viewModel::onWardClick,
        onWardRemoveClick = viewModel::onWardRemoveClick,
        onCancelRemove = viewModel::onCancelRemove,
        onConfirmRemove = viewModel::onConfirmRemove
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConnectionsContent(
    state: ConnectionsUiState,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onAddWardClick: () -> Unit,
    onWardClick: (WardSummary) -> Unit,
    onWardRemoveClick: (WardSummary) -> Unit,
    onCancelRemove: () -> Unit,
    onConfirmRemove: () -> Unit
) {
    val title = when (state.role) {
        Role.CURATOR -> "Мои подопечные"
        Role.WARD -> "Мои кураторы"
        null -> "Связи"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
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
            if (state.role == Role.CURATOR && !state.isLoading) {
                FloatingActionButton(onClick = onAddWardClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Добавить подопечного"
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    LoadingState()
                }
                state.errorMessage != null && state.role == null -> {
                    ErrorState(message = state.errorMessage, onRetry = onRetry)
                }
                else -> {
                    PullToRefreshBox(
                        isRefreshing = state.isRefreshing,
                        onRefresh = onRefresh,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        when {
                            state.isEmpty -> EmptyState(role = state.role)
                            state.role == Role.CURATOR -> WardsList(
                                wards = state.wards,
                                onWardClick = onWardClick,
                                onRemoveClick = onWardRemoveClick
                            )
                            state.role == Role.WARD -> CuratorsList(curators = state.curators)
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    if (state.wardToRemove != null) {
        RemoveConfirmDialog(
            ward = state.wardToRemove,
            isRemoving = state.isRemoving,
            onConfirm = onConfirmRemove,
            onDismiss = onCancelRemove
        )
    }
}

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Не удалось загрузить связи",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry) { Text("Повторить") }
    }
}

@Composable
private fun EmptyState(role: Role?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val (title, hint) = when (role) {
            Role.CURATOR -> "У вас пока нет подопечных" to
                    "Нажмите «+» внизу, чтобы привязать подопечного по номеру телефона"
            Role.WARD -> "К вам ещё не привязан куратор" to
                    "Куратор должен сам привязать вас по вашему номеру телефона"
            null -> "Нет данных" to ""
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = hint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun WardsList(
    wards: List<WardSummary>,
    onWardClick: (WardSummary) -> Unit,
    onRemoveClick: (WardSummary) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 16.dp, vertical = 12.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(wards, key = { it.id }) { ward ->
            WardCard(
                ward = ward,
                onClick = { onWardClick(ward) },
                onRemoveClick = { onRemoveClick(ward) }
            )
        }
    }
}

@Composable
private fun CuratorsList(curators: List<CuratorSummary>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 16.dp, vertical = 12.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(curators, key = { it.id }) { curator ->
            CuratorCard(curator = curator)
        }
    }
}

@Composable
private fun WardCard(
    ward: WardSummary,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(initials = ward.initials)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ward.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                IconLabel(
                    icon = Icons.Default.Phone,
                    text = ward.phoneNumber
                )
                if (!ward.address.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    IconLabel(
                        icon = Icons.Default.Home,
                        text = ward.address
                    )
                }
            }
            IconButton(onClick = onRemoveClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить связь",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun CuratorCard(curator: CuratorSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(initials = curator.initials)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = curator.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                IconLabel(icon = Icons.Default.Phone, text = curator.phoneNumber)
                Spacer(Modifier.height(2.dp))
                IconLabel(icon = Icons.Default.Email, text = curator.email)
            }
        }
    }
}

@Composable
private fun Avatar(initials: String) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun IconLabel(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RemoveConfirmDialog(
    ward: WardSummary,
    isRemoving: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isRemoving) onDismiss() },
        title = { Text("Отвязать подопечного?") },
        text = {
            Text(
                "Вы перестанете видеть расписание и задачи " +
                        "${ward.fullName}. Расписания самого подопечного не будут затронуты."
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isRemoving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                if (isRemoving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onError
                    )
                } else {
                    Text("Отвязать")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isRemoving) {
                Text("Отмена")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun ConnectionsContentCuratorPreview() {
    OrientirTheme {
        ConnectionsContent(
            state = ConnectionsUiState(
                role = Role.CURATOR,
                wards = listOf(
                    WardSummary(1, 2, "Иванов", "Иван", null,
                        "+79161234567", "г. Москва, ул. Ленина, 1"),
                    WardSummary(2, 3, "Петров", "Пётр", "Сергеевич",
                        "+79169876543", null)
                ),
                isLoading = false
            ),
            onBackClick = {}, onRefresh = {}, onRetry = {},
            onAddWardClick = {}, onWardClick = {}, onWardRemoveClick = {},
            onCancelRemove = {}, onConfirmRemove = {}
        )
    }
}

@Preview(showBackground = true, name = "Curator empty")
@Composable
private fun ConnectionsContentCuratorEmptyPreview() {
    OrientirTheme {
        ConnectionsContent(
            state = ConnectionsUiState(role = Role.CURATOR, isLoading = false),
            onBackClick = {}, onRefresh = {}, onRetry = {},
            onAddWardClick = {}, onWardClick = {}, onWardRemoveClick = {},
            onCancelRemove = {}, onConfirmRemove = {}
        )
    }
}

@Preview(showBackground = true, name = "Ward")
@Composable
private fun ConnectionsContentWardPreview() {
    OrientirTheme {
        ConnectionsContent(
            state = ConnectionsUiState(
                role = Role.WARD,
                curators = listOf(
                    CuratorSummary(1, 1, "Кузюхин", "Артемий", "Вячеславович",
                        "+79223334455", "kuzyukhin@example.com")
                ),
                isLoading = false
            ),
            onBackClick = {}, onRefresh = {}, onRetry = {},
            onAddWardClick = {}, onWardClick = {}, onWardRemoveClick = {},
            onCancelRemove = {}, onConfirmRemove = {}
        )
    }
}