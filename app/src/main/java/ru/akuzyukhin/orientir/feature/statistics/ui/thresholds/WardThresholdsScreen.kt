package ru.akuzyukhin.orientir.feature.statistics.ui.thresholds

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.roundToInt
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Индивидуальные пороги нарушений. При выходе показателей " +
                    "за эти границы вы получите уведомление.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ThresholdCard {
            PeriodInputRow(
                value = state.periodDays,
                valueRange = 1..90,
                onValueChange = onPeriodChange
            )
        }

        ThresholdCard {
            ThresholdSliderRow(
                title = "Максимальный коэффициент отклонений",
                value = state.maxGlobalDeviationPercent,
                valueRange = 0..100,
                unit = "%",
                onValueChange = onGlobalDeviationChange
            )
        }

        ThresholdCard {
            ThresholdSliderRow(
                title = "Минимальный процент выполнения",
                value = state.minCompletionRatePercent,
                valueRange = 0..100,
                unit = "%",
                onValueChange = onCompletionChange
            )
        }

        ThresholdCard {
            ThresholdSliderRow(
                title = "Максимальный процент пропусков",
                value = state.maxOverdueRatePercent,
                valueRange = 0..100,
                unit = "%",
                onValueChange = onOverdueChange
            )
        }

        ThresholdCard {
            ThresholdSliderRow(
                title = "Максимальное среднее отклонение",
                value = state.maxAvgDeviationMinutes,
                valueRange = 0..120,
                unit = "мин",
                onValueChange = onAvgDeviationChange
            )
        }

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
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ThresholdCard(content: @Composable () -> Unit) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun ThresholdSliderRow(
    title: String,
    value: Int,
    valueRange: IntRange,
    unit: String,
    onValueChange: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "$value $unit",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
            steps = 0,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
private fun PeriodInputRow(
    value: Int,
    valueRange: IntRange,
    onValueChange: (Int) -> Unit
) {
    var textValue by remember(value) { mutableStateOf(value.toString()) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Период анализа",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(12.dp))
            OutlinedTextField(
                value = textValue,
                onValueChange = { input ->
                    textValue = input.filter { it.isDigit() }.take(2)
                    textValue.toIntOrNull()?.let { num ->
                        if (num in valueRange) onValueChange(num)
                    }
                },
                suffix = { Text("дн") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.width(110.dp)
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = {
                val num = it.roundToInt()
                onValueChange(num)
                textValue = num.toString()
            },
            valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
            steps = 0,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}
