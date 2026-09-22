package com.whoareyou.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun ResultEvidenceCard(
    evidence: List<ResultEvidence>,
    modifier: Modifier = Modifier
) {
    if (evidence.isEmpty()) return

    Card(
        colors = CardDefaults.cardColors(containerColor = V2Colors.SurfaceElevated),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(V2Radius.Card),
        modifier = modifier
            .fillMaxWidth()
            .testTag("result_evidence")
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Why this result?",
                color = V2Colors.TextPrimary,
                style = V2Type.BodyStrong
            )
            evidence.forEach { item ->
                Column {
                    Text(
                        text = item.questionText,
                        color = V2Colors.TextPrimary,
                        style = V2Type.BodyStrong
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = item.answerText,
                        color = V2Colors.TextSecondary,
                        style = V2Type.Supporting
                    )
                }
            }
        }
    }
}
