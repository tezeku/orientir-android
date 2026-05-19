package ru.akuzyukhin.orientir.feature.statistics.ui.thresholds

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import ru.akuzyukhin.orientir.core.ui.CollectAsEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardThresholdsScreen(
    onNavigateBack: () -> Unit,
    viewModel: WardThresholdsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    CollectAsEffect(viewModel.events) { event ->
        when (event) {
            WardThresholdsUiEvent.NavigateBack -> onNavigateBack()
            WardThresholdsUiEvent.SavedSuccessfully -> {
                scope.launch { snackbarHostState.showSnackbar("Пороги сохранены") }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки порогов") },
                navigationIcon = {
                    IconButton(onClick = viewModel::onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> ThresholdsContent(
                    state = state,
                    onPeriodChange = viewModel::onPeriodChange,
                    onGlobalDeviationChange = viewModel::onGlobalDeviationChange,
                    onCompletionChange = viewModel::onCompletionChange,
                    onOverdueChange = viewModel::onOverdueChange,
                    onAvgDeviationChange = viewModel::onAvgDeviationChange,
                    onSave = viewModel::save
                )
            }
        }
    }
}

@Composable
private fun ThresholdsContent(
    state: WardThresholdsUiState,
    onPeriodChange: (Int) -> Unit,
    onGlobalDeviationChange: (Int) -> Unit,
    onCompletionChange: (Int) -> Unit,
    onOverdueChange: (Int) -> Unit,
    onAvgDeviationChange: (Int) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Индивидуальные пороги нарушений. При выходе показателей " +
                    "за эти границы вы получите уведомление.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ThresholdSlider(
            title = "Период анализа",
            value = state.periodDays,
            valueRange = 1f..90f,
            steps = 88,
            displayValue = "${state.periodDays} дн.",
            onChange = onPeriodChange
        )

        ThresholdSlider(
            title = "Максимальный коэффициент отклонений (T)",
            value = state.maxGlobalDeviationPercent,
            valueRange = 0f..100f,
            steps = 99,
            displayValue = "${state.maxGlobalDeviationPercent}%",
            onChange = onGlobalDeviationChange
        )

        ThresholdSlider(
            title = "Минимальный процент выполнения",
            value = state.minCompletionRatePercent,
            valueRange = 0f..100f,
            steps = 99,
            displayValue = "${state.minCompletionRatePercent}%",
            onChange = onCompletionChange
        )

        ThresholdSlider(
            title = "Максимальный процент пропусков",
            value = state.maxOverdueRatePercent,
            valueRange = 0f..100f,
            steps = 99,
            displayValue = "${state.maxOverdueRatePercent}%",
            onChange = onOverdueChange
        )

        ThresholdSlider(
            title = "Максимальное среднее отклонение",
            value = state.maxAvgDeviationMinutes,
            valueRange = 0f..120f,
            steps = 119,
            displayValue = "${state.maxAvgDeviationMinutes} мин.",
            onChange = onAvgDeviationChange
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onSave,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text("Сохранить")
            }
        }

        state.errorMessage?.let { msg ->
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun ThresholdSlider(
    title: String,
    value: Int,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    displayValue: String,
    onChange: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = displayValue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toInt()) },
            valueRange = valueRange,
            steps = steps
        )
    }
}