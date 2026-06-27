package com.example.sololeveling90days.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// --- Static Color Constants ---
val StaticDarkBg = Color(0xFF000000) // OLED Obsidian Black
val StaticLightBg = Color(0xFFFFFFFF)
val StaticDisciplineNavy = Color(0xFF1C1C1E) // Frosted Slate Surface
val StaticLightSecondary = Color(0xFFF2F2F7)
val StaticAppleBlueDark = Color(0xFF0A84FF) // Electric Blue
val StaticAppleBlueLight = Color(0xFF0A84FF)

// 10% Semantic Status Colors
val StaticSuccessGreenDark = Color(0xFF10B981) // Growth Emerald
val StaticSuccessGreenLight = Color(0xFF10B981)
val StaticHardRedDark = Color(0xFFEF4444) // Crimson Red (Warning / threat)
val StaticHardRedLight = Color(0xFFEF4444)
val StaticActionOrangeDark = Color(0xFF7C3AED) // Shadow Violet / Purple
val StaticActionOrangeLight = Color(0xFF7C3AED)

// Text Colors
val StaticTextPrimaryDark = Color(0xFFE4E2E4)
val StaticTextPrimaryLight = Color(0xFF000000)
val StaticTextSecondaryDark = Color(0xFFC0C6D6)
val StaticTextSecondaryLight = Color(0x993C3C43)

// --- Dynamic @Composable Colors ---
val DarkBg: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) StaticDarkBg else StaticLightBg

val DisciplineNavy: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) StaticDisciplineNavy else StaticLightSecondary

val AppleBlue: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) StaticAppleBlueDark else StaticAppleBlueLight

val SuccessGreen: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) StaticSuccessGreenDark else StaticSuccessGreenLight

val HardRed: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) StaticHardRedDark else StaticHardRedLight

val ActionOrange: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) StaticActionOrangeDark else StaticActionOrangeLight

val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) StaticTextPrimaryDark else StaticTextPrimaryLight

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) StaticTextSecondaryDark else StaticTextSecondaryLight

val SteelGray: Color
    @Composable
    @ReadOnlyComposable
    get() = TextSecondary

// Theme Mapping for Backward Compatibility
val ObsidianBase: Color
    @Composable
    @ReadOnlyComposable
    get() = DarkBg

val DarkSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = DarkBg

val DarkCard: Color
    @Composable
    @ReadOnlyComposable
    get() = DisciplineNavy

val DarkCardAlt: Color
    @Composable
    @ReadOnlyComposable
    get() = DisciplineNavy

val PrimaryPurple: Color
    @Composable
    @ReadOnlyComposable
    get() = AppleBlue

val PrimaryPurpleLight: Color
    @Composable
    @ReadOnlyComposable
    get() = AppleBlue

val PrimaryPurpleDark: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) Color(0xFF004499) else Color(0xFF0055BB)

val XPGold: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) Color(0xFFFACC15) else Color(0xFFE5A900)

val XPGoldLight: Color
    @Composable
    @ReadOnlyComposable
    get() = Color(0xFFE5A900)

val XPGoldDark: Color
    @Composable
    @ReadOnlyComposable
    get() = Color(0xFFFACC15)

val LevelUpGold: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) Color(0xFFFACC15) else Color(0xFFE5A900)

val FireOrange: Color
    @Composable
    @ReadOnlyComposable
    get() = ActionOrange

val FireOrangeLight: Color
    @Composable
    @ReadOnlyComposable
    get() = ActionOrange

val HardRedLight: Color
    @Composable
    @ReadOnlyComposable
    get() = HardRed

val SuccessGreenLight: Color
    @Composable
    @ReadOnlyComposable
    get() = SuccessGreen

val TextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = TextSecondary

val OutlineGray: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) Color(0xFF38383A) else Color(0xFFD1D1D6)

val GrowthEmerald: Color
    @Composable
    @ReadOnlyComposable
    get() = SuccessGreen

// Card category fallback colors (styled cleanly to keep screens simple)
val CardConfidence: Color
    @Composable
    @ReadOnlyComposable
    get() = AppleBlue

val CardStrength: Color
    @Composable
    @ReadOnlyComposable
    get() = AppleBlue

val CardDiscipline: Color
    @Composable
    @ReadOnlyComposable
    get() = AppleBlue

val CardWisdom: Color
    @Composable
    @ReadOnlyComposable
    get() = AppleBlue

val CardFocus: Color
    @Composable
    @ReadOnlyComposable
    get() = AppleBlue
