package com.example.sololeveling90days.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// --- Static Color Constants ---
val StaticDarkBg = Color(0xFF000000)
val StaticLightBg = Color(0xFFFFFFFF)
val StaticDisciplineNavy = Color(0xFF1C1C1E)
val StaticLightSecondary = Color(0xFFF2F2F7)
val StaticAppleBlueDark = Color(0xFF0A84FF)
val StaticAppleBlueLight = Color(0xFF007AFF)

// 10% Semantic Status Colors
val StaticSuccessGreenDark = Color(0xFF30D158)
val StaticSuccessGreenLight = Color(0xFF34C759)
val StaticHardRedDark = Color(0xFFFF453A)
val StaticHardRedLight = Color(0xFFFF3B30)
val StaticActionOrangeDark = Color(0xFFFF9F0A)
val StaticActionOrangeLight = Color(0xFFFF9500)

// Text Colors
val StaticTextPrimaryDark = Color(0xFFFFFFFF)
val StaticTextPrimaryLight = Color(0xFF000000)
val StaticTextSecondaryDark = Color(0x99EBEBF5)
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
    get() = AppleBlue

val XPGoldLight: Color
    @Composable
    @ReadOnlyComposable
    get() = AppleBlue

val XPGoldDark: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) Color(0xFF004499) else Color(0xFF0055BB)

val LevelUpGold: Color
    @Composable
    @ReadOnlyComposable
    get() = AppleBlue

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
