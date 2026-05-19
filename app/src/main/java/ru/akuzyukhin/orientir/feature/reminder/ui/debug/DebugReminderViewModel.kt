package ru.akuzyukhin.orientir.feature.reminder.ui.debug

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.akuzyukhin.orientir.feature.reminder.domain.model.ReminderInfo
import ru.akuzyukhin.orientir.feature.reminder.domain.repository.ReminderRepository
import ru.akuzyukhin.orientir.feature.task.domain.model.Importance
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/** Временный ViewModel для отладочного запуска локального напоминания */
@HiltViewModel
class DebugReminderViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    private val _lastScheduledAt = MutableStateFlow<Instant?>(null)
    val lastScheduledAt: StateFlow<Instant?> = _lastScheduledAt.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleTestReminder(delaySeconds: Long = 5L) {
        val scheduledAt = Instant.now().plus(delaySeconds, ChronoUnit.SECONDS)
        viewModelScope.launch {
            reminderRepository.schedule(
                ReminderInfo(
                    taskExecutionId = TEST_REMINDER_ID,
                    taskName = "Тестовое уведомление",
                    importance = Importance.MEDIUM,
                    scheduledAt = scheduledAt,
                    windowMinutes = 15
                )
            )
            _lastScheduledAt.value = scheduledAt
        }
    }

    companion object {
        private const val TEST_REMINDER_ID = 999_999_999L
    }
}