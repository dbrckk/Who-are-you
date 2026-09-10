package com.whoareyou.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object V2Colors {
    // Deep plum-blue foundation: softer than pure black while retaining strong contrast.
    val Ink = Color(0xFF090A16)
    val InkSoft = Color(0xFF101124)
    val Plum = Color(0xFF251C3A)
    val Surface = Color(0xFF17182D)
    val SurfaceRaised = Color(0xFF20223C)
    val SurfaceGlass = Color(0xE01B1C34)

    // Inclusive brand spectrum: lavender and orchid lead, balanced by blue/aqua and warm peach.
    val Violet = Color(0xFFA58BFF)
    val VioletBright = Color(0xFFC8B9FF)
    val Orchid = Color(0xFFE59BEF)
    val Rose = Color(0xFFFFA7C4)
    val Peach = Color(0xFFFFB49E)
    val Blue = Color(0xFF72A7FF)
    val Cyan = Color(0xFF6DDBDD)
    val Success = Color(0xFF72DDA7)

    // Compatibility alias used by existing warm-accent surfaces.
    val Coral = Rose

    val TextPrimary = Color(0xFFFAF8FF)
    val TextSecondary = Color(0xFFC6C3D5)
    val TextTertiary = Color(0xFF9693AB)
    val Hairline = Color(0x24FFFFFF)
    val HairlineStrong = Color(0x3DFFFFFF)
    val OverlayStrong = Color(0xD9090A16)
    val OverlaySoft = Color(0x33090A16)

    val SurfaceElevated = SurfaceRaised
    val AccentViolet = Violet
    val AccentCyan = Cyan
}

object V2Spacing {
    val Screen = 20.dp
    val Section = 20.dp
    val Card = 18.dp
    val Compact = 10.dp
}

object V2Radius {
    val Hero = 32.dp
    val Card = 26.dp
    val Compact = 18.dp
    val Pill = 50.dp
}

object V2Type {
    val Eyebrow = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.7.sp,
        fontWeight = FontWeight.Bold
    )
    val Hero = TextStyle(
        fontSize = 36.sp,
        lineHeight = 41.sp,
        letterSpacing = (-0.7).sp,
        fontWeight = FontWeight.Black
    )
    val Question = TextStyle(
        fontSize = 29.sp,
        lineHeight = 35.sp,
        letterSpacing = (-0.35).sp,
        fontWeight = FontWeight.Black
    )
    val SectionTitle = TextStyle(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.2).sp,
        fontWeight = FontWeight.Black
    )
    val Body = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Normal
    )
    val BodyStrong = TextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.SemiBold
    )
    val Metric = TextStyle(
        fontSize = 20.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Black
    )
    val Supporting = TextStyle(
        fontSize = 13.sp,
        lineHeight = 19.sp,
        fontWeight = FontWeight.Normal
    )
    val Caption = TextStyle(
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.2.sp,
        fontWeight = FontWeight.Bold
    )
}

object V2Motion {
    const val FastMillis = 120
    const val StandardMillis = 220
    const val EmphasizedMillis = 360
    const val AmbientMillis = 3200
    const val PressedScale = 0.975f
}
