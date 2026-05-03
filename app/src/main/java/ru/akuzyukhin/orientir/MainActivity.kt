package ru.akuzyukhin.orientir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import ru.akuzyukhin.orientir.navigation.OrientirNavGraph
import ru.akuzyukhin.orientir.ui.theme.OrientirTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OrientirTheme {
                OrientirNavGraph()
            }
        }
    }
}