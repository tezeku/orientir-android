package ru.akuzyukhin.orientir.feature.connections.ui.add_ward

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.akuzyukhin.orientir.core.ui.CollectAsEffect
import ru.akuzyukhin.orientir.ui.theme.OrientirTheme

@Composable
fun AddWardScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddWardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CollectAsEffect(viewModel.events) { event ->
        when (event) {
            AddWardUiEvent.NavigateBackWithSuccess -> onNavigateBack()
        }
    }

    AddWardContent(
        state = state,
        onPhoneChanged = viewModel::onPhoneNumberChanged,
        onSubmit = viewModel::onSubmit,
        onBackClick = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddWardContent(
    state: AddWardUiState,
    onPhoneChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Привязать подопечного") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            InfoBlock()

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = state.phoneNumber,
                onValueChange = onPhoneChanged,
                label = { Text("Номер телефона подопечного") },
                placeholder = { Text("+79161234567") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                isError = state.phoneError != null,
                supportingText = state.phoneError?.let { { Text(it) } }
                    ?: { Text("Номер должен начинаться с + и кода страны") },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            if (state.errorMessage != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onSubmit,
                enabled = state.isSubmitEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Привязать")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InfoBlock() {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Как привязать подопечного",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Подопечный должен быть уже зарегистрирован в приложении. " +
                            "Введите его номер телефона в международном формате.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddWardContentPreview() {
    OrientirTheme {
        AddWardContent(
            state = AddWardUiState(),
            onPhoneChanged = {}, onSubmit = {}, onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "With error")
@Composable
private fun AddWardContentErrorPreview() {
    OrientirTheme {
        AddWardContent(
            state = AddWardUiState(
                phoneNumber = "+79161234567",
                errorMessage = "Подопечный с номером телефона +79161234567 не найден"
            ),
            onPhoneChanged = {}, onSubmit = {}, onBackClick = {}
        )
    }
}