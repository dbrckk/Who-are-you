package com.whoareyou.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
    Column(modifier = modifier.fillMaxWidth()) {
        Button(
            onClick = onOpenHabits,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .testTag("profile_open_habits"),
            colors = ButtonDefaults.buttonColors(containerColor = V2Colors.SurfaceElevated),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(
                text = stringResource(R.string.habits_title),
                color = V2Colors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = stringResource(R.string.habits_local_only),
            color = V2Colors.TextSecondary,
            style = V2Type.Supporting,
            modifier = Modifier
                .padding(top = 8.dp)
                .testTag("profile_habits_privacy_hint")
        )
    }
}
