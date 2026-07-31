package com.KotEdit.mobiletexteditor.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val VellumColorScheme = darkColorScheme(
    primary = VellumPrimary,
    onPrimary = VellumOnPrimary,
    primaryContainer = VellumPrimaryContainer,
    onPrimaryContainer = VellumOnPrimaryContainer,
    secondary = VellumSecondary,
    onSecondary = VellumOnSecondary,
    secondaryContainer = VellumSecondaryContainer,
    onSecondaryContainer = VellumOnSecondaryContainer,
    tertiary = VellumTertiary,
    onTertiary = VellumOnTertiary,
    tertiaryContainer = VellumTertiaryContainer,
    onTertiaryContainer = VellumOnTertiaryContainer,
    error = VellumError,
    onError = VellumOnError,
    errorContainer = VellumErrorContainer,
    onErrorContainer = VellumOnErrorContainer,
    background = VellumBackground,
    onBackground = VellumOnBackground,
    surface = VellumSurface,
    onSurface = VellumOnSurface,
    surfaceVariant = VellumSurfaceVariant,
    onSurfaceVariant = VellumOnSurfaceVariant,
    outline = VellumOutline,
    outlineVariant = VellumOutlineVariant
)

@Composable
fun MobileTextEditorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled to enforce our premium Obsidian theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> VellumColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}