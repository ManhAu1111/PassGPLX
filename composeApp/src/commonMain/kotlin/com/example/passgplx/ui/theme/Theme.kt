package com.example.passgplx.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

val PrimaryTeal = Color(0xFF00ADB5)
val BackgroundLight = Color(0xFFF0F5F9)
val SurfaceSilver = Color(0xFFC9D6DF)
val TextDark = Color(0xFF1E2022)

private val AppLightColorScheme = lightColorScheme(
    primary = PrimaryTeal,
    background = BackgroundLight,
    surface = Color.White,
    surfaceVariant = Color(0xFFEEF2F6), // Xám rất nhạt cho answer chưa chọn
    primaryContainer = Color(0xFFE0F7F8), // Light teal cho answer đã chọn
    onPrimaryContainer = TextDark,
    onPrimary = Color.White,
    onBackground = TextDark,
    onSurface = TextDark,
    onSurfaceVariant = Color(0xFF5F6368) // Xám trung cho text phụ
)

@Composable
fun PassGPLXTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppLightColorScheme,
        content = content
    )
}
