package ru.akuzyukhin.orientir.feature.profile.ui.password

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.akuzyukhin.orientir.core.ui.CollectAsEffect
import ru.akuzyukhin.orientir.ui.theme.OrientirTheme

@Composable
fun ChangePasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChangePasswordViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CollectAsEffect(viewModel.events) { event ->
        when (event) {
            ChangePasswordUiEvent.NavigateBack -> onNavigateBack()
        }
    }

    ChangePasswordContent(
        state = state,
        onCurrentPasswordChanged = viewModel::onCurrentPasswordChanged,
        onNewPasswordChanged = viewModel::onNewPasswordChanged,
        onConfirmPasswordChanged = viewModel::onConfirmPasswordChanged,
        onToggleShowPasswords = viewModel::onToggleShowPasswords,
        onSubmit = viewModel::onSubmit,
        onBackClick = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangePasswordContent(
    state: ChangePasswordUiState,
    onCurrentPasswordChanged: (String) -> Unit,
    onNewPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onToggleShowPasswords: () -> Unit,
    onSubmit: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Смена пароля") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onToggleShowPasswords) {
                        Icon(
                            imageVector = if (state.showPasswords)
                                Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (state.showPasswords)
                                "Скрыть пароли" else "Показать пароли"
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
            Text(
                text = "Минимальная длина — 8 символов",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            PasswordField(
                value = state.currentPassword,
                onValueChange = onCurrentPasswordChanged,
                label = "Текущий пароль",
                error = state.currentPasswordError,
                showPassword = state.showPasswords,
                enabled = !state.isLoading
            )
            Spacer(Modifier.height(16.dp))

            PasswordField(
                value = state.newPassword,
                onValueChange = onNewPasswordChanged,
                label = "Новый пароль",
                error = state.newPasswordError,
                showPassword = state.showPasswords,
                enabled = !state.isLoading
            )
            Spacer(Modifier.height(16.dp))

            PasswordField(
                value = state.confirmPassword,
                onValueChange = onConfirmPasswordChanged,
                label = "Подтверждение нового пароля",
                error = state.confirmPasswordError,
                showPassword = state.showPasswords,
                enabled = !state.isLoading
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
                    Text("Сменить пароль")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    showPassword: Boolean,
    enabled: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        visualTransformation = if (showPassword)
            VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true)
@Composable
private fun ChangePasswordContentPreview() {
    OrientirTheme {
        ChangePasswordContent(
            state = ChangePasswordUiState(),
            onCurrentPasswordChanged = {}, onNewPasswordChanged = {},
            onConfirmPasswordChanged = {}, onToggleShowPasswords = {},
            onSubmit = {}, onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "With errors")
@Composable
private fun ChangePasswordContentErrorPreview() {
    OrientirTheme {
        ChangePasswordContent(
            state = ChangePasswordUiState(
                currentPassword = "wrong",
                newPassword = "short",
                confirmPassword = "different",
                newPasswordError = "Минимум 8 символов",
                confirmPasswordError = "Пароли не совпадают",
                errorMessage = "Неверный текущий пароль"
            ),
            onCurrentPasswordChanged = {}, onNewPasswordChanged = {},
            onConfirmPasswordChanged = {}, onToggleShowPasswords = {},
            onSubmit = {}, onBackClick = {}
        )
    }
}