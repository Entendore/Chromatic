package com.example.chromatic.feature.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.chromatic.core.color.ColorOfDay
import com.example.chromatic.core.color.PaletteFactory
import com.example.chromatic.core.designsystem.components.toComposeColor
import kotlinx.coroutines.launch

private val PAGE_COUNT = 3

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val scope = rememberCoroutineScope()
    // The app's own engine paints its own pitch (v18 §3): today's color, live.
    val swatches = remember { PaletteFactory.tonalScale(ColorOfDay.today()).map { it.rgb } }

    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(0.6f))
        HorizontalPager(state = pagerState, modifier = Modifier.weight(3f)) { page ->
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (page == 0) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        swatches.take(5).forEach { rgb ->
                            Box(
                                Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(rgb.toComposeColor())
                            )
                        }
                    }
                }
                Text(
                    when (page) {
                        0 -> stringResource(R.string.onboarding_1_title)
                        1 -> stringResource(R.string.onboarding_2_title)
                        else -> stringResource(R.string.onboarding_3_title)
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )
                Text(
                    when (page) {
                        0 -> stringResource(R.string.onboarding_1_body)
                        1 -> stringResource(R.string.onboarding_2_body)
                        else -> stringResource(R.string.onboarding_3_body)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.weight(0.4f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(PAGE_COUNT) { i ->
                Box(
                    Modifier
                        .size(if (pagerState.currentPage == i) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (pagerState.currentPage == i) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                if (pagerState.currentPage == PAGE_COUNT - 1) onDone()
                else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (pagerState.currentPage == PAGE_COUNT - 1) stringResource(R.string.onboarding_start)
                else stringResource(R.string.onboarding_next)
            )
        }
        TextButton(
            onClick = onDone,
            enabled = pagerState.currentPage == PAGE_COUNT - 1,   // buyers see all three pages
        ) { Text(stringResource(R.string.onboarding_skip)) }
    }
}
