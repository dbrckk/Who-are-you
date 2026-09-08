package com.whoareyou.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
                    listOf(Color.Transparent, V2Colors.Ink.copy(alpha = 0.98f))
                )
            )
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(V2Radius.Card))
                .background(V2Colors.SurfaceGlass)
                .padding(6.dp),
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

@Composable
private fun ShellTab(
    modifier: Modifier,
    selected: Boolean,
    symbol: String,
    label: String,
    onClick: () -> Unit
) {
    val background = if (selected) V2Colors.SurfaceElevated else Color.Transparent
    val foreground = if (selected) V2Colors.TextPrimary else V2Colors.TextSecondary
    val accent = if (selected) V2Colors.AccentViolet else V2Colors.TextSecondary

    Row(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(V2Radius.Compact))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (selected) accent.copy(alpha = 0.16f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(symbol, color = accent, fontSize = 15.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.size(8.dp))
        Text(label, color = foreground, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}
