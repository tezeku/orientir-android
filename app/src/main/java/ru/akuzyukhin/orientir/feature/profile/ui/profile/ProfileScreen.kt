package ru.akuzyukhin.orientir.feature.profile.ui.profile

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import ru.akuzyukhin.orientir.core.accessibility.domain.model.AccessibilityProfile
import ru.akuzyukhin.orientir.core.accessibility.ui.AccessibilityViewModel
import ru.akuzyukhin.orientir.core.ui.CollectAsEffect
import ru.akuzyukhin.orientir.feature.auth.domain.model.Role
import ru.akuzyukhin.orientir.feature.profile.domain.model.Profile
import ru.akuzyukhin.orientir.ui.theme.OrientirTheme

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileScreen(
    onNavigateToEdit: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToConnections: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onNavigateToAccessibilitySettings: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val accessibilityVm: AccessibilityViewModel = hiltViewModel()
    val accessibilityProfile by accessibilityVm.profile.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner.lifecycle) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            if (viewModel.uiState.value.profile != null) {
                viewModel.refresh()
            }
        }
    }


    CollectAsEffect(viewModel.events) { event ->
        when (event) {
            ProfileUiEvent.NavigateToEdit -> onNavigateToEdit()
            ProfileUiEvent.NavigateToChangePassword -> onNavigateToChangePassword()
            ProfileUiEvent.NavigateToConnections -> onNavigateToConnections()
            ProfileUiEvent.NavigateToLogin -> onNavigateToLogin()
        }
    }

    ProfileContent(
        state = state,
        onEditClick = viewModel::onEditClick,
        onChangePasswordClick = viewModel::onChangePasswordClick,
        onConnectionsClick = viewModel::onConnectionsClick,
        onLogoutClick = viewModel::onLogoutClick,
        onRetry = viewModel::retry,
        onRefresh = viewModel::refresh,
        onNotificationsClick = onNavigateToNotificationSettings,
        onAccessibilityClick = onNavigateToAccessibilitySettings,
        accessibilityDisplayName = accessibilityProfile.displayName
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(
    state: ProfileUiState,
    onEditClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onConnectionsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onNotificationsClick: () -> Unit,
    onAccessibilityClick: () -> Unit = {},
    accessibilityDisplayName: String = AccessibilityProfile.STANDARD.displayName
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                actions = {
                    if (state.profile != null && !state.isRefreshing) {
                        IconButton(onClick = onRefresh) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Обновить"
                            )
                        }
                    }
                    if (state.isRefreshing) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                    if (state.profile != null) {
                        IconButton(onClick = onEditClick) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Редактировать"
                            )
                        }
                    }
                }
            )
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
                state.errorMessage != null && state.profile == null -> {
                    ErrorState(
                        message = state.errorMessage,
                        onRetry = onRetry
                    )
                }
                state.profile != null -> {
                    LoadedState(
                        profile = state.profile,
                        isLoggingOut = state.isLoggingOut,
                        onChangePasswordClick = onChangePasswordClick,
                        onConnectionsClick = onConnectionsClick,
                        onLogoutClick = onLogoutClick,
                        onNotificationsClick = onNotificationsClick,
                        onAccessibilityClick = onAccessibilityClick,
                        accessibilityDisplayName = accessibilityDisplayName
                    )
                }
            }
        }
    }
}

// Состояния экрана
@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Не удалось загрузить профиль",
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
        Button(onClick = onRetry) {
            Text("Повторить")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun LoadedState(
    profile: Profile,
    isLoggingOut: Boolean,
    onChangePasswordClick: () -> Unit,
    onConnectionsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onAccessibilityClick: () -> Unit,
    accessibilityDisplayName: String
) {
    var showAboutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(16.dp))
            ProfileHeader(profile = profile)
            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onChangePasswordClick,
                    enabled = !isLoggingOut,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Lock, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Пароль")
                }
                OutlinedButton(
                    onClick = onLogoutClick,
                    enabled = !isLoggingOut,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (isLoggingOut) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Logout, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Выйти")
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
        }

        HorizontalDivider()

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(20.dp))

            InfoRow(icon = Icons.Default.Phone, label = "Телефон", value = profile.phoneNumber)

            if (profile.role == Role.CURATOR && !profile.email.isNullOrBlank()) {
                Spacer(Modifier.height(16.dp))
                InfoRow(icon = Icons.Default.Email, label = "Email", value = profile.email!!)
            }

            if (profile.role == Role.WARD && !profile.address.isNullOrBlank()) {
                Spacer(Modifier.height(16.dp))
                InfoRow(icon = Icons.Default.Home, label = "Адрес", value = profile.address!!)
            }

            Spacer(Modifier.height(20.dp))
        }

        HorizontalDivider()

        ListItem(
            headlineContent = {
                Text(
                    when (profile.role) {
                        Role.CURATOR -> "Мои подопечные"
                        Role.WARD -> "Мои кураторы"
                    }
                )
            },
            leadingContent = { Icon(Icons.Default.People, contentDescription = null) },
            trailingContent = {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            },
            modifier = Modifier.clickable(enabled = !isLoggingOut, onClick = onConnectionsClick)
        )

        HorizontalDivider()

        ListItem(
            headlineContent = { Text("Размер интерфейса") },
            leadingContent = { Icon(Icons.Default.TextFields, contentDescription = null) },
            supportingContent = { Text(accessibilityDisplayName) },
            trailingContent = {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            },
            modifier = Modifier.clickable(onClick = onAccessibilityClick)
        )

        HorizontalDivider()

        ListItem(
            headlineContent = { Text("Уведомления") },
            leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
            trailingContent = {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            },
            modifier = Modifier.clickable(onClick = onNotificationsClick)
        )

        ListItem(
            headlineContent = { Text("О приложении") },
            leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
            trailingContent = {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            },
            modifier = Modifier.clickable { showAboutDialog = true }
        )

        HorizontalDivider()

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Версия 1.0.0",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("Ориентир") },
            text = {
                Text(
                    "Приложение для мониторинга расписания людей с особыми потребностями. " +
                            "\n\nКураторы составляют расписания задач, подопечные выполняют их и отмечают результат. " +
                            "\nСистема отслеживает отклонения и уведомляет куратора о критических ситуациях."
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("OK") }
            }
        )
    }
}

// Подкомпоненты
@Composable
private fun ProfileHeader(profile: Profile) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = profile.initials(),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = profile.fullName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = roleDisplayName(profile.role),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

private fun Profile.initials(): String {
    val s = surname.firstOrNull()?.uppercase() ?: ""
    val n = name.firstOrNull()?.uppercase() ?: ""
    return "$s$n".ifBlank { "?" }
}

private fun roleDisplayName(role: Role): String = when (role) {
    Role.CURATOR -> "Куратор"
    Role.WARD -> "Подопечный"
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
private fun ProfileContentLoadedPreview() {
    OrientirTheme {
        ProfileContent(
            state = ProfileUiState(
                profile = Profile(
                    id = 1,
                    surname = "Кузюхин",
                    name = "Артемий",
                    patronymic = "Вячеславович",
                    phoneNumber = "+79223334455",
                    role = Role.CURATOR,
                    isActive = true,
                    email = "ivan@mail.ru",
                    address = null
                ),
                isLoading = false
            ),
            onEditClick = {}, onChangePasswordClick = {},
            onConnectionsClick = {},
            onLogoutClick = {}, onRetry = {}, onRefresh = {}, onNotificationsClick = {}, onAccessibilityClick = {}
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "Loading")
@Composable
private fun ProfileContentLoadingPreview() {
    OrientirTheme {
        ProfileContent(
            state = ProfileUiState(isLoading = true),
            onEditClick = {}, onChangePasswordClick = {},
            onConnectionsClick = {},
            onLogoutClick = {}, onRetry = {}, onRefresh = {}, onNotificationsClick = {}, onAccessibilityClick = {}
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "Error")
@Composable
private fun ProfileContentErrorPreview() {
    OrientirTheme {
        ProfileContent(
            state = ProfileUiState(
                isLoading = false,
                errorMessage = "Нет соединения с сервером. Проверьте интернет."
            ),
            onEditClick = {}, onChangePasswordClick = {},
            onConnectionsClick = {},
            onLogoutClick = {}, onRetry = {}, onRefresh = {}, onNotificationsClick = {}, onAccessibilityClick = {}
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "Ward")
@Composable
private fun ProfileContentWardPreview() {
    OrientirTheme {
        ProfileContent(
            state = ProfileUiState(
                profile = Profile(
                    id = 2,
                    surname = "Иванов",
                    name = "Иван",
                    patronymic = null,
                    phoneNumber = "+79334445566",
                    role = Role.WARD,
                    isActive = true,
                    email = null,
                    address = "г. Москва, ул. Московская, д. 1"
                ),
                isLoading = false
            ),
            onEditClick = {}, onChangePasswordClick = {},
            onConnectionsClick = {},
            onLogoutClick = {}, onRetry = {}, onRefresh = {}, onNotificationsClick = {}, onAccessibilityClick = {}
        )
    }
}