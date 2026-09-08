package com.whoareyou.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object V2Colors {
    val Ink = Color(0xFF080A10)
    val Surface = Color(0xFF12151E)
    val SurfaceRaised = Color(0xFF181C27)
    val SurfaceGlass = Color(0xD91B1F2B)
    val Violet = Color(0xFFA58BFF)
    val Cyan = Color(0xFF72E5F6)
    val TextPrimary = Color(0xFFF7F7FB)
    val TextSecondary = Color(0xFFAAAFBE)
    val Hairline = Color(0x18FFFFFF)
    val OverlayStrong = Color(0xD9080A10)
    val OverlaySoft = Color(0x2A080A10)

    // Semantic aliases keep feature UI readable while preserving one palette source.
    val SurfaceElevated = SurfaceRaised
    val AccentViolet = Violet
    val AccentCyan = Cyan
}

object V2Spacing {
    val Screen = 20.dp
    val Section = 18.dp
    val Card = 16.dp
    val Compact = 10.dp
}

object V2Radius {
    val Hero = 30.dp
    val Card = 26.dp
    val Compact = 20.dp
    val Pill = 50.dp
}
