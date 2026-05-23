package ru.akuzyukhin.orientir.feature.profile.ui.notification_settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    role: String,
    curatorEmail: String?,
    onEditProfileClick: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки уведомлений") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SectionPushNotifications(context)
            SectionChannels()
            if (role == "CURATOR" && curatorEmail != null) {
                SectionEmail(curatorEmail = curatorEmail, onEditProfileClick = onEditProfileClick)
            }
        }
    }
}

@Composable
private fun SectionPushNotifications(context: Context) {
    Column {
        SectionHeader("PUSH-УВЕДОМЛЕНИЯ")
        Spacer(Modifier.height(8.dp))
        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
        ) {
            ListItem(
                headlineContent = { Text("Системные настройки") },
                supportingContent = {
                    Text("Звук, вибрация и важность каналов настраиваются в Android.")
                },
                leadingContent = {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null)
                },
                trailingContent = {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                },
                modifier = Modifier.clickable {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
private fun SectionChannels() {
    Column {
        SectionHeader("КАНАЛЫ В ПРИЛОЖЕНИИ")
        Spacer(Modifier.height(8.dp))
        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
        ) {
            Column {
                ListItem(
                    headlineContent = { Text("Напоминания о задачах") },
                    supportingContent = {
                        Text("Стандартные напоминания о задачах из расписания")
                    },
                    leadingContent = {
                        Icon(Icons.Default.Notifications, contentDescription = null)
                    }
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("Критические задачи") },
                    supportingContent = {
                        Text("Heads-up уведомления для задач высокой важности")
                    },
                    leadingContent = {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null)
                    }
                )
            }
        }
    }
}

@Composable
private fun SectionEmail(curatorEmail: String, onEditProfileClick: () -> Unit) {
    Column {
        SectionHeader("EMAIL-УВЕДОМЛЕНИЯ")
        Spacer(Modifier.height(8.dp))
        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
        ) {
            ListItem(
                headlineContent = { Text(curatorEmail) },
                supportingContent = { Text("Изменить в Профиле") },
                leadingContent = {
                    Icon(Icons.Default.Email, contentDescription = null)
                },
                trailingContent = {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                },
                modifier = Modifier.clickable(onClick = onEditProfileClick)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Письмо приходит при:",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "• Блокировке задачи подопечным",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "• Пропуске критически важной задачи",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "• Превышении порога отклонений",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp)
    )
}
