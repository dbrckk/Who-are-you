package com.whoareyou.app

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun CatalogUnavailableScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .background(
                Brush.verticalGradient(
                    listOf(V2Colors.Plum.copy(alpha = 0.54f), V2Colors.InkSoft, V2Colors.Ink)
                )
            )
            .padding(horizontal = 28.dp, vertical = 36.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .clip(RoundedCornerShape(V2Radius.Hero))
                .background(V2Colors.SurfaceGlass)
                .padding(horizontal = 24.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IdentityAura(score = 50, primary = V2Colors.Violet, secondary = V2Colors.Cyan)
            Spacer(Modifier.height(18.dp))
            Text(
                stringResource(R.string.catalog_unavailable_title),
                color = V2Colors.TextPrimary,
                style = V2Type.Question,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.catalog_unavailable_body),
                color = V2Colors.TextSecondary,
                style = V2Type.Body,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun OnboardingScreen(onStart: () -> Unit) {
    val ambient = rememberInfiniteTransition(label = "onboardingAmbient")
    val glowScale by ambient.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(V2Motion.AmbientMillis),
            repeatMode = RepeatMode.Reverse
        ),
        label = "onboardingGlowScale"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        V2Colors.Plum.copy(alpha = 0.72f),
                        V2Colors.InkSoft,
                        V2Colors.Ink,
                        V2Colors.Blue.copy(alpha = 0.08f)
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 70.dp, y = (-60).dp)
                .size(240.dp)
                .graphicsLayer {
                    scaleX = glowScale
                    scaleY = glowScale
                }
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(V2Colors.Orchid.copy(alpha = 0.22f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-70).dp, y = 75.dp)
                .size(230.dp)
                .graphicsLayer {
                    scaleX = 1.04f / glowScale
                    scaleY = 1.04f / glowScale
                }
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(V2Colors.Cyan.copy(alpha = 0.16f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .widthIn(max = 620.dp)
                .padding(horizontal = if (maxWidth >= 600.dp) 42.dp else 28.dp, vertical = 36.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(V2Radius.Hero))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                V2Colors.SurfaceGlass,
                                V2Colors.Violet.copy(alpha = 0.13f),
                                V2Colors.Cyan.copy(alpha = 0.07f)
                            )
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IdentityAura(score = 62, primary = V2Colors.Orchid, secondary = V2Colors.Cyan)
                Spacer(Modifier.height(18.dp))
                Text(
                    stringResource(R.string.onboarding_title),
                    color = V2Colors.TextPrimary,
                    style = V2Type.Hero,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(R.string.onboarding_subtitle),
                    color = V2Colors.Orchid,
                    style = V2Type.SectionTitle,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    stringResource(R.string.onboarding_body),
                    color = V2Colors.TextSecondary,
                    style = V2Type.Body,
                    textAlign = TextAlign.Center
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = onStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 58.dp)
                        .testTag("onboarding_start"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = V2Colors.Orchid,
                        contentColor = V2Colors.Ink
                    ),
                    shape = RoundedCornerShape(V2Radius.Compact)
                ) {
                    Text(
                        stringResource(R.string.onboarding_start),
                        style = V2Type.BodyStrong,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(R.string.onboarding_no_account),
                    color = V2Colors.TextSecondary,
                    style = V2Type.Supporting,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
