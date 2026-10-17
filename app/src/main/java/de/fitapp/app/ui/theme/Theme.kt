package de.fitapp.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val Blue = Color(0xFF60A5FA)
private val BlueDeep = Color(0xFF17365C)
private val BlueLight = Color(0xFF93C5FD)
private val Background = Color(0xFF080D16)
private val Surface = Color(0xFF111927)
private val SurfaceVariant = Color(0xFF1C293B)
private val Outline = Color(0xFF3A4B63)

private val AppColors = darkColorScheme(
    primary = Blue,
    onPrimary = Color(0xFF07182B),
    primaryContainer = BlueDeep,
    onPrimaryContainer = Color(0xFFD5E8FF),
    secondary = BlueLight,
    onSecondary = Color(0xFF10243A),
    secondaryContainer = Color(0xFF203A58),
    onSecondaryContainer = Color(0xFFD7E9FF),
    tertiary = Color(0xFFB8D5FF),
    background = Background,
    onBackground = Color(0xFFE8EFFA),
    surface = Surface,
    onSurface = Color(0xFFE8EFFA),
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = Color(0xFFBAC8DA),
    outline = Outline,
    outlineVariant = Color(0xFF2A394E),
    error = Color(0xFFFF8A80),
    onError = Color(0xFF420000),
    errorContainer = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

private val BaseTypography = Typography()
private val AppTypography = BaseTypography.copy(
    headlineSmall = BaseTypography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = BaseTypography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = BaseTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = BaseTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
)

@Composable
fun FitAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AppColors, shapes = AppShapes, typography = AppTypography, content = content)
}

val BrandBlueDark = BlueDeep

