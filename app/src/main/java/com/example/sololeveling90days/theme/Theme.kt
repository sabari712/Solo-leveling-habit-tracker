package com.example.sololeveling90days.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val SoloLeveling90DaysDarkColorScheme = darkColorScheme(
    primary = StaticAppleBlueDark,
    onPrimary = StaticTextPrimaryDark,
    primaryContainer = StaticAppleBlueDark,
    onPrimaryContainer = StaticTextPrimaryDark,
    secondary = StaticAppleBlueDark,
    onSecondary = StaticDarkBg,
    secondaryContainer = StaticDisciplineNavy,
    onSecondaryContainer = StaticTextPrimaryDark,
    tertiary = StaticActionOrangeDark,
    onTertiary = StaticTextPrimaryDark,
    background = StaticDarkBg,
    onBackground = StaticTextPrimaryDark,
    surface = StaticDarkBg,
    onSurface = StaticTextPrimaryDark,
    surfaceVariant = StaticDisciplineNavy,
    onSurfaceVariant = StaticTextSecondaryDark,
    error = StaticHardRedDark,
    onError = StaticTextPrimaryDark,
)

private val SoloLeveling90DaysLightColorScheme = lightColorScheme(
    primary = StaticAppleBlueLight,
    onPrimary = StaticTextPrimaryLight,
    primaryContainer = StaticAppleBlueLight,
    onPrimaryContainer = StaticTextPrimaryLight,
    secondary = StaticAppleBlueLight,
    onSecondary = StaticLightBg,
    secondaryContainer = StaticLightSecondary,
    onSecondaryContainer = StaticTextPrimaryLight,
    tertiary = StaticActionOrangeLight,
    onTertiary = StaticTextPrimaryLight,
    background = StaticLightBg,
    onBackground = StaticTextPrimaryLight,
    surface = StaticLightBg,
    onSurface = StaticTextPrimaryLight,
    surfaceVariant = StaticLightSecondary,
    onSurfaceVariant = StaticTextSecondaryLight,
    error = StaticHardRedLight,
    onError = StaticTextPrimaryLight,
)

@Composable
fun SoloLeveling90DaysTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) {
        SoloLeveling90DaysDarkColorScheme
    } else {
        SoloLeveling90DaysLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
