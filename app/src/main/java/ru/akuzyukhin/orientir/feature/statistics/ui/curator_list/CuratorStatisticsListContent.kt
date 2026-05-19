package ru.akuzyukhin.orientir.feature.statistics.ui.curator_list

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.akuzyukhin.orientir.core.ui.CollectAsEffect
import ru.akuzyukhin.orientir.feature.statistics.domain.model.GlobalDeviation
import ru.akuzyukhin.orientir.feature.statistics.domain.model.Trend

/** Composable для содержимого таба «Статистика» при роли CURATOR */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuratorStatisticsListContent(
    onWardClick: (Long) -> Unit,
    viewModel: CuratorStatisticsListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CollectAsEffect(viewModel.events) { event ->
        when (event) {
            is CuratorStatisticsListUiEvent.NavigateToWardStatistics -> onWardClick(event.wardId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Статистика подопечных") })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
                state.errorMessage != null -> Text(
                    text = state.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp)
                )
                state.items.isEmpty() -> EmptyState()
                else -> WardsList(state.items, viewModel::onWardClick)
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Подопечные не привязаны",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Добавьте подопечного в разделе Профиль → Мои подопечные",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WardsList(items: List<WardStatisticsItem>, onClick: (Long) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items, key = { it.wardId }) { item ->
            WardStatisticsCard(item, onClick)
        }
    }
}

@Composable
private fun WardStatisticsCard(item: WardStatisticsItem, onClick: (Long) -> Unit) {
    val deviation = item.deviation
    val percent = deviation?.let { (it.current * 100).toInt() }
    val isExceeded = deviation?.isExceeded == true

    val accentColor = when {
        deviation == null -> MaterialTheme.colorScheme.outlineVariant
        isExceeded -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }

    ElevatedCard(
        onClick = { onClick(item.wardId) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                TrendRow(deviation)
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = percent?.let { "$it%" } ?: "—",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun TrendRow(deviation: GlobalDeviation?) {
    if (deviation == null) {
        Text(
            text = "Нет данных за период",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }
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
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = color)
    }
}