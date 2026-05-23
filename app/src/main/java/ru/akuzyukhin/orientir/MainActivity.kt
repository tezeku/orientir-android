package ru.akuzyukhin.orientir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import ru.akuzyukhin.orientir.core.accessibility.ui.AccessibilityViewModel
import ru.akuzyukhin.orientir.navigation.OrientirNavGraph
import ru.akuzyukhin.orientir.ui.theme.OrientirTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val accessibilityVm: AccessibilityViewModel = hiltViewModel()
            val profile by accessibilityVm.profile.collectAsStateWithLifecycle()
            OrientirTheme(accessibilityProfile = profile) {
                OrientirNavGraph()
            }
        }
    }
}