package ru.akuzyukhin.orientir.feature.reminder.ui.debug

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.akuzyukhin.orientir.core.notification.NotificationPermissions
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Отладочная секция профиля для запуска тестового локального напоминания */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DebugReminderSection(
    viewModel: DebugReminderViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lastScheduledAt by viewModel.lastScheduledAt.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.scheduleTestReminder()
    }

    var showExactAlarmDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Отладка локальных уведомлений",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Запланировать тестовое уведомление через 5 секунд",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = {
                    when {
                        !NotificationPermissions.isPostNotificationsGranted(context) ->
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        !NotificationPermissions.canScheduleExactAlarms(context) ->
                            showExactAlarmDialog = true
                        else ->
                            viewModel.scheduleTestReminder()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Запланировать тестовое уведомление")
            }

            lastScheduledAt?.let { instant ->
                val formattedTime = remember(instant) {
                    DateTimeFormatter.ofPattern("HH:mm:ss")
                        .withZone(ZoneId.systemDefault())
                        .format(instant)
                }
                Text(
                    text = "Уведомление запланировано на $formattedTime",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (showExactAlarmDialog) {
        AlertDialog(
            onDismissRequest = { showExactAlarmDialog = false },
            title = { Text("Нужно разрешение") },
            text = {
                Text(
                    "Для точных напоминаний необходимо разрешение системы. " +
                            "Открыть настройки?"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    NotificationPermissions.openExactAlarmsSettings(context)
                    showExactAlarmDialog = false
                }) { Text("Открыть настройки") }
            },
            dismissButton = {
                TextButton(onClick = { showExactAlarmDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}