package com.softworld.mecadocs.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softworld.mecadocs.R
import kotlinx.coroutines.delay

/**
 * Splash Screen profissional de Engenharia (Estação de Trabalho / Lab de Bancada).
 *
 * @param onSplashFinished Callback executado ao finalizar o tempo de exibição da splash screen.
 */
@Composable
fun MecaDocsSplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(
                durationMillis = 700,
                easing = FastOutSlowInEasing,
            ),
        )
        alpha.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 500),
        )
        delay(1200)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value),
        ) {
            // Ícone de Logo Técnico
            Surface(
                modifier = Modifier
                    .size(96.dp)
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary),
                        ),
                        shape = RoundedCornerShape(24.dp),
                    ),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 12.dp,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_logo),
                        contentDescription = "Logo MecaDocs",
                        modifier = Modifier
                            .size(88.dp)
                            .clip(RoundedCornerShape(20.dp)),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Título
            Text(
                text = "MecaDocs",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = 1.5.sp,
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtítulo
            Text(
                text = "DATASHEET DE BOLSO COM IA",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 2.sp,
            )
        }

        // Branding no rodapé
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(alpha.value),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "POWERED BY AI • LAB STATION",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                letterSpacing = 1.2.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "By: Softworld",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
