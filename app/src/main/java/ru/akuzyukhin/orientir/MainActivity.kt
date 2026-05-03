package ru.akuzyukhin.orientir

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.akuzyukhin.orientir.feature.auth.domain.repository.AuthRepository
import ru.akuzyukhin.orientir.feature.auth.ui.login.LoginScreen
import ru.akuzyukhin.orientir.feature.auth.ui.register.RegisterScreen
import ru.akuzyukhin.orientir.ui.theme.OrientirTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OrientirTheme {
                RegisterScreen(
                    onNavigateToHome = { role ->
                        Log.d("MainActivity", "Register OK, role=$role")
                    },
                    onNavigateToLogin = {
                        Log.d("MainActivity", "Go to login")
                    },
                    onNavigateBack = {
                        Log.d("MainActivity", "Back pressed")
                    }
                )
            }
        }
    }
}