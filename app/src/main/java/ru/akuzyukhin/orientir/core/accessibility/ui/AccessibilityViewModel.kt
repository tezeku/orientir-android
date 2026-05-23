package ru.akuzyukhin.orientir.core.accessibility.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.akuzyukhin.orientir.core.accessibility.domain.model.AccessibilityProfile
import ru.akuzyukhin.orientir.core.accessibility.domain.repository.AccessibilityRepository
import javax.inject.Inject

@HiltViewModel
class AccessibilityViewModel @Inject constructor(
    private val repository: AccessibilityRepository
) : ViewModel() {

    val profile: StateFlow<AccessibilityProfile> = repository
        .profileFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AccessibilityProfile.STANDARD
        )

    fun selectProfile(profile: AccessibilityProfile) {
        viewModelScope.launch {
            repository.setProfile(profile)
        }
    }
}
