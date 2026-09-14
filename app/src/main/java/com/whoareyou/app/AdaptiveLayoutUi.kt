package com.whoareyou.app

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

fun Modifier.readableContentWidth(): Modifier =
    this
        .wrapContentWidth(Alignment.CenterHorizontally)
        .widthIn(max = 840.dp)
        .fillMaxWidth()
