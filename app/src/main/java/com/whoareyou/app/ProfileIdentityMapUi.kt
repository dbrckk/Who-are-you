package com.whoareyou.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ProfileIdentityMap(summary: GlobalProfileSummary) {
    if (summary.dimensions.size < 3) return

    val map = remember(summary.dimensions) { ProfileIdentityMapEngine.build(summary.dimensions) }
    val axes = map.axes
    val mapDescription = stringResource(
        R.string.identity_map_accessibility,
        map.dominantSignalPercent,
        map.contrast,
        map.balancedAxes
    )

    Card(
        modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) { contentDescription = mapDescription },
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            V2Colors.SurfaceRaised,
                            Color(0xFF151525),
                            Color(0xFF101923)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Text(
                stringResource(R.string.identity_map_eyebrow),
                color = V2Colors.AccentCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(5.dp))
            Text(
                stringResource(R.string.identity_map_title),
                color = V2Colors.TextPrimary,
                fontSize = 23.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.identity_map_body),
                color = V2Colors.TextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(18.dp))

            Box(
                modifier = Modifier.fillMaxWidth().height(248.dp),
                contentAlignment = Alignment.Center
            ) {
                ProfileRadar(axes = axes, modifier = Modifier.size(226.dp))
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(V2Colors.Ink.copy(alpha = 0.82f), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✦", color = V2Colors.AccentViolet, fontSize = 23.sp, fontWeight = FontWeight.Black)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                axes.forEachIndexed { index, dimension ->
                    DimensionLegendRow(
                        index = index + 1,
                        dimension = dimension,
                        accent = if (index % 2 == 0) V2Colors.AccentViolet else V2Colors.AccentCyan
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                IdentityStat(
                    value = "${map.dominantSignalPercent}%",
                    label = stringResource(R.string.identity_map_dominant_signal),
                    modifier = Modifier.weight(1f)
                )
                IdentityStat(
                    value = map.contrast.toString(),
                    label = stringResource(R.string.identity_map_contrast),
                    modifier = Modifier.weight(1f)
                )
                IdentityStat(
                    value = map.balancedAxes.toString(),
                    label = stringResource(R.string.identity_map_balanced_axes),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Cached radar geometry: paths, axis angles and circles are rebuilt only when
 * the available size or profile axes change, not for every Compose draw frame.
 */
@Composable
private fun ProfileRadar(axes: List<ProfileDimension>, modifier: Modifier = Modifier) {
    val violet = V2Colors.AccentViolet
    val cyan = V2Colors.AccentCyan
    val grid = Color.White.copy(alpha = 0.11f)
    val spokes = Color.White.copy(alpha = 0.08f)
    val whiteDot = Color.White.copy(alpha = 0.9f)

    Box(modifier = modifier.drawWithCache {
        if (axes.size < 3) {
            onDrawBehind { }
        } else {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension * 0.44f
            val axisCount = axes.size
            val angleStep = 2 * PI / axisCount

            fun point(index: Int, scale: Float): Offset {
                val angle = -PI / 2 + index * angleStep
                return Offset(
                    x = center.x + cos(angle).toFloat() * radius * scale,
                    y = center.y + sin(angle).toFloat() * radius * scale
                )
            }

            val spokeEnds = List(axisCount) { index -> point(index, 1f) }
            val rings = listOf(0.25f, 0.5f, 0.75f, 1f).map { fraction ->
                Path().apply {
                    repeat(axisCount) { index ->
                        val p = point(index, fraction)
                        if (index == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                    }
                    close()
                }
            }
            val profileDots = axes.mapIndexed { index, dimension ->
                val scale = 0.24f + dimension.score.coerceIn(0, 100) / 100f * 0.76f
                point(index, scale)
            }
            val profilePath = Path().apply {
                profileDots.forEachIndexed { index, p ->
                    if (index == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                }
                close()
            }
            val gridStroke = Stroke(width = 1.4f)
            val profileStroke = Stroke(width = 3.4f)
            val profileFill = violet.copy(alpha = 0.18f)
            val profileOutline = violet.copy(alpha = 0.92f)

            onDrawBehind {
                rings.forEach { path ->
                    drawPath(path, color = grid, style = gridStroke)
                }
                spokeEnds.forEach { end ->
                    drawLine(color = spokes, start = center, end = end, strokeWidth = 1.2f)
                }
                drawPath(profilePath, color = profileFill)
                drawPath(profilePath, color = profileOutline, style = profileStroke)
                profileDots.forEachIndexed { index, p ->
                    drawCircle(
                        color = if (index % 2 == 0) violet else cyan,
                        radius = 6.5f,
                        center = p
                    )
                    drawCircle(color = whiteDot, radius = 2.3f, center = p)
                }
            }
        }
    })
}

@Composable
private fun DimensionLegendRow(index: Int, dimension: ProfileDimension, accent: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(accent.copy(alpha = 0.14f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(index.toString(), color = accent, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                dimension.title,
                color = V2Colors.TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                dimension.metricLabel,
                color = V2Colors.TextSecondary,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            "${dimension.score}%",
            color = accent,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun IdentityStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.055f), RoundedCornerShape(17.dp))
            .padding(horizontal = 11.dp, vertical = 11.dp)
    ) {
        Text(value, color = V2Colors.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            color = V2Colors.TextSecondary,
            fontSize = 9.sp,
            lineHeight = 12.sp,
            maxLines = 2
        )
    }
}
