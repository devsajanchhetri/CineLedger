package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val BebasNeueFontFamily = FontFamily(
  Font(R.font.bebas_neue, FontWeight.Normal),
  Font(R.font.bebas_neue, FontWeight.Bold),
)

val MontserratFontFamily = FontFamily(
  Font(R.font.montserrat, FontWeight.Normal),
  Font(R.font.montserrat, FontWeight.Medium),
  Font(R.font.montserrat, FontWeight.SemiBold),
  Font(R.font.montserrat, FontWeight.Bold),
)

val Typography = Typography(
  displayLarge = TextStyle(
    fontFamily = BebasNeueFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 48.sp,
    lineHeight = 52.sp,
    letterSpacing = 1.2.sp,
  ),
  displayMedium = TextStyle(
    fontFamily = BebasNeueFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 36.sp,
    lineHeight = 40.sp,
    letterSpacing = 1.sp,
  ),
  displaySmall = TextStyle(
    fontFamily = BebasNeueFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 28.sp,
    lineHeight = 32.sp,
    letterSpacing = 0.8.sp,
  ),
  headlineLarge = TextStyle(
    fontFamily = BebasNeueFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 26.sp,
    lineHeight = 30.sp,
    letterSpacing = 0.8.sp,
  ),
  headlineMedium = TextStyle(
    fontFamily = MontserratFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 20.sp,
    lineHeight = 26.sp,
    letterSpacing = (-0.2).sp,
  ),
  titleLarge = TextStyle(
    fontFamily = MontserratFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 18.sp,
    lineHeight = 24.sp,
  ),
  titleMedium = TextStyle(
    fontFamily = MontserratFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 15.sp,
    lineHeight = 21.sp,
  ),
  titleSmall = TextStyle(
    fontFamily = MontserratFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 13.sp,
    lineHeight = 18.sp,
  ),
  bodyLarge = TextStyle(
    fontFamily = MontserratFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 22.sp,
  ),
  bodyMedium = TextStyle(
    fontFamily = MontserratFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 19.sp,
  ),
  bodySmall = TextStyle(
    fontFamily = MontserratFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
  ),
  labelLarge = TextStyle(
    fontFamily = MontserratFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.4.sp,
  ),
  labelMedium = TextStyle(
    fontFamily = MontserratFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 11.sp,
    lineHeight = 15.sp,
    letterSpacing = 0.5.sp,
  ),
  labelSmall = TextStyle(
    fontFamily = MontserratFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 10.sp,
    lineHeight = 14.sp,
    letterSpacing = 0.6.sp,
  ),
)
