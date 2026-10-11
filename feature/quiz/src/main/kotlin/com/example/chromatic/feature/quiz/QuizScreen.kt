package com.example.chromatic.feature.quiz

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chromatic.core.color.LightnessGame
import com.example.chromatic.core.designsystem.components.LabeledSlider
import com.example.chromatic.core.designsystem.components.toComposeColor
import kotlin.math.roundToInt

enum class QuizMode { KNOWLEDGE, HUE_MATCH, NAME_COLOR, LIGHTNESS }

@Composable
fun QuizMode.label(): String = stringResource(when (this) {
    QuizMode.KNOWLEDGE -> R.string.quiz_mode_knowledge
    QuizMode.HUE_MATCH -> R.string.quiz_mode_hue
    QuizMode.NAME_COLOR -> R.string.quiz_mode_name
    QuizMode.LIGHTNESS -> R.string.quiz_mode_lighter
})

private fun hueGrade(score: Int): String = when {
    score >= 90 -> stringResource(R.string.grade_sharp_eye)
    score >= 75 -> stringResource(R.string.grade_great)
    score >= 60 -> stringResource(R.string.grade_solid)
    score >= 40 -> stringResource(R.string.grade_practice)
    else -> stringResource(R.string.grade_trust_dials)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    knowledgeViewModel: KnowledgeQuizViewModel = hiltViewModel(),
    hueViewModel: HueMatchViewModel = hiltViewModel(),
    nameViewModel: NameColorViewModel = hiltViewModel(),
    lightnessViewModel: LightnessViewModel = hiltViewModel(),
) {
    var modeName by rememberSaveable { mutableStateOf(QuizMode.KNOWLEDGE.name) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Text(
            stringResource(R.string.quiz_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 16.dp),
        )
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            QuizMode.entries.forEach { mode ->
                SegmentedButton(
                    selected = modeName == mode.name,
                    onClick = { modeName = mode.name },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = mode.ordinal, count = QuizMode.entries.size,
                    ),
                ) { Text(mode.label()) }
            }
        }
        Spacer(Modifier.height(16.dp))
        AnimatedContent(targetState = modeName, label = "quizMode") { name ->
            when (QuizMode.valueOf(name)) {
                QuizMode.KNOWLEDGE -> KnowledgeQuiz(knowledgeViewModel)
                QuizMode.HUE_MATCH -> HueMatchGame(hueViewModel)
                QuizMode.NAME_COLOR -> NameColorGame(nameViewModel)
                QuizMode.LIGHTNESS -> LightnessGameScreen(lightnessViewModel)
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun KnowledgeQuiz(viewModel: KnowledgeQuizViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    if (state.finished) {
        QuizSummary(
            score = state.score,
            total = state.questions.size,
            message = when (state.score) {
                state.questions.size -> stringResource(R.string.quiz_perfect)
                in (state.questions.size - 2) until state.questions.size -> stringResource(R.string.quiz_almost)
                else -> stringResource(R.string.quiz_revisit)
            },
            onRestart = viewModel::start,
        )
    } else {
        state.current?.let { question ->
            QuestionCard(
                question = question,
                index = state.index,
                total = state.questions.size,
                selected = state.selected,
                onSelect = viewModel::onSelect,
                onNext = viewModel::onNext,
            )
        }
    }
}

@Composable
private fun NameColorGame(viewModel: NameColorViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    if (state.finished) {
        QuizSummary(state.score, state.questions.size, stringResource(R.string.quiz_revisit), viewModel::restart)
    } else {
        state.current?.let { question ->
            QuestionCard(
                question = question,
                index = state.index,
                total = state.questions.size,
                selected = state.selected,
                onSelect = viewModel::onSelect,
                onNext = viewModel::onNext,
            )
        }
    }
}

@Composable
private fun QuestionCard(
    question: com.example.chromatic.core.color.QuizQuestion,
    index: Int,
    total: Int,
    selected: Int?,
    onSelect: (Int) -> Unit,
    onNext: () -> Unit,
) {
    val revealed = selected != null
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                stringResource(R.string.quiz_question_of, index + 1, total),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LinearProgressIndicator(
                progress = { (index + if (revealed) 1 else 0) / total.toFloat() },
                modifier = Modifier.fillMaxWidth(),
            )
            if (question.promptSwatches.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    question.promptSwatches.forEach { rgb ->
                        Box(
                            Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(rgb.toComposeColor())
                        )
                    }
                }
            }
            Text(question.prompt.resolve(), style = MaterialTheme.typography.titleMedium)

            question.options.forEachIndexed { i, option ->
                val isCorrect = i == question.correctIndex
                val isSelected = i == selected
                val border = when {
                    revealed && isCorrect -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    isSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.error)
                    else -> null
                }
                Card(
                    onClick = { if (!revealed) onSelect(i) },
                    border = border,
                    modifier = Modifier.testTag("quiz.option"),   // v13 §0a: runtime-generated options
                    colors = CardDefaults.cardColors(
                        containerColor = if (revealed && isCorrect) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    ),
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        option.swatches.forEach { rgb ->
                            Box(Modifier.size(28.dp).clip(CircleShape).background(rgb.toComposeColor()))
                        }
                        Text(option.text.resolve(), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        if (revealed && isCorrect) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        if (revealed && isSelected && !isCorrect) {
                            Icon(Icons.Filled.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            if (revealed) {
                Text(
                    question.explanation.resolve(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        if (index + 1 >= total) stringResource(R.string.quiz_see_results)
                        else stringResource(R.string.quiz_next_question)
                    )
                }
            }
        }
    }
}

@Composable
private fun HueMatchGame(viewModel: HueMatchViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    if (state.finished) {
        QuizSummary(
            score = state.totalScore,
            total = state.rounds.size * 100,
            message = hueGrade(state.totalScore / state.rounds.size),
            onRestart = viewModel::restart,
        )
        return
    }
    val round = state.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            stringResource(R.string.quiz_round_of, state.roundIndex + 1, state.rounds.size, state.totalScore),
            style = MaterialTheme.typography.titleMedium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            listOf(
                stringResource(R.string.quiz_target) to round.target,
                stringResource(R.string.quiz_your_mix) to round.guess.toRgb(),
            ).forEach { (label, rgb) ->
                Column(Modifier.weight(1f)) {
                    Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(96.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(rgb.toComposeColor())
                    )
                }
            }
        }
        LabeledSlider(
            stringResource(R.string.quiz_hue), "${round.guess.hue.roundToInt()}°", round.guess.hue,
            { viewModel.onGuessChange(round.guess.copy(hue = it)) }, 0f..360f,
            listOf(Color(0xFFFF0000), Color(0xFFFFFF00), Color(0xFF00FF00),
                Color(0xFF00FFFF), Color(0xFF0000FF), Color(0xFFFF00FF), Color(0xFFFF0000)),
        )
        LabeledSlider(
            stringResource(R.string.quiz_saturation), "${(round.guess.saturation * 100).roundToInt()}%", round.guess.saturation,
            { viewModel.onGuessChange(round.guess.copy(saturation = it)) }, 0f..1f,
            listOf(Color.Gray, Color.hsl(round.guess.hue, 1f, 0.5f)),
        )
        LabeledSlider(
            stringResource(R.string.quiz_lightness), "${(round.guess.lightness * 100).roundToInt()}%", round.guess.lightness,
            { viewModel.onGuessChange(round.guess.copy(lightness = it)) }, 0f..1f,
            listOf(Color.Black, Color.hsl(round.guess.hue, round.guess.saturation, 0.5f), Color.White),
        )
        if (round.result != null) {
            Text(
                stringResource(R.string.quiz_score_line, round.result, hueGrade(round.result)),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
        Button(
            onClick = { if (round.result == null) viewModel.onCheck() else viewModel.onNext() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                when {
                    round.result == null -> stringResource(R.string.quiz_check_match)
                    state.roundIndex + 1 >= state.rounds.size -> stringResource(R.string.quiz_see_results)
                    else -> stringResource(R.string.quiz_next_round)
                }
            )
        }
    }
}

@Composable
private fun LightnessGameScreen(viewModel: LightnessViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    if (state.finished) {
        QuizSummary(state.score, 100, stringResource(R.string.quiz_revisit), viewModel::restart)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            stringResource(R.string.quiz_round_of, state.index, 5, state.score),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(stringResource(R.string.lighter_prompt), style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            val revealed = state.lastResult != null
            val firstCorrect = LightnessGame.isCorrect(state.round, choseFirst = true)
            listOf(
                state.round.first to firstCorrect,
                state.round.second to !firstCorrect,
            ).forEachIndexed { i, (rgb, isLighter) ->
                Card(
                    onClick = { if (!revealed) viewModel.onAnswer(i == 0) },
                    modifier = Modifier.weight(1f),
                    border = if (revealed && isLighter) BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else null,
                ) {
                    Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(96.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(rgb.toComposeColor())
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            stringResource(if (i == 0) R.string.q_opt_color_a else R.string.q_opt_color_b),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        }
        state.lastResult?.let { correct ->
            Text(
                if (correct) stringResource(R.string.quiz_correct)
                else stringResource(R.string.quiz_wrong, twoDecimals(state.difficulty)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        // v8 §0c fix: the submit path is onNext only — the unreachable onAnswer branch is gone.
        Button(
            onClick = viewModel::onNext,
            enabled = state.lastResult != null,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (state.index >= 5) stringResource(R.string.quiz_see_results)
                else stringResource(R.string.quiz_next_round)
            )
        }
    }
}

@Composable
private fun QuizSummary(score: Int, total: Int, message: String, onRestart: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            Icons.Filled.EmojiEvents, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(56.dp),
        )
        Text("$score / $total", style = MaterialTheme.typography.displaySmall)
        Text(message, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        Button(onClick = onRestart) { Text(stringResource(R.string.quiz_play_again)) }
    }
}

private fun twoDecimals(v: Double): String {
    val scaled = (v * 1000).roundToInt()
    return "${scaled / 1000}.${(scaled % 1000).toString().padStart(3, '0')}"
}
