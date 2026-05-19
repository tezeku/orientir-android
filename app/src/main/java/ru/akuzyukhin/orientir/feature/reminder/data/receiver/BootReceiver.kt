package ru.akuzyukhin.orientir.feature.reminder.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.akuzyukhin.orientir.feature.reminder.domain.repository.ReminderRepository
import javax.inject.Inject

/** BroadcastReceiver, обрабатывающий завершение загрузки устройства */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var reminderRepository: ReminderRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            android.util.Log.d(TAG, "onReceive: ignored action ${intent.action}")
            return
        }

        android.util.Log.d(TAG, "onReceive: BOOT_COMPLETED, restoring reminders")

        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        scope.launch {
            try {
                reminderRepository.restoreFromStorage()
                android.util.Log.d(TAG, "onReceive: restoration completed")
            } catch (e: Exception) {
                android.util.Log.e(TAG, "onReceive: restoration failed", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}