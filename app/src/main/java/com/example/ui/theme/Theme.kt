package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CoinFlipDarkColorScheme = darkColorScheme(
  primary = GoldPrimary,
  onPrimary = Color(0xFF221600),
  primaryContainer = GoldContainer,
  onPrimaryContainer = OnGoldContainer,
  secondary = SilverPrimary,
  onSecondary = Color(0xFF14243B),
  secondaryContainer = Color(0xFF223552),
  onSecondaryContainer = SilverLight,
  tertiary = VipPurple,
  background = BgDark,
  onBackground = TextPrimaryDark,
  surface = SurfaceDark,
  onSurface = TextPrimaryDark,
  surfaceVariant = SurfaceCardDark,
  onSurfaceVariant = TextSecondaryDark,
  outline = BorderLineDark,
  outlineVariant = Color(0xFF262638)
)

private val CoinFlipLightColorScheme = lightColorScheme(
  primary = GoldDark,
  onPrimary = Color.White,
  primaryContainer = GoldLight,
  onPrimaryContainer = Color(0xFF422E00),
  secondary = Color(0xFF33588A),
  onSecondary = Color.White,
  secondaryContainer = SilverPrimary,
  onSecondaryContainer = Color(0xFF14243B),
  tertiary = VipPurple,
  background = BgLight,
  onBackground = TextPrimaryLight,
  surface = SurfaceLight,
  onSurface = TextPrimaryLight,
  surfaceVariant = SurfaceCardLight,
  onSurfaceVariant = TextSecondaryLight,
  outline = BorderLineLight,
  outlineVariant = Color(0xFFE2E7F0)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) CoinFlipDarkColorScheme else CoinFlipLightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
