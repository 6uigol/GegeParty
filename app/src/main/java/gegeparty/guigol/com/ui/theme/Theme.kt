package gegeparty.guigol.com.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Gege {
    val Pink = Color(0xFFFF6F91)
    val PinkSoft = Color(0xFFFFE3EA)
    val Coral = Color(0xFFFF9671)
    val Yellow = Color(0xFFFFC75F)
    val YellowSoft = Color(0xFFFFF4DA)
    val Mint = Color(0xFF3DC9A9)
    val MintSoft = Color(0xFFDDF7F0)
    val Lilac = Color(0xFF9B8AFB)
    val LilacSoft = Color(0xFFECE8FF)
    val Sky = Color(0xFF5BA8F5)
    val SkySoft = Color(0xFFE2F0FF)
    val Ink = Color(0xFF2E2433)
    val InkSoft = Color(0xFF7A6E80)
    val Bg = Color(0xFFFFFBF7)
    val Line = Color(0xFFF1E7E4)

    val Hero = Brush.linearGradient(listOf(Pink, Coral, Yellow))
    val NoteColors = listOf(Color.White, PinkSoft, YellowSoft, MintSoft, LilacSoft, SkySoft)

    fun userColor(user: String) = when (user) {
        "baixinha" -> Pink
        "querido" -> Sky
        else -> Lilac
    }

    fun userSoft(user: String) = when (user) {
        "baixinha" -> PinkSoft
        "querido" -> SkySoft
        else -> LilacSoft
    }
}

private val scheme = lightColorScheme(
    primary = Gege.Pink,
    onPrimary = Color.White,
    primaryContainer = Gege.PinkSoft,
    onPrimaryContainer = Color(0xFF5C1027),
    secondary = Gege.Yellow,
    onSecondary = Gege.Ink,
    secondaryContainer = Gege.YellowSoft,
    onSecondaryContainer = Color(0xFF4A3300),
    tertiary = Gege.Mint,
    onTertiary = Color.White,
    tertiaryContainer = Gege.MintSoft,
    background = Gege.Bg,
    onBackground = Gege.Ink,
    surface = Color.White,
    onSurface = Gege.Ink,
    surfaceVariant = Color(0xFFFAF1EE),
    onSurfaceVariant = Gege.InkSoft,
    surfaceContainer = Color(0xFFFFF6F2),
    surfaceContainerLow = Color(0xFFFFFAF7),
    surfaceContainerHigh = Color(0xFFFCEFEA),
    outline = Color(0xFFE2D4D0),
    outlineVariant = Gege.Line,
    error = Color(0xFFE5484D),
)

private val base = Typography()
private val typography = Typography(
    displaySmall = base.displaySmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
    headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.3).sp),
    headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold),
    bodyMedium = TextStyle(fontFamily = FontFamily.Default, fontSize = 14.sp, lineHeight = 20.sp),
)

private val shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun GegeTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, typography = typography, shapes = shapes, content = content)
}
