package ru.akuzyukhin.orientir.feature.reminder.data.storage

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ru.akuzyukhin.orientir.feature.reminder.domain.model.ReminderInfo
import ru.akuzyukhin.orientir.feature.task.domain.model.Importance
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

private val Context.reminderDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "reminder_storage"
)

private val ACTIVE_REMINDERS_KEY = stringPreferencesKey("active_reminders")

/** Сериализуемый снимок одного напоминания */
@Serializable
internal data class StoredReminder(
    val taskExecutionId: Long,
    val taskName: String,
    val importance: String,
    val scheduledAtMillis: Long,
    val windowMinutes: Int
)

/** Контейнер активных напоминаний для сериализации одной JSON-строкой */
@Serializable
internal data class StoredReminderSet(
    val reminders: List<StoredReminder> = emptyList()
)

/** Локальное хранилище активных напоминаний */
@Singleton
class ReminderStorage @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private val dataStore: DataStore<Preferences> = context.reminderDataStore

    private val json: Json = Json {
        ignoreUnknownKeys = true
    }

    /**
     * Текущее состояние активных напоминаний, индексированных по
     * идентификатору экземпляра выполнения задачи.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun snapshot(): Map<Long, ReminderInfo> {
        val raw = dataStore.data.first()[ACTIVE_REMINDERS_KEY] ?: return emptyMap()
        val parsed = runCatching { json.decodeFromString<StoredReminderSet>(raw) }
            .getOrNull()
            ?: return emptyMap()
        return parsed.reminders
            .mapNotNull { it.toDomainOrNull() }
            .associateBy { it.taskExecutionId }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun replaceAll(reminders: List<ReminderInfo>) {
        val payload = StoredReminderSet(reminders.map { it.toStored() })
        val raw = json.encodeToString(StoredReminderSet.serializer(), payload)
        dataStore.edit { prefs ->
            prefs[ACTIVE_REMINDERS_KEY] = raw
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun put(reminder: ReminderInfo) {
        dataStore.edit { prefs ->
            val current = readSet(prefs[ACTIVE_REMINDERS_KEY])
                .reminders
                .associateBy { it.taskExecutionId }
                .toMutableMap()
            current[reminder.taskExecutionId] = reminder.toStored()
            prefs[ACTIVE_REMINDERS_KEY] = json.encodeToString(
                StoredReminderSet.serializer(),
                StoredReminderSet(current.values.toList())
            )
        }
    }

    suspend fun remove(taskExecutionId: Long) {
        dataStore.edit { prefs ->
            val current = readSet(prefs[ACTIVE_REMINDERS_KEY])
                .reminders
                .filter { it.taskExecutionId != taskExecutionId }
            prefs[ACTIVE_REMINDERS_KEY] = json.encodeToString(
                StoredReminderSet.serializer(),
                StoredReminderSet(current)
            )
        }
    }

    private fun readSet(raw: String?): StoredReminderSet {
        if (raw.isNullOrEmpty()) return StoredReminderSet()
        return runCatching { json.decodeFromString<StoredReminderSet>(raw) }
            .getOrNull()
            ?: StoredReminderSet()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun ReminderInfo.toStored() = StoredReminder(
        taskExecutionId = taskExecutionId,
        taskName = taskName,
        importance = importance.name,
        scheduledAtMillis = scheduledAt.toEpochMilli(),
        windowMinutes = windowMinutes
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun StoredReminder.toDomainOrNull(): ReminderInfo? {
        val importance = runCatching { Importance.valueOf(importance) }.getOrNull()
            ?: return null
        return ReminderInfo(
            taskExecutionId = taskExecutionId,
            taskName = taskName,
            importance = importance,
            scheduledAt = Instant.ofEpochMilli(scheduledAtMillis),
            windowMinutes = windowMinutes
        )
    }
}