package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NetflixCinemaColorScheme = darkColorScheme(
  primary = NetflixRed,
  onPrimary = NetflixWhite,
  primaryContainer = NetflixRedDark,
  onPrimaryContainer = NetflixWhite,
  secondary = NetflixMatchGreen,
  onSecondary = Color.Black,
  secondaryContainer = NetflixMatchGreenSoft,
  onSecondaryContainer = NetflixMatchGreen,
  tertiary = NetflixGold,
  onTertiary = Color.Black,
  tertiaryContainer = NetflixAmberSoft,
  onTertiaryContainer = NetflixGold,
  background = NetflixBlack,
  onBackground = NetflixWhite,
  surface = NetflixSurface,
  onSurface = NetflixWhite,
  surfaceVariant = NetflixCard,
  onSurfaceVariant = NetflixSilver,
  surfaceContainer = NetflixCard,
  surfaceContainerHigh = NetflixCardElevated,
  outline = NetflixBorder,
  error = NetflixRedSoft,
  onError = Color.Black,
)

@Composable
fun CineLedgerTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = NetflixCinemaColorScheme,
    typography = Typography,
    content = content,
  )
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  CineLedgerTheme(content = content)
}
