package com.sagor.fatloss.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sagor.fatloss.AppGraph

val Green = Color(0xFFC3F400)
val Blue = Color(0xFF00E3FD)
val Red = Color(0xFFFFB4AB)
val Amber = Color(0xFFD29922)
val Coral = Color(0xFFFFB4A2)
val Bg = Color(0xFF131313)
val Surface = Color(0xFF1C1B1B)
val Surface2 = Color(0xFF2A2A2A)
val Surface3 = Color(0xFF353534)
val TextColor = Color(0xFFE5E2E1)
val Muted = Color(0xFFC4C9AC)
val Outline = Color(0xFF444933)

val LocalAppGraph = staticCompositionLocalOf<AppGraph> { error("AppGraph missing") }

@Composable
fun SagorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Green,
            secondary = Blue,
            tertiary = Amber,
            background = Bg,
            surface = Surface,
            surfaceVariant = Surface2,
            onPrimary = Bg,
            onSecondary = Bg,
            onTertiary = Bg,
            onBackground = TextColor,
            onSurface = TextColor,
            onSurfaceVariant = TextColor,
            error = Red
        ),
        typography = Typography(
            headlineLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                fontSize = 34.sp,
                lineHeight = 38.sp
            ),
            headlineMedium = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                fontSize = 28.sp,
                lineHeight = 32.sp
            ),
            titleMedium = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                lineHeight = 24.sp
            ),
            bodyMedium = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                lineHeight = 22.sp
            ),
            labelMedium = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        ),
        content = content
    )
}
