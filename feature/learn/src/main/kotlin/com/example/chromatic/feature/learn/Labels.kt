package com.example.chromatic.feature.learn

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.chromatic.core.data.achievement.Achievement
import com.example.chromatic.core.data.model.Practice

@Composable
fun Practice.label(): String = stringResource(when (this) {
    Practice.HARMONY -> R.string.practice_harmony
    Practice.CONTRAST -> R.string.practice_contrast
    Practice.PALETTE -> R.string.practice_palette
    Practice.GRID -> R.string.practice_grid
    Practice.ILLUSION -> R.string.practice_illusion
    Practice.MIX -> R.string.practice_mix
    Practice.THEME -> R.string.practice_theme
})

@Composable
fun Achievement.title(): String = stringResource(when (id) {
    "first_quiz" -> R.string.badge_first_steps
    "perfect_quiz" -> R.string.badge_flawless
    "sharp_eye" -> R.string.badge_sharp_eye
    "ten_rounds" -> R.string.badge_dedicated
    else -> R.string.badge_on_a_roll
})
