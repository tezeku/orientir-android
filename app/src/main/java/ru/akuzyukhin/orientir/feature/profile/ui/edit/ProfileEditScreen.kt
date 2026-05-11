package ru.akuzyukhin.orientir.feature.profile.ui.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.akuzyukhin.orientir.core.ui.CollectAsEffect
import ru.akuzyukhin.orientir.feature.auth.domain.model.Role
import ru.akuzyukhin.orientir.feature.profile.domain.model.Profile
import ru.akuzyukhin.orientir.ui.theme.OrientirTheme

@Composable
fun ProfileEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileEditViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CollectAsEffect(viewModel.events) { event ->
        when (event) {
            ProfileEditUiEvent.NavigateBack -> onNavigateBack()
        }
    }

    ProfileEditContent(
        state = state,
        onSurnameChanged = viewModel::onSurnameChanged,
        onNameChanged = viewModel::onNameChanged,
        onPatronymicChanged = viewModel::onPatronymicChanged,
        onEmailChanged = viewModel::onEmailChanged,
        onAddressChanged = viewModel::onAddressChanged,
        onSave = viewModel::onSave,
        onRetry = viewModel::retry,
        onBackClick = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileEditContent(
    state: ProfileEditUiState,
    onSurnameChanged: (String) -> Unit,
    onNameChanged: (String) -> Unit,
    onPatronymicChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onAddressChanged: (String) -> Unit,
    onSave: () -> Unit,
    onRetry: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактирование") },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.loadErrorMessage != null && state.initialProfile == null -> {
                    LoadError(
                        message = state.loadErrorMessage,
                        onRetry = onRetry
                    )
                }
                state.initialProfile != null -> {
                    EditForm(
                        state = state,
                        profile = state.initialProfile,
                        onSurnameChanged = onSurnameChanged,
                        onNameChanged = onNameChanged,
                        onPatronymicChanged = onPatronymicChanged,
                        onEmailChanged = onEmailChanged,
                        onAddressChanged = onAddressChanged,
                        onSave = onSave
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadError(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Не удалось загрузить профиль",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry) { Text("Повторить") }
    }
}

@Composable
private fun EditForm(
    state: ProfileEditUiState,
    profile: Profile,
    onSurnameChanged: (String) -> Unit,
    onNameChanged: (String) -> Unit,
    onPatronymicChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onAddressChanged: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        ReadOnlyField(label = "Номер телефона", value = profile.phoneNumber)
        Spacer(Modifier.height(8.dp))
        ReadOnlyField(label = "Роль", value = roleDisplayName(profile.role))

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Личные данные",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = state.surname,
            onValueChange = onSurnameChanged,
            label = { Text("Фамилия") },
            singleLine = true,
            isError = state.surnameError != null,
            supportingText = state.surnameError?.let { { Text(it) } },
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = state.name,
            onValueChange = onNameChanged,
            label = { Text("Имя") },
            singleLine = true,
            isError = state.nameError != null,
            supportingText = state.nameError?.let { { Text(it) } },
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = state.patronymic,
            onValueChange = onPatronymicChanged,
            label = { Text("Отчество (необязательно)") },
            singleLine = true,
            isError = state.patronymicError != null,
            supportingText = state.patronymicError?.let { { Text(it) } },
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        )

        if (profile.role == Role.CURATOR) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Контакты",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.email,
                onValueChange = onEmailChanged,
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                isError = state.emailError != null,
                supportingText = state.emailError?.let { { Text(it) } }
                    ?: { Text("Для уведомлений о подопечных") },
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (profile.role == Role.WARD) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Адрес",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.address,
                onValueChange = onAddressChanged,
                label = { Text("Адрес проживания (необязательно)") },
                singleLine = true,
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (state.saveErrorMessage != null) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = state.saveErrorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onSave,
            enabled = state.isSaveEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(if (state.hasChanges) "Сохранить изменения" else "Без изменений")
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ReadOnlyField(label: String, value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        enabled = false,
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

private fun roleDisplayName(role: Role): String = when (role) {
    Role.CURATOR -> "Куратор"
    Role.WARD -> "Подопечный"
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun ProfileEditContentLoadedPreview() {
    OrientirTheme {
        val profile = Profile(
            id = 1, surname = "Кузюхин", name = "Артемий", patronymic = "Вячеславович",
            phoneNumber = "+79223334455", role = Role.CURATOR, isActive = true,
            email = "ivan@mail.ru", address = null
        )
        ProfileEditContent(
            state = ProfileEditUiState(
                initialProfile = profile,
                surname = profile.surname,
                name = profile.name,
                patronymic = profile.patronymic ?: "",
                email = profile.email ?: "",
                isLoading = false
            ),
            onSurnameChanged = {}, onNameChanged = {}, onPatronymicChanged = {},
            onEmailChanged = {}, onAddressChanged = {},
            onSave = {}, onRetry = {}, onBackClick = {}
        )
    }
}