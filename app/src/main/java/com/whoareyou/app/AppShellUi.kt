package com.whoareyou.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// The persistent shell belongs only to top-level exploration spaces; focused quiz/result flows stay chrome-free.
enum class AppShellTab { DISCOVER, PROFILE }

object AppShellNavigation {
    fun tabFor(screen: AppScreen): AppShellTab? = when (screen) {
        AppScreen.DISCOVER -> AppShellTab.DISCOVER
        AppScreen.PROFILE -> AppShellTab.PROFILE
        AppScreen.QUIZ, AppScreen.RESULT -> null
    }

    fun destination(tab: AppShellTab): AppScreen = when (tab) {
        AppShellTab.DISCOVER -> AppScreen.DISCOVER
        AppShellTab.PROFILE -> AppScreen.PROFILE
    }

    fun isShellVisible(screen: AppScreen): Boolean = tabFor(screen) != null
}

@Composable
fun PremiumAppShellBar(
    selected: AppShellTab,
    onSelect: (AppShellTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, V2Colors.Ink.copy(alpha = 0.94f), V2Colors.Ink)
                )
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.linearGradient(
                        listOf(V2Colors.SurfaceGlass, V2Colors.SurfaceElevated.copy(alpha = 0.96f))
                    )
                )
                .padding(1.dp)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, V2Colors.AccentViolet.copy(alpha = 0.42f), V2Colors.AccentCyan.copy(alpha = 0.28f), Color.Transparent)
                        )
                    )
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ShellTab(
                    modifier = Modifier.weight(1f),
                    selected = selected == AppShellTab.DISCOVER,
                    symbol = "✦",
                    label = stringResource(R.string.shell_discover),
                    onClick = { onSelect(AppShellTab.DISCOVER) }
                )
                ShellTab(
                    modifier = Modifier.weight(1f),
                    selected = selected == AppShellTab.PROFILE,
                    symbol = "◉",
                    label = stringResource(R.string.shell_profile),
                    onClick = { onSelect(AppShellTab.PROFILE) }
                )
            }
        }
    }
}

@Composable
private fun ShellTab(
    modifier: Modifier,
    selected: Boolean,
    symbol: String,
    label: String,
    onClick: () -> Unit
) {
    val foreground = if (selected) V2Colors.TextPrimary else V2Colors.TextSecondary
    val accent = if (selected) V2Colors.AccentViolet else V2Colors.TextSecondary

    Row(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                if (selected) {
                    Brush.linearGradient(
                        listOf(
                            V2Colors.AccentViolet.copy(alpha = 0.18f),
                            V2Colors.AccentCyan.copy(alpha = 0.07f),
                            V2Colors.SurfaceElevated
                        )
                    )
                } else {
                    Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                }
            )
            .semantics(mergeDescendants = true) { }
            .selectable(
                selected = selected,
                role = Role.Tab,
                onClick = onClick
            )
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(
                    if (selected) {
                        Brush.linearGradient(
                            listOf(V2Colors.AccentViolet.copy(alpha = 0.24f), V2Colors.AccentCyan.copy(alpha = 0.10f))
                        )
                    } else {
                        Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = symbol,
                color = accent,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.clearAndSetSemantics { }
            )
        }
        Spacer(Modifier.size(9.dp))
        Text(
            label,
            color = foreground,
            fontSize = 12.sp,
            letterSpacing = 0.2.sp,
            fontWeight = FontWeight.Black
        )
    }
}
