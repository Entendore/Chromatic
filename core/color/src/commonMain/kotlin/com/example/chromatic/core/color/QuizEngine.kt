package com.example.chromatic.core.color

import kotlin.math.roundToInt
import kotlin.random.Random

/** Reference into a consumer's string table — the engine holds no display strings (ADR #4). */
sealed interface QuizText {
    data class Key(val key: TextKey, val args: List<String> = emptyList()) : QuizText
    /** Proper nouns only — e.g. ColorNamer output (ADR #12: the corpus is not localized). */
    data class Raw(val value: String) : QuizText
}

enum class TextKey {
    PROMPT_HARMONY, PROMPT_CONTRAST, PROMPT_SHADE, PROMPT_WARMER, PROMPT_SATURATION,
    PROMPT_NAME, PROMPT_LIGHTER,
    EXPL_HARMONY, EXPL_CONTRAST, EXPL_SHADE, EXPL_WARMER, EXPL_SATURATION, EXPL_NAME,
    OPT_COMPLEMENTARY, OPT_ANALOGOUS, OPT_TRIADIC, OPT_SPLIT,
    OPT_TINT, OPT_SHADE, OPT_TONE, OPT_COMPLEMENT,
    OPT_WHITE_ON_DARK, OPT_WHITE_ON_LIGHT, OPT_COLOR_A, OPT_COLOR_B,
    S1_PROMPT, S1_A, S1_B, S1_C, S1_D, S1_EXPL,
    S2_PROMPT, S2_A, S2_B, S2_C, S2_D, S2_EXPL,
    S3_PROMPT, S3_A, S3_B, S3_C, S3_D, S3_EXPL,
    S4_PROMPT, S4_A, S4_B, S4_C, S4_D, S4_EXPL,
    S5_PROMPT, S5_A, S5_B, S5_C, S5_D, S5_EXPL,
    S6_PROMPT, S6_A, S6_B, S6_C, S6_D, S6_EXPL,
}

enum class QuestionKind { HARMONY, CONTRAST, SHADE, TEMPERATURE, SATURATION, NAME, STATIC }

data class QuizOption(val text: QuizText, val swatches: List<Rgb> = emptyList())

data class QuizQuestion(
    val kind: QuestionKind,
    val prompt: QuizText,
    val promptSwatches: List<Rgb> = emptyList(),
    val options: List<QuizOption>,
    val correctIndex: Int,
    val explanation: QuizText,
)

internal fun formatRatio(ratio: Double): String {
    val scaled = (ratio * 100).roundToInt()
    val whole = scaled / 100
    val frac = (scaled % 100).toString().padStart(2, '0')
    return "$whole.$frac"
}

object HueMatch {
    /** 100 at ΔE 0; ΔE 0.05 ≈ 94; ΔE 0.25 ≈ 70. Grades are a display concern. */
    fun score(target: Rgb, guess: Rgb): Int =
        (100.0 - deltaEOkLab(target, guess) * 120.0).roundToInt().coerceIn(0, 100)
}

/** Deterministic question generation — pass a seeded [Random] in tests. */
object QuizEngine {

    /** [staticBank] comes from the consumer (key-mapped static questions, v16). */
    fun generate(
        count: Int = 6,
        random: Random = Random.Default,
        staticBank: List<QuizQuestion> = emptyList(),
    ): List<QuizQuestion> {
        val generators = listOf(
            ::harmonyQuestion, ::contrastQuestion, ::shadeQuestion,
            ::temperatureQuestion, ::saturationQuestion,
        )
        val dynamic = generators.shuffled(random).take(3).map { it(random) }
        val static = staticBank.shuffled(random).take((count - dynamic.size).coerceAtLeast(0))
        return (dynamic + static).shuffled(random)
    }

    private fun harmonyQuestion(random: Random): QuizQuestion {
        val baseHue = random.nextFloat() * 360f
        val sat = 0.55f + random.nextFloat() * 0.3f
        val schemes = listOf(
            TextKey.OPT_COMPLEMENTARY to 180f, TextKey.OPT_ANALOGOUS to 30f,
            TextKey.OPT_TRIADIC to 120f, TextKey.OPT_SPLIT to 150f,
        )
        val correct = schemes[random.nextInt(schemes.size)]
        val partner = Hsv(baseHue + correct.second, sat, 0.9f).toRgb()
        val optionKeys = schemes.map { it.first }.shuffled(random)
        return QuizQuestion(
            kind = QuestionKind.HARMONY,
            prompt = QuizText.Key(TextKey.PROMPT_HARMONY),
            promptSwatches = listOf(Hsv(baseHue, sat, 0.9f).toRgb(), partner),
            options = optionKeys.map { QuizOption(QuizText.Key(it)) },
            correctIndex = optionKeys.indexOf(correct.first),
            explanation = QuizText.Key(TextKey.EXPL_HARMONY, listOf("${correct.second.toInt()}")),
        )
    }

    private fun contrastQuestion(random: Random): QuizQuestion {
        val hue = random.nextFloat() * 360f
        val dark = Hsl(hue, 0.55f, 0.25f + random.nextFloat() * 0.1f).toRgb()
        val light = Hsl(hue, 0.15f, 0.6f + random.nextFloat() * 0.15f).toRgb()
        require(WcagContrast.contrastRatio(Rgb.WHITE, dark) >= WcagContrast.AA_NORMAL)
        require(WcagContrast.contrastRatio(Rgb.WHITE, light) < WcagContrast.AA_NORMAL)

        val pairs = listOf(
            TextKey.OPT_WHITE_ON_DARK to (Rgb.WHITE to dark),
            TextKey.OPT_WHITE_ON_LIGHT to (Rgb.WHITE to light),
        ).let { if (random.nextBoolean()) it else it.reversed() }
        val correct = pairs.first {
            WcagContrast.contrastRatio(it.second.first, it.second.second) >= WcagContrast.AA_NORMAL
        }
        return QuizQuestion(
            kind = QuestionKind.CONTRAST,
            prompt = QuizText.Key(TextKey.PROMPT_CONTRAST),
            options = pairs.map { (key, pair) -> QuizOption(QuizText.Key(key), listOf(pair.second, pair.first)) },
            correctIndex = pairs.indexOf(correct),
            explanation = QuizText.Key(
                TextKey.EXPL_CONTRAST,
                listOf(formatRatio(WcagContrast.contrastRatio(correct.second.first, correct.second.second))),
            ),
        )
    }

    private fun shadeQuestion(random: Random): QuizQuestion {
        val hue = random.nextFloat() * 360f
        val sat = 0.5f + random.nextFloat() * 0.4f
        val light = 0.45f + random.nextFloat() * 0.2f
        val base = Hsl(hue, sat, light).toRgb()
        val defs = listOf(
            TextKey.OPT_TINT to Hsl(hue, sat, (light + 0.25f).coerceAtMost(1f)),
            TextKey.OPT_SHADE to Hsl(hue, sat, (light - 0.25f).coerceAtLeast(0f)),
            TextKey.OPT_TONE to Hsl(hue, sat * 0.5f, light),
            TextKey.OPT_COMPLEMENT to Hsl(hue + 180f, sat, light),
        )
        return QuizQuestion(
            kind = QuestionKind.SHADE,
            prompt = QuizText.Key(TextKey.PROMPT_SHADE),
            promptSwatches = listOf(base),
            options = defs.map { (key, hsl) -> QuizOption(QuizText.Key(key), listOf(hsl.toRgb())) },
            correctIndex = defs.indexOfFirst { it.first == TextKey.OPT_SHADE },
            explanation = QuizText.Key(TextKey.EXPL_SHADE),
        )
    }

    private fun temperatureQuestion(random: Random): QuizQuestion {
        val warm = Hsl(10f + random.nextFloat() * 40f, 0.65f, 0.55f).toRgb()
        val cool = Hsl(190f + random.nextFloat() * 70f, 0.65f, 0.55f).toRgb()
        val colors = if (random.nextBoolean()) listOf(warm, cool) else listOf(cool, warm)
        return QuizQuestion(
            kind = QuestionKind.TEMPERATURE,
            prompt = QuizText.Key(TextKey.PROMPT_WARMER),
            options = listOf(
                QuizOption(QuizText.Key(TextKey.OPT_COLOR_A), listOf(colors[0])),
                QuizOption(QuizText.Key(TextKey.OPT_COLOR_B), listOf(colors[1])),
            ),
            correctIndex = colors.indexOf(warm),
            explanation = QuizText.Key(TextKey.EXPL_WARMER, listOf(ColorNamer.nameOf(warm))),
        )
    }

    private fun saturationQuestion(random: Random): QuizQuestion {
        val hue = random.nextFloat() * 360f
        val light = 0.5f + random.nextFloat() * 0.15f
        val vivid = Hsl(hue, 0.55f + random.nextFloat() * 0.4f, light).toRgb()
        val dull = Hsl(hue, 0.15f + random.nextFloat() * 0.15f, light).toRgb()
        val colors = if (random.nextBoolean()) listOf(vivid, dull) else listOf(dull, vivid)
        return QuizQuestion(
            kind = QuestionKind.SATURATION,
            prompt = QuizText.Key(TextKey.PROMPT_SATURATION),
            options = listOf(
                QuizOption(QuizText.Key(TextKey.OPT_COLOR_A), listOf(colors[0])),
                QuizOption(QuizText.Key(TextKey.OPT_COLOR_B), listOf(colors[1])),
            ),
            correctIndex = colors.indexOf(vivid),
            explanation = QuizText.Key(TextKey.EXPL_SATURATION),
        )
    }
}

/** "Name it" — distractors from the same hue neighborhood; names are Raw (ADR #12). */
object NameGame {

    fun generate(random: Random): QuizQuestion {
        val hue = random.nextDouble() * 360.0
        val l = 0.50 + random.nextDouble() * 0.20
        val chroma = GamutMapper.maxChroma(l, hue) * (0.65 + random.nextDouble() * 0.30)
        val rgb = OkLch(l, chroma, hue).toRgbClamped()

        val correct = ColorNamer.nameOf(rgb)
        val correctHue = rgb.toOkLch().hueDegrees
        val pool = ColorNamer.namePool().filter { it != correct }
        val near = pool
            .mapNotNull { name -> ColorNamer.hueOf(name)?.let { name to it } }
            .filter { (_, h) -> angularDistance(h, correctHue) < 75.0 }
            .map { it.first }
        val distractors = (near + pool.filter { it !in near }).distinct().take(3)
        val options = (distractors + correct).shuffled(random)   // exactly one shuffle (v7 §0a)

        return QuizQuestion(
            kind = QuestionKind.NAME,
            prompt = QuizText.Key(TextKey.PROMPT_NAME),
            promptSwatches = listOf(rgb),
            options = options.map { QuizOption(QuizText.Raw(it)) },
            correctIndex = options.indexOf(correct),
            explanation = QuizText.Key(TextKey.EXPL_NAME, listOf(correct, "${correctHue.toInt()}")),
        )
    }

    private fun angularDistance(a: Double, b: Double): Double {
        val d = kotlin.math.abs(a - b) % 360.0
        return if (d > 180.0) 360.0 - d else d
    }
}

/** "Lighter?" — perceptual discrimination with an adaptive OKLab lightness gap. */
object LightnessGame {

    data class Round(val first: Rgb, val second: Rgb, val lighterIsFirst: Boolean)

    const val INITIAL_DIFFICULTY = 0.06
    const val MIN = 0.015
    const val MAX = 0.12

    fun round(deltaL: Double, random: Random): Round {
        val hue = random.nextDouble() * 360.0
        val chroma = GamutMapper.maxChroma(0.55, hue) * 0.75
        val baseL = 0.35 + random.nextDouble() * 0.30
        val darker = OkLch((baseL - deltaL / 2).coerceIn(0.06, 0.94), chroma, hue).toRgbClamped()
        val lighter = OkLch((baseL + deltaL / 2).coerceIn(0.06, 0.94), chroma, hue).toRgbClamped()
        return if (random.nextBoolean()) Round(darker, lighter, lighterIsFirst = false)
        else Round(lighter, darker, lighterIsFirst = true)
    }

    fun isCorrect(round: Round, choseFirst: Boolean): Boolean = choseFirst == round.lighterIsFirst

    /** Shrink the gap on hits, widen on misses. Pure — unit-tested. */
    fun nextDifficulty(correct: Boolean, current: Double): Double =
        (if (correct) current - 0.012 else current + 0.02).coerceIn(MIN, MAX)
}
