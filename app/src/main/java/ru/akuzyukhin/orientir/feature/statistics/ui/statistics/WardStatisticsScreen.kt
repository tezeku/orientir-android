package ru.akuzyukhin.orientir.feature.statistics.ui.statistics

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.akuzyukhin.orientir.core.ui.CollectAsEffect
import ru.akuzyukhin.orientir.feature.statistics.domain.model.GlobalDeviation
import ru.akuzyukhin.orientir.feature.statistics.domain.model.Trend
import kotlin.math.abs

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardStatisticsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToThresholds: () -> Unit,
    viewModel: WardStatisticsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CollectAsEffect(viewModel.events) { event ->
        when (event) {
            WardStatisticsUiEvent.NavigateBack -> onNavigateBack()
            WardStatisticsUiEvent.NavigateToThresholds -> onNavigateToThresholds()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Статистика подопечного") },
                navigationIcon = {
                    IconButton(onClick = viewModel::onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::onThresholdsClick) {
                        Icon(Icons.Default.Tune, contentDescription = "Настройки порогов")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
                state.errorMessage != null -> Text(
                    text = state.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp)
                )
                state.deviation != null -> StatisticsContent(state.deviation!!)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun StatisticsContent(deviation: GlobalDeviation) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GlobalCoefficientCard(deviation)
        DeviationVisualization(deviation)
        TrendCard(deviation)
        InfoCard(deviation)
    }
}

@Composable
private fun GlobalCoefficientCard(deviation: GlobalDeviation) {
    val currentPercent = (deviation.current * 100).toInt()
    val thresholdPercent = (deviation.threshold * 100).toInt()
    val color =
        if (deviation.isExceeded) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.primary
    val containerColor =
        if (deviation.isExceeded) MaterialTheme.colorScheme.errorContainer
        else MaterialTheme.colorScheme.primaryContainer

    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Глобальный коэффициент отклонений",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "$currentPercent%",
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (deviation.current).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(MaterialTheme.shapes.small),
                color = color
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (deviation.isExceeded)
                    "Превышен порог $thresholdPercent%"
                else
                    "В пределах нормы (порог $thresholdPercent%)",
                style = MaterialTheme.typography.bodyMedium,
                color = color
            )
        }
    }
}

@Composable
private fun TrendCard(deviation: GlobalDeviation) {
    val (icon, label, color) = when (deviation.trend) {
        Trend.IMPROVING -> Triple(
            Icons.AutoMirrored.Filled.TrendingDown,
            "Улучшение",
            Color(0xFF2E7D32)
        )
        Trend.WORSENING -> Triple(
            Icons.AutoMirrored.Filled.TrendingUp,
            "Ухудшение",
            MaterialTheme.colorScheme.error
        )
        Trend.STABLE -> Triple(
            Icons.AutoMirrored.Filled.TrendingFlat,
            "Стабильно",
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        Trend.UNKNOWN -> Triple(
            Icons.AutoMirrored.Filled.TrendingFlat,
            "Недостаточно данных",
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    val deltaPercent = ((deviation.current - deviation.previous) * 100).toInt()

    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = color
                )
                Text(
                    text = "Δ ${if (deltaPercent > 0) "+" else ""}$deltaPercent% " +
                            "по сравнению с прошлым периодом",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun InfoCard(deviation: GlobalDeviation) {
    val periodDays = java.time.temporal.ChronoUnit.DAYS.between(
        deviation.periodFrom, deviation.periodTo
    ) + 1

    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Анализ",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(12.dp))
            InfoRow("Период анализа", "$periodDays дн.")
            InfoRow("Текущее значение", "${(deviation.current * 100).toInt()}%")
            InfoRow("Предыдущее значение", "${(deviation.previous * 100).toInt()}%")
            InfoRow("Установленный порог", "${(deviation.threshold * 100).toInt()}%")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DeviationVisualization(deviation: GlobalDeviation) {
    val currentPercent = (deviation.current * 100).toInt()
    val previousPercent = (deviation.previous * 100).toInt()
    val thresholdPercent = (deviation.threshold * 100).toInt()

    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Шкала отклонений",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(20.dp))

            val safeColor = Color(0xFF66BB6A)
            val dangerColor = MaterialTheme.colorScheme.error
            val currentColor =
                if (deviation.isExceeded) dangerColor else MaterialTheme.colorScheme.primary
            val previousColor = MaterialTheme.colorScheme.onSurfaceVariant

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                val width = size.width
                val barHeight = 14f
                val barTop = (size.height - barHeight) / 2f
                val thresholdX = width * (thresholdPercent / 100f)
                val currentX = width * (currentPercent / 100f).coerceIn(0f, 1f)
                val previousX = width * (previousPercent / 100f).coerceIn(0f, 1f)

                drawRoundRect(
                    color = safeColor.copy(alpha = 0.5f),
                    topLeft = androidx.compose.ui.geometry.Offset(0f, barTop),
                    size = androidx.compose.ui.geometry.Size(thresholdX, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(7f, 7f)
                )
                drawRoundRect(
                    color = dangerColor.copy(alpha = 0.5f),
                    topLeft = androidx.compose.ui.geometry.Offset(thresholdX, barTop),
                    size = androidx.compose.ui.geometry.Size(width - thresholdX, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(7f, 7f)
                )
                val dashPath = androidx.compose.ui.graphics.PathEffect
                    .dashPathEffect(floatArrayOf(6f, 4f), 0f)
                drawLine(
                    color = previousColor,
                    start = androidx.compose.ui.geometry.Offset(previousX, barTop - 6f),
                    end = androidx.compose.ui.geometry.Offset(previousX, barTop + barHeight + 6f),
                    strokeWidth = 3f,
                    pathEffect = dashPath
                )
                drawLine(
                    color = currentColor,
                    start = androidx.compose.ui.geometry.Offset(currentX, barTop - 8f),
                    end = androidx.compose.ui.geometry.Offset(currentX, barTop + barHeight + 8f),
                    strokeWidth = 5f
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "0%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Порог $thresholdPercent%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "100%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                LegendItem(
                    label = "Сейчас: $currentPercent%",
                    isDashed = false,
                    color = currentColor
                )
                LegendItem(
                    label = "Ранее: $previousPercent%",
                    isDashed = true,
                    color = previousColor
                )
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, isDashed: Boolean, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(16.dp, 4.dp)) {
            if (isDashed) {
                val dashPath = androidx.compose.ui.graphics.PathEffect
                    .dashPathEffect(floatArrayOf(3f, 2f), 0f)
                drawLine(
                    color = color,
                    start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                    end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2),
                    strokeWidth = 4f,
                    pathEffect = dashPath
                )
            } else {
                drawLine(
                    color = color,
                    start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                    end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2),
                    strokeWidth = 4f
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}