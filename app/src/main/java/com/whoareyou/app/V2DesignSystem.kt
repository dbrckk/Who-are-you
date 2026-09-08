package com.whoareyou.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

object V2Type {
    val Eyebrow = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        fontWeight = FontWeight.Bold
    )
    val Hero = TextStyle(
        fontSize = 34.sp,
        lineHeight = 39.sp,
        letterSpacing = (-0.6).sp,
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
        lineHeight = 27.sp,
        letterSpacing = (-0.2).sp,
        fontWeight = FontWeight.Black
    )
    val Body = TextStyle(
        fontSize = 16.sp,
        lineHeight = 23.sp,
        fontWeight = FontWeight.Normal
    )
    val BodyStrong = TextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.SemiBold
    )
    val Metric = TextStyle(
        fontSize = 18.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Black
    )
    val Supporting = TextStyle(
        fontSize = 12.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Normal
    )
    val Caption = TextStyle(
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.2.sp,
        fontWeight = FontWeight.Bold
    )
}

object V2Motion {
    const val FastMillis = 110
    const val StandardMillis = 180
    const val PressedScale = 0.985f
}
