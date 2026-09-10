package com.whoareyou.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object V2Colors {
    // Deep blue-black foundation keeps long-form quiz reading comfortable while
    // allowing accents to feel luminous rather than neon-on-black.
    val Ink = Color(0xFF070914)
    val InkSoft = Color(0xFF0B0F1D)
    val Surface = Color(0xFF11162A)
    val SurfaceRaised = Color(0xFF18213A)
    val SurfaceGlass = Color(0xE0161C31)

    // Brand spectrum: violet -> electric blue -> aqua, with a warm counter-accent.
    val Violet = Color(0xFF927CFF)
    val VioletBright = Color(0xFFB6A8FF)
    val Blue = Color(0xFF5EA1FF)
    val Cyan = Color(0xFF5DE1E6)
    val Coral = Color(0xFFFF8FA3)
    val Success = Color(0xFF66E0A3)

    val TextPrimary = Color(0xFFF8F9FF)
    val TextSecondary = Color(0xFFB7BED2)
    val TextTertiary = Color(0xFF8790AA)
    val Hairline = Color(0x24FFFFFF)
    val HairlineStrong = Color(0x3DFFFFFF)
    val OverlayStrong = Color(0xD9070914)
    val OverlaySoft = Color(0x33070914)

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
