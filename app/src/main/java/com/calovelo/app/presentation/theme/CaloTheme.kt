package com.calovelo.app.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class CaloThemeId { EmeraldDark, EmeraldLight, ThermalDark }

data class CaloTokens(
    val themeId: CaloThemeId,
    val background: Color,
    val surface: Color,
    val surfaceAlpha: Float,
    val border: Color,
    val text: Color,
    val muted: Color,
    val startFill: Color,
    val startEnd: Color,
    val onStart: Color,
    val scanFill: Color,
    val onScan: Color,
    val energyMark: Color,
    val energyInk: Color,
    val nutritionMark: Color,
    val nutritionInk: Color,
    val aiMark: Color,
    val aiInk: Color,
    val hydrationMark: Color,
    val hydrationInk: Color,
    val waterContainer: Color,
    val waterInk: Color,
    val proteinMark: Color,
    val proteinInk: Color,
    val carbsMark: Color,
    val carbsInk: Color,
    val fatMark: Color,
    val fatInk: Color,
    val heartMark: Color,
    val mapBackground: Color,
    val mapRoad: Color,
    val mapRoadBorder: Color,
    val mapWater: Color,
    val mapPark: Color,
    val mapRoute: Color,
    val mapRouteGlow: Color,
    val mapBeacon: Color,
    val greenStateContainer: Color?,
    val nutritionStateContainer: Color?,
    val aiStateContainer: Color?,
    val sportsNumerals: Boolean
)

val EmeraldDarkTokens = CaloTokens(
    themeId = CaloThemeId.EmeraldDark,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    surfaceAlpha = 0.80f,
    border = Color(0xFF334155),
    text = Color(0xFFF8FAFC),
    muted = Color(0xFF94A3B8),
    startFill = Color(0xFF00E676),
    startEnd = Color(0xFF00E676),
    onStart = Color(0xFF0F172A),
    scanFill = Color(0xFFFF6D00),
    onScan = Color(0xFF0F172A),
    energyMark = Color(0xFF00E676),
    energyInk = Color(0xFF00E676),
    nutritionMark = Color(0xFFFF6D00),
    nutritionInk = Color(0xFFFF6D00),
    aiMark = Color(0xFF00E676),
    aiInk = Color(0xFF00E676),
    hydrationMark = Color(0xFF00E5FF),
    hydrationInk = Color(0xFF00E5FF),
    waterContainer = Color(0xFF123D50),
    waterInk = Color(0xFF00E5FF),
    proteinMark = Color(0xFFFF5252),
    proteinInk = Color(0xFFFF5252),
    carbsMark = Color(0xFFFFD600),
    carbsInk = Color(0xFFFFD600),
    fatMark = Color(0xFF00E5FF),
    fatInk = Color(0xFF00E5FF),
    heartMark = Color(0xFFFF5252),
    mapBackground = Color(0xFF111D30),
    mapRoad = Color(0xFF1D2D43),
    mapRoadBorder = Color(0xFF2A3B50),
    mapWater = Color(0xFF153344),
    mapPark = Color(0xFF17332E),
    mapRoute = Color(0xFF00E676),
    mapRouteGlow = Color(0xFF00E676),
    mapBeacon = Color(0xFF00E676),
    greenStateContainer = Color(0xFF164335),
    nutritionStateContainer = Color(0xFF493021),
    aiStateContainer = Color(0xFF153729),
    sportsNumerals = false
)

val EmeraldLightTokens = EmeraldDarkTokens.copy(
    themeId = CaloThemeId.EmeraldLight,
    background = Color(0xFFF5F8F7),
    surface = Color.White,
    surfaceAlpha = 0.94f,
    border = Color(0xFFDCE6E2),
    text = Color(0xFF0F172A),
    muted = Color(0xFF5E7083),
    energyMark = Color(0xFF059669),
    energyInk = Color(0xFF047857),
    nutritionMark = Color(0xFFF97316),
    nutritionInk = Color(0xFFB74700),
    aiMark = Color(0xFF059669),
    aiInk = Color(0xFF047857),
    hydrationMark = Color(0xFF0891B2),
    hydrationInk = Color(0xFF007C91),
    waterContainer = Color(0xFFE0F5FC),
    waterInk = Color(0xFF007C91),
    proteinMark = Color(0xFFE54D5A),
    proteinInk = Color(0xFFBF3543),
    carbsMark = Color(0xFFD5A600),
    carbsInk = Color(0xFF856300),
    fatMark = Color(0xFF0891B2),
    fatInk = Color(0xFF007C91),
    heartMark = Color(0xFFE54D5A),
    mapBackground = Color(0xFFEAF1ED),
    mapRoad = Color.White,
    mapRoadBorder = Color(0xFFD1DED6),
    mapWater = Color(0xFFCEE9F2),
    mapPark = Color(0xFFD4EBDD),
    mapRoute = Color(0xFF059669),
    mapRouteGlow = Color(0xFF059669),
    mapBeacon = Color(0xFF059669),
    greenStateContainer = Color(0xFFE0F5E8),
    nutritionStateContainer = Color(0xFFFFF0E3),
    aiStateContainer = Color(0xFFE1F5E9)
)

val ThermalDarkTokens = CaloTokens(
    themeId = CaloThemeId.ThermalDark,
    background = Color(0xFF08090E),
    surface = Color(0xFF141827),
    surfaceAlpha = 0.90f,
    border = Color(0xFF2B3044),
    text = Color(0xFFF8FAFC),
    muted = Color(0xFFA2ADC2),
    startFill = Color(0xFFFF5733),
    startEnd = Color(0xFFFF8D4D),
    onStart = Color(0xFF08090E),
    scanFill = Color(0xFF8B5CF6),
    onScan = Color(0xFFF8FAFC),
    energyMark = Color(0xFFFF5733),
    energyInk = Color(0xFFFF8D4D),
    nutritionMark = Color(0xFFFF5733),
    nutritionInk = Color(0xFFFF8D4D),
    aiMark = Color(0xFF8B5CF6),
    aiInk = Color(0xFFC084FC),
    hydrationMark = Color(0xFF38BDF8),
    hydrationInk = Color(0xFF38BDF8),
    waterContainer = Color(0xFF12364B),
    waterInk = Color(0xFF38BDF8),
    proteinMark = Color(0xFFF43F5E),
    proteinInk = Color(0xFFF43F5E),
    carbsMark = Color(0xFFFFD166),
    carbsInk = Color(0xFFFFD166),
    fatMark = Color(0xFF38BDF8),
    fatInk = Color(0xFF38BDF8),
    heartMark = Color(0xFFF43F5E),
    mapBackground = Color(0xFF0D1524),
    mapRoad = Color(0xFF1B263A),
    mapRoadBorder = Color(0xFF29354B),
    mapWater = Color(0xFF102D3C),
    mapPark = Color(0xFF142B29),
    mapRoute = Color(0xFFB892FF),
    mapRouteGlow = Color(0xFF8B5CF6),
    mapBeacon = Color(0xFFFF5733),
    greenStateContainer = null,
    nutritionStateContainer = Color(0xFF33201C),
    aiStateContainer = Color(0xFF302140),
    sportsNumerals = true
)

val LocalCaloTokens = staticCompositionLocalOf { EmeraldDarkTokens }

@Composable
fun CaloTheme(
    themeId: CaloThemeId = CaloThemeId.EmeraldDark,
    content: @Composable () -> Unit
) {
    val tokens = when (themeId) {
        CaloThemeId.EmeraldDark -> EmeraldDarkTokens
        CaloThemeId.EmeraldLight -> EmeraldLightTokens
        CaloThemeId.ThermalDark -> ThermalDarkTokens
    }
    CompositionLocalProvider(LocalCaloTokens provides tokens) {
        content()
    }
}

object CaloTheme {
    val tokens: CaloTokens
        @Composable
        get() = LocalCaloTokens.current
}
