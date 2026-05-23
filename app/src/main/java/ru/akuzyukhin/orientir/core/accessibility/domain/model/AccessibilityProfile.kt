package ru.akuzyukhin.orientir.core.accessibility.domain.model

enum class AccessibilityProfile(val fontScale: Float, val displayName: String) {
    STANDARD(1.0f, "Стандартный"),
    LARGE(1.25f, "Крупный"),
    EXTRA_LARGE(1.5f, "Очень крупный");

    companion object {
        fun fromName(name: String?): AccessibilityProfile =
            values().firstOrNull { it.name == name } ?: STANDARD
    }
}
