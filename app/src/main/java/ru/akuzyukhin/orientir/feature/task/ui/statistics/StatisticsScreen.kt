package ru.akuzyukhin.orientir.feature.task.ui.statistics

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ru.akuzyukhin.orientir.feature.auth.domain.model.Role
import ru.akuzyukhin.orientir.feature.task.domain.model.ExecutionStatus

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatisticsScreen() {
    val viewModel: StatisticsViewModel = hiltViewModel()
    val role by viewModel.role.collectAsState()

    when (role) {
        Role.WARD -> WardStatistics(viewModel)
        Role.CURATOR -> CuratorEmpty()
        null -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WardStatistics(viewModel: StatisticsViewModel) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Статистика") }) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PeriodSelector(state.period, viewModel::onPeriodChange)

            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxWidth().padding(48.dp), Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.errorMessage != null -> {
                    val err = state.errorMessage!!
                    Column(
                        Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Не удалось загрузить статистику")
                        Spacer(Modifier.height(8.dp))
                        Text(err, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = viewModel::retry) { Text("Повторить") }
                    }
                }
                state.counters.total == 0 -> EmptyStatistics()
                else -> StatisticsContent(state.counters)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodSelector(
    period: StatisticsPeriod,
    onChange: (StatisticsPeriod) -> Unit
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        StatisticsPeriod.entries.forEachIndexed { index, p ->
            SegmentedButton(
                selected = period == p,
                onClick = { onChange(p) },
                shape = SegmentedButtonDefaults.itemShape(index, StatisticsPeriod.entries.size)
            ) {
                Text(p.label)
            }
        }
    }
}

@Composable
private fun StatisticsContent(counters: StatisticsCounters) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Процент выполнения",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "${(counters.completionRate * 100).toInt()}%",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${counters.completedTotal} из ${counters.total} задач",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }

    Text(
        "По статусам",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 8.dp)
    )

    listOf(
        ExecutionStatus.COMPLETED to "Выполнено",
        ExecutionStatus.COMPLETED_LATE to "Выполнено с опозданием",
        ExecutionStatus.OVERDUE to "Просрочено",
        ExecutionStatus.BLOCKED to "Не удалось выполнить",
        ExecutionStatus.SKIPPED to "Пропущено",
        ExecutionStatus.PENDING to "Ожидает выполнения"
    ).forEach { (status, label) ->
        StatusRow(
            label = label,
            count = counters.byStatus[status] ?: 0,
            total = counters.total,
            color = statusColor(status)
        )
    }
}

@Composable
private fun StatusRow(label: String, count: Int, total: Int, color: Color) {
    val ratio = if (total == 0) 0f else count.toFloat() / total
    val percent = (ratio * 100).toInt()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(12.dp).background(color, RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(8.dp))
                Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Text(
                    "$count ($percent%)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(3.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(ratio)
                        .height(6.dp)
                        .background(color, RoundedCornerShape(3.dp))
                )
            }
        }
    }
}

@Composable
private fun EmptyStatistics() {
    Column(
        Modifier.fillMaxWidth().padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.BarChart, null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "За этот период данных пока нет",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CuratorEmpty() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.People, null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("Статистика подопечных", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Откройте конкретного подопечного через раздел «Мои подопечные» в Профиле — статистика по нему появится в одном из следующих обновлений.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private fun statusColor(status: ExecutionStatus): Color = when (status) {
    ExecutionStatus.COMPLETED -> Color(0xFF2E7D32)
    ExecutionStatus.COMPLETED_LATE -> Color(0xFFE65100)
    ExecutionStatus.OVERDUE -> Color(0xFFD32F2F)
    ExecutionStatus.BLOCKED -> Color(0xFFC62828)
    ExecutionStatus.SKIPPED -> Color(0xFF757575)
    ExecutionStatus.PENDING -> Color(0xFF1976D2)
}