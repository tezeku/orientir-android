package ru.akuzyukhin.orientir.feature.reminder.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.akuzyukhin.orientir.feature.monitoring.domain.repository.MonitoringRepository
import ru.akuzyukhin.orientir.feature.reminder.domain.repository.ReminderRepository
import javax.inject.Inject

/** BroadcastReceiver, обрабатывающий действия из уведомления о задаче */
@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var monitoringRepository: MonitoringRepository

    @Inject
    lateinit var reminderRepository: ReminderRepository

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val executionId = intent.getLongExtra(EXTRA_TASK_EXECUTION_ID, INVALID_ID)
        if (executionId == INVALID_ID) {
            android.util.Log.w(TAG, "onReceive: missing execution id, skip")
            return
        }

        android.util.Log.d(TAG, "onReceive: action=$action, executionId=$executionId")

        when (action) {
            ACTION_COMPLETE -> handleComplete(context, executionId)
            else -> android.util.Log.w(TAG, "onReceive: unknown action $action")
        }
    }

    private fun handleComplete(context: Context, executionId: Long) {
        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        scope.launch {
            try {
                monitoringRepository.complete(executionId)
                    .onSuccess {
                        android.util.Log.d(TAG, "handleComplete: success id=$executionId")
                        reminderRepository.cancel(executionId)
                        NotificationManagerCompat.from(context).cancel(executionId.toInt())
                    }
                    .onFailure { error ->
                        android.util.Log.e(TAG, "handleComplete: failed id=$executionId", error)
                    }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_COMPLETE = "ru.akuzyukhin.orientir.action.COMPLETE_TASK"
        const val EXTRA_TASK_EXECUTION_ID = "task_execution_id"

        private const val INVALID_ID = Long.MIN_VALUE
        private const val TAG = "NotificationAction"
    }
}