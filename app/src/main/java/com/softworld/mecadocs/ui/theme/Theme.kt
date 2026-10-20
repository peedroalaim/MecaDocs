package com.softworld.mecadocs.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val EngineeringDarkColorScheme = darkColorScheme(
    primary = EngineeringElectricBlue,
    onPrimary = EngineeringTextPrimary,
    primaryContainer = EngineeringSurfaceVariant,
    onPrimaryContainer = EngineeringTextPrimary,
    secondary = EngineeringCyan,
    onSecondary = EngineeringDeepNavy,
    secondaryContainer = EngineeringSurfaceVariant,
    onSecondaryContainer = EngineeringCyan,
    background = EngineeringDeepNavy,
    surface = EngineeringSurface,
    surfaceVariant = EngineeringSurfaceVariant,
    error = EngineeringDanger,
    onError = EngineeringTextPrimary,
    outline = EngineeringDivider
)

private val EngineeringLightColorScheme = lightColorScheme(
    primary = EngineeringElectricBlue,
    onPrimary = EngineeringTextPrimary,
    primaryContainer = EngineeringSurfaceVariant,
    onPrimaryContainer = EngineeringTextPrimary,
    secondary = EngineeringCyan,
    onSecondary = EngineeringDeepNavy,
    secondaryContainer = EngineeringSurfaceVariant,
    onSecondaryContainer = EngineeringCyan,
    background = EngineeringDeepNavy,
    surface = EngineeringSurface,
    surfaceVariant = EngineeringSurfaceVariant,
    error = EngineeringDanger,
    onError = EngineeringTextPrimary,
    outline = EngineeringDivider
)

/**
 * Tema principal do MecaDocs - Modo Expert Pro (Estação de Engenharia).
 * Focado primordialmente em Dark Theme profissional de laboratório.
 */
@Composable
fun MecaDocsTheme(
    darkTheme: Boolean = true, // Dark theme como padrão profissional de engenharia
    dynamicColor: Boolean = false, // Desativado por padrão para preservar a identidade visual técnica
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> EngineeringDarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
