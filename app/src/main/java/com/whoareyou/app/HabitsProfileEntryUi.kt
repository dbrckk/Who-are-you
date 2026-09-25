package com.whoareyou.app

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HabitsProfileEntry(
    onOpenHabits: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onOpenHabits,
        modifier = modifier
            .statusBarsPadding()
            .padding(top = 10.dp, end = V2Spacing.Screen)
            .heightIn(min = 48.dp)
            .testTag("profile_open_habits"),
        colors = ButtonDefaults.buttonColors(containerColor = V2Colors.SurfaceElevated)
    ) {
        Text(
            text = stringResource(R.string.habits_title),
            color = V2Colors.TextPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}
