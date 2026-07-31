package com.julien.frigomalin.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ColorSchemeClair = lightColorScheme(
    primary = TealPrimary,
    onPrimary = TealOnPrimary,
    primaryContainer = TealPrimaryContainer,
    onPrimaryContainer = TealOnPrimaryContainer,
    secondary = CorailSecondary,
    onSecondary = CorailOnSecondary,
    secondaryContainer = CorailSecondaryContainer,
    onSecondaryContainer = CorailOnSecondaryContainer,
    tertiary = Tertiary,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    error = Erreur
)

private val ColorSchemeSombre = darkColorScheme(
    primary = TealPrimaryDark,
    onPrimary = TealOnPrimaryDark,
    primaryContainer = TealPrimaryContainerDark,
    onPrimaryContainer = TealOnPrimaryContainerDark,
    secondary = CorailSecondaryDark,
    onSecondary = CorailOnSecondaryDark,
    secondaryContainer = CorailSecondaryContainerDark,
    onSecondaryContainer = CorailOnSecondaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    error = ErreurDark
)

@Composable
fun FrigoMalinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ColorSchemeSombre else ColorSchemeClair
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}