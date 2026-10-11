package com.example.chromatic.feature.quiz

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.chromatic.core.color.QuizText
import com.example.chromatic.core.color.TextKey

/** Engine keys → module resources. Resolution happens ONLY at render time. */
internal fun textRes(key: TextKey): Int = when (key) {
    TextKey.PROMPT_HARMONY -> R.string.q_prompt_harmony
    TextKey.PROMPT_CONTRAST -> R.string.q_prompt_contrast
    TextKey.PROMPT_SHADE -> R.string.q_prompt_shade
    TextKey.PROMPT_WARMER -> R.string.q_prompt_warmer
    TextKey.PROMPT_SATURATION -> R.string.q_prompt_saturation
    TextKey.PROMPT_NAME -> R.string.q_prompt_name
    TextKey.PROMPT_LIGHTER -> R.string.q_prompt_lighter_inline
    TextKey.EXPL_HARMONY -> R.string.q_expl_harmony
    TextKey.EXPL_CONTRAST -> R.string.q_expl_contrast
    TextKey.EXPL_SHADE -> R.string.q_expl_shade
    TextKey.EXPL_WARMER -> R.string.q_expl_warmer
    TextKey.EXPL_SATURATION -> R.string.q_expl_saturation
    TextKey.EXPL_NAME -> R.string.q_expl_name
    TextKey.OPT_COMPLEMENTARY -> R.string.q_opt_complementary
    TextKey.OPT_ANALOGOUS -> R.string.q_opt_analogous
    TextKey.OPT_TRIADIC -> R.string.q_opt_triadic
    TextKey.OPT_SPLIT -> R.string.q_opt_split
    TextKey.OPT_TINT -> R.string.q_opt_tint
    TextKey.OPT_SHADE -> R.string.q_opt_shade
    TextKey.OPT_TONE -> R.string.q_opt_tone
    TextKey.OPT_COMPLEMENT -> R.string.q_opt_complement
    TextKey.OPT_WHITE_ON_DARK -> R.string.q_opt_white_on_dark
    TextKey.OPT_WHITE_ON_LIGHT -> R.string.q_opt_white_on_light
    TextKey.OPT_COLOR_A -> R.string.q_opt_color_a
    TextKey.OPT_COLOR_B -> R.string.q_opt_color_b
    TextKey.S1_PROMPT -> R.string.q_s1_prompt
    TextKey.S1_A -> R.string.q_s1_a
    TextKey.S1_B -> R.string.q_s1_b
    TextKey.S1_C -> R.string.q_s1_c
    TextKey.S1_D -> R.string.q_s1_d
    TextKey.S1_EXPL -> R.string.q_s1_expl
    TextKey.S2_PROMPT -> R.string.q_s2_prompt
    TextKey.S2_A -> R.string.q_s2_a
    TextKey.S2_B -> R.string.q_s2_b
    TextKey.S2_C -> R.string.q_s2_c
    TextKey.S2_D -> R.string.q_s2_d
    TextKey.S2_EXPL -> R.string.q_s2_expl
    TextKey.S3_PROMPT -> R.string.q_s3_prompt
    TextKey.S3_A -> R.string.q_s3_a
    TextKey.S3_B -> R.string.q_s3_b
    TextKey.S3_C -> R.string.q_s3_c
    TextKey.S3_D -> R.string.q_s3_d
    TextKey.S3_EXPL -> R.string.q_s3_expl
    TextKey.S4_PROMPT -> R.string.q_s4_prompt
    TextKey.S4_A -> R.string.q_s4_a
    TextKey.S4_B -> R.string.q_s4_b
    TextKey.S4_C -> R.string.q_s4_c
    TextKey.S4_D -> R.string.q_s4_d
    TextKey.S4_EXPL -> R.string.q_s4_expl
    TextKey.S5_PROMPT -> R.string.q_s5_prompt
    TextKey.S5_A -> R.string.q_s5_a
    TextKey.S5_B -> R.string.q_s5_b
    TextKey.S5_C -> R.string.q_s5_c
    TextKey.S5_D -> R.string.q_s5_d
    TextKey.S5_EXPL -> R.string.q_s5_expl
    TextKey.S6_PROMPT -> R.string.q_s6_prompt
    TextKey.S6_A -> R.string.q_s6_a
    TextKey.S6_B -> R.string.q_s6_b
    TextKey.S6_C -> R.string.q_s6_c
    TextKey.S6_D -> R.string.q_s6_d
    TextKey.S6_EXPL -> R.string.q_s6_expl
}

@Composable
fun QuizText.resolve(): String = when (this) {
    is QuizText.Key -> stringResource(textRes(key), *args.toTypedArray())
    is QuizText.Raw -> value
}
