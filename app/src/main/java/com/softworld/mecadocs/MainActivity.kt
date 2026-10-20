package com.softworld.mecadocs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.softworld.mecadocs.data.local.database.MecaDocsDatabase
import com.softworld.mecadocs.data.repository.MecaDocsRepository
import com.softworld.mecadocs.ui.screens.HomeScreen
import com.softworld.mecadocs.ui.splash.MecaDocsSplashScreen
import com.softworld.mecadocs.ui.theme.MecaDocsTheme
import com.softworld.mecadocs.ui.viewmodel.MainViewModel

/**
 * Activity Principal do aplicativo MecaDocs (Modo Expert).
 * Configura o banco de dados Room, o tema Material 3 com suporte Edge-to-Edge
 * e transição fluida entre a Tela de Splash e a Tela Principal (Home).
 */
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = MecaDocsDatabase.getInstance(applicationContext)
                    val repository = MecaDocsRepository(mecaDocsDao = db.mecaDocsDao())
                return MainViewModel(repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MecaDocsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MecaDocsApp(viewModel = viewModel)
                }
            }
        }
    }
}

/**
 * Composable raiz da aplicação que transiciona suavemente da Splash Screen para a HomeScreen.
 */
@Composable
fun MecaDocsApp(viewModel: MainViewModel) {
    var isSplashFinished by remember { mutableStateOf(false) }

    Crossfade(
        targetState = isSplashFinished,
        animationSpec = tween(durationMillis = 500),
        label = "SplashToMainTransition"
    ) { finished ->
        if (!finished) {
            MecaDocsSplashScreen(
                onSplashFinished = { isSplashFinished = true }
            )
        } else {
            HomeScreen(viewModel = viewModel)
        }
    }
}
