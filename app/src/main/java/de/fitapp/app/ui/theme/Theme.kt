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

private val Lime = Color(0xFFC9F36A)
private val LimeDeep = Color(0xFF33451D)
private val Teal = Color(0xFF73DCC4)
private val Background = Color(0xFF101411)
private val Surface = Color(0xFF191F1A)
private val SurfaceVariant = Color(0xFF242C25)
private val Outline = Color(0xFF414A41)

private val AppColors = darkColorScheme(
    primary = Lime,
    onPrimary = Color(0xFF1B260B),
    primaryContainer = LimeDeep,
    onPrimaryContainer = Color(0xFFE2F8BD),
    secondary = Teal,
    onSecondary = Color(0xFF00372D),
    secondaryContainer = Color(0xFF194A40),
    onSecondaryContainer = Color(0xFFB8F4E5),
    tertiary = Color(0xFFB7C8FF),
    background = Background,
    onBackground = Color(0xFFE8EEE8),
    surface = Surface,
    onSurface = Color(0xFFE8EEE8),
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = Color(0xFFB9C2B9),
    outline = Outline,
    outlineVariant = Color(0xFF303830),
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

val BrandBlueDark = LimeDeep
