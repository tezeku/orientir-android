package ru.akuzyukhin.orientir.feature.auth.ui.register

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.akuzyukhin.orientir.core.ui.CollectAsEffect
import ru.akuzyukhin.orientir.feature.auth.domain.model.Role
import ru.akuzyukhin.orientir.ui.theme.OrientirTheme

@Composable
fun RegisterScreen(
    onNavigateToHome: (Role) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CollectAsEffect(viewModel.events) { event ->
        when (event) {
            is RegisterUiEvent.NavigateToHome -> onNavigateToHome(event.role)
            RegisterUiEvent.NavigateToLogin -> onNavigateToLogin()
            RegisterUiEvent.NavigateBack -> onNavigateBack()
        }
    }

    RegisterContent(
        state = state,
        onRoleSelected = viewModel::onRoleSelected,
        onSurnameChanged = viewModel::onSurnameChanged,
        onNameChanged = viewModel::onNameChanged,
        onPatronymicChanged = viewModel::onPatronymicChanged,
        onPhoneNumberChanged = viewModel::onPhoneNumberChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onEmailChanged = viewModel::onEmailChanged,
        onAddressChanged = viewModel::onAddressChanged,
        onSubmit = viewModel::onSubmit,
        onLoginClick = viewModel::onLoginClick,
        onBackClick = viewModel::onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterContent(
    state: RegisterUiState,
    onRoleSelected: (Role) -> Unit,
    onSurnameChanged: (String) -> Unit,
    onNameChanged: (String) -> Unit,
    onPatronymicChanged: (String) -> Unit,
    onPhoneNumberChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onAddressChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onLoginClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Регистрация") },
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
            SectionHeader("Кто вы")
            Spacer(Modifier.height(12.dp))

            var rolePickerExpanded by rememberSaveable(state.role) {
                mutableStateOf(state.role == null)
            }

            AnimatedContent(
                targetState = state.role != null && !rolePickerExpanded,
                label = "rolePicker",
                transitionSpec = {
                    (fadeIn(animationSpec = tween(200)))
                        .togetherWith(fadeOut(animationSpec = tween(200)))
                }
            ) { isCollapsed ->
                if (isCollapsed && state.role != null) {
                    SelectedRoleChip(
                        role = state.role,
                        onClick = { rolePickerExpanded = true },
                        enabled = !state.isLoading
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RoleCard(
                            role = Role.CURATOR,
                            title = "Куратор",
                            description = "Создаю расписание и слежу за выполнением",
                            icon = Icons.Default.SupervisorAccount,
                            isSelected = state.role == Role.CURATOR,
                            onClick = {
                                onRoleSelected(Role.CURATOR)
                                rolePickerExpanded = false
                            },
                            modifier = Modifier.weight(1f)
                        )
                        RoleCard(
                            role = Role.WARD,
                            title = "Подопечный",
                            description = "Выполняю задачи по своему расписанию",
                            icon = Icons.Default.Person,
                            isSelected = state.role == Role.WARD,
                            onClick = {
                                onRoleSelected(Role.WARD)
                                rolePickerExpanded = false
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            SectionHeader("Основное")
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.surname,
                onValueChange = onSurnameChanged,
                label = { Text("Фамилия") },
                singleLine = true,
                isError = state.surnameError != null,
                supportingText = state.surnameError?.let { { Text(it) } },
                enabled = !state.isLoading,
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
                enabled = !state.isLoading,
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
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.phoneNumber,
                onValueChange = onPhoneNumberChanged,
                label = { Text("Номер телефона") },
                placeholder = { Text("+79161234567") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                isError = state.phoneError != null || state.phoneAlreadyTaken,
                supportingText = {
                    when {
                        state.phoneAlreadyTaken -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Такой телефон уже зарегистрирован — ")
                                Text(
                                    text = "Войти",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable(enabled = !state.isLoading) {
                                        onLoginClick()
                                    }
                                )
                            }
                        }
                        state.phoneError != null -> Text(state.phoneError)
                    }
                },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.password,
                onValueChange = onPasswordChanged,
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                isError = state.passwordError != null,
                supportingText = state.passwordError?.let { { Text(it) } }
                    ?: { Text("Минимум 8 символов") },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            if (state.role != null) {
                Spacer(Modifier.height(12.dp))
                SectionHeader("Дополнительно")
                Spacer(Modifier.height(12.dp))

                when (state.role) {
                    Role.CURATOR -> {
                        OutlinedTextField(
                            value = state.email,
                            onValueChange = onEmailChanged,
                            label = { Text("Email") },
                            placeholder = { Text("Для уведомлений") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            isError = state.emailError != null,
                            supportingText = state.emailError?.let { { Text(it) } }
                                ?: { Text("Будем присылать важные уведомления о подопечном") },
                            enabled = !state.isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Role.WARD -> {
                        OutlinedTextField(
                            value = state.address,
                            onValueChange = onAddressChanged,
                            label = { Text("Адрес (необязательно)") },
                            singleLine = true,
                            enabled = !state.isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            if (state.errorMessage != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(12.dp))

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
                    Text("Зарегистрироваться")
                }
            }

            TextButton(
                onClick = onLoginClick,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Уже есть аккаунт? Войти")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun RoleCard(
    role: Role,
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }

    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp)
            )
        }

        Column {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SelectedRoleChip(
    role: Role,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val (title, icon) = when (role) {
        Role.CURATOR -> "Куратор" to Icons.Default.SupervisorAccount
        Role.WARD -> "Подопечный" to Icons.Default.Person
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Нажмите, чтобы изменить",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Изменить",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun RegisterContentPreview_NoRole() {
    OrientirTheme {
        RegisterContent(
            state = RegisterUiState(),
            onRoleSelected = {},
            onSurnameChanged = {}, onNameChanged = {}, onPatronymicChanged = {},
            onPhoneNumberChanged = {}, onPasswordChanged = {},
            onEmailChanged = {}, onAddressChanged = {},
            onSubmit = {}, onLoginClick = {}, onBackClick = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 1200, name = "Curator selected")
@Composable
private fun RegisterContentPreview_Curator() {
    OrientirTheme {
        RegisterContent(
            state = RegisterUiState(role = Role.CURATOR),
            onRoleSelected = {},
            onSurnameChanged = {}, onNameChanged = {}, onPatronymicChanged = {},
            onPhoneNumberChanged = {}, onPasswordChanged = {},
            onEmailChanged = {}, onAddressChanged = {},
            onSubmit = {}, onLoginClick = {}, onBackClick = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 1200, name = "Phone taken")
@Composable
private fun RegisterContentPreview_PhoneTaken() {
    OrientirTheme {
        RegisterContent(
            state = RegisterUiState(
                role = Role.WARD,
                surname = "Иванов",
                name = "Иван",
                phoneNumber = "+79161234567",
                password = "password1",
                phoneAlreadyTaken = true
            ),
            onRoleSelected = {},
            onSurnameChanged = {}, onNameChanged = {}, onPatronymicChanged = {},
            onPhoneNumberChanged = {}, onPasswordChanged = {},
            onEmailChanged = {}, onAddressChanged = {},
            onSubmit = {}, onLoginClick = {}, onBackClick = {}
        )
    }
}