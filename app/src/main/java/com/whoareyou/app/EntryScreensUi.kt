package com.whoareyou.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CatalogUnavailableScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(V2Colors.Ink)
            .padding(horizontal = 28.dp, vertical = 36.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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

@Composable
fun OnboardingScreen(onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(V2Colors.Ink)
            .padding(horizontal = 28.dp, vertical = 36.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                stringResource(R.string.onboarding_title),
                color = V2Colors.TextPrimary,
                fontSize = 58.sp,
                lineHeight = 56.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(22.dp))
            Text(
                stringResource(R.string.onboarding_subtitle),
                color = V2Colors.AccentViolet,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.onboarding_body),
                color = V2Colors.TextSecondary,
                style = V2Type.Body
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 58.dp)
                    .testTag("onboarding_start"),
                colors = ButtonDefaults.buttonColors(containerColor = V2Colors.AccentViolet),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(stringResource(R.string.onboarding_start), fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.onboarding_no_account),
                color = V2Colors.TextSecondary,
                style = V2Type.Supporting
            )
        }
    }
}
