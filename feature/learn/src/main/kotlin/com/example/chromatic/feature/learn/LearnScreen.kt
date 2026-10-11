package com.example.chromatic.feature.learn

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Gradient
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chromatic.core.data.model.Lesson
import com.example.chromatic.core.data.model.Practice
import com.example.chromatic.core.designsystem.util.findActivity
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun LearnScreen(
    onOpenQuiz: () -> Unit,
    onOpenVision: () -> Unit,
    onOpenGradient: () -> Unit,
    onOpenThemeStudio: () -> Unit,
    onOpenMixLab: () -> Unit,
    onOpenIllusionLab: () -> Unit,
    onOpenGrid: () -> Unit,
    onOpenSettings: () -> Unit,
    onPractice: (Practice) -> Unit,
    pendingLessonId: Long? = null,
    viewModel: LearnViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigator = rememberListDetailPaneScaffoldNavigator<Long>()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    BackHandler(enabled = navigator.canNavigateBack) {
        scope.launch { navigator.navigateBack() }
    }

    // Deep link (v17 §0a): navigate the pane; do NOT pop the entry — popping would
    // destroy the navigator and close the lesson. Back stack: [Learn, DeepLink entry].
    LaunchedEffect(pendingLessonId, uiState.lessons) {
        if (pendingLessonId != null && uiState.lessons.any { it.id == pendingLessonId }) {
            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, pendingLessonId)
        }
    }

    // In-app review (v16/v18): mark on launch; Play-less installs never ask (pure policy).
    LaunchedEffect(uiState.shouldRequestReview) {
        if (uiState.shouldRequestReview) {
            context.findActivity()?.let { activity ->
                val manager = ReviewManagerFactory.create(context)
                manager.requestReviewFlow().addOnSuccessListener { info ->
                    manager.launchReviewFlow(activity, info)
                        .addOnCompleteListener { viewModel.onReviewPromptShown() }
                }.addOnFailureListener { viewModel.onReviewPromptShown() }
            } ?: viewModel.onReviewPromptShown()
        }
    }

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                LearnListPane(
                    uiState = uiState,
                    onLessonClick = { id ->
                        scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, id) }
                    },
                    onOpenQuiz = onOpenQuiz, onOpenVision = onOpenVision, onOpenGradient = onOpenGradient,
                    onOpenThemeStudio = onOpenThemeStudio, onOpenMixLab = onOpenMixLab,
                    onOpenIllusionLab = onOpenIllusionLab, onOpenGrid = onOpenGrid,
                    onOpenSettings = onOpenSettings,
                )
            }
        },
        detailPane = {
            AnimatedPane {
                val lesson = navigator.currentDestination?.contentKey
                    ?.let { id -> uiState.lessons.firstOrNull { it.id == id } }
                if (lesson == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            stringResource(R.string.pick_a_lesson),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        if (navigator.scaffoldValue.detail == PaneAdaptedValue.SinglePane) {
                            IconButton(onClick = { scope.launch { navigator.navigateBack() } }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.back),
                                )
                            }
                        }
                        LessonDetailContent(
                            lesson = lesson,
                            isCompleted = lesson.id in uiState.completedIds,
                            onToggle = { viewModel.onToggleCompleted(lesson.id) },
                            onPractice = onPractice,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
        },
    )
}

@Composable
fun LearnListPane(
    uiState: LearnUiState,
    onLessonClick: (Long) -> Unit,
    onOpenQuiz: () -> Unit,
    onOpenVision: () -> Unit,
    onOpenGradient: () -> Unit,
    onOpenThemeStudio: () -> Unit,
    onOpenMixLab: () -> Unit,
    onOpenIllusionLab: () -> Unit,
    onOpenGrid: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "title", contentType = "header") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.home_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings")
                }
            }
        }
        item(key = "progress", contentType = "header") { StatsCard(uiState) }
        item(key = "toolbox", contentType = "header") {
            ToolboxCard(onOpenQuiz, onOpenVision, onOpenGradient, onOpenThemeStudio, onOpenMixLab, onOpenIllusionLab, onOpenGrid)
        }
        item(key = "lessons-label", contentType = "header") {
            Text(
                stringResource(R.string.lessons_header),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .semantics { heading() },
            )
        }
        items(uiState.lessons, key = { it.id }, contentType = { "lesson" }) { lesson ->
            LessonCard(lesson, lesson.id in uiState.completedIds) { onLessonClick(lesson.id) }
        }
    }
}

@Composable
private fun StatsCard(state: LearnUiState) {
    val completed = state.completedIds.count { id -> state.lessons.any { it.id == id } }
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.your_progress),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "${(state.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            LinearProgressIndicator(progress = { state.progress }, modifier = Modifier.fillMaxWidth())
            Text(
                pluralStringResource(
                    R.plurals.lessons_complete_summary, state.lessons.size, completed, state.lessons.size,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (state.streakDays > 0) {
                Text(
                    pluralStringResource(R.plurals.streak_days, state.streakDays, state.streakDays),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (state.achievements.any { it.unlockedAtEpochMs != null }) {
                HorizontalDivider()
                BadgeRow(state.achievements)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BadgeRow(achievements: List<com.example.chromatic.core.data.achievement.Achievement>) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        achievements.forEach { achievement ->
            val unlocked = achievement.unlockedAtEpochMs != null
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (unlocked) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (unlocked) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                Row(
                    Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        if (unlocked) Icons.Filled.EmojiEvents else Icons.Outlined.EmojiEvents,
                        contentDescription = achievement.title(),
                        modifier = Modifier.size(14.dp),
                    )
                    Text(achievement.title(), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun ToolboxCard(
    onQuiz: () -> Unit, onVision: () -> Unit, onGradient: () -> Unit,
    onThemeStudio: () -> Unit, onMixLab: () -> Unit, onIllusionLab: () -> Unit,
    onGrid: () -> Unit,
) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column {
            ToolboxItem(Icons.Filled.Quiz, stringResource(R.string.toolbox_challenges), stringResource(R.string.toolbox_challenges_desc), onQuiz)
            ToolboxItem(Icons.Filled.Palette, stringResource(R.string.toolbox_theme_studio), stringResource(R.string.toolbox_theme_studio_desc), onThemeStudio)
            ToolboxItem(Icons.Filled.Science, stringResource(R.string.toolbox_mix_lab), stringResource(R.string.toolbox_mix_lab_desc), onMixLab)
            ToolboxItem(Icons.Filled.Psychology, stringResource(R.string.toolbox_illusion_lab), stringResource(R.string.toolbox_illusion_lab_desc), onIllusionLab)
            ToolboxItem(Icons.Filled.Visibility, stringResource(R.string.toolbox_vision), stringResource(R.string.toolbox_vision_desc), onVision)
            ToolboxItem(Icons.Filled.Gradient, stringResource(R.string.toolbox_gradient), stringResource(R.string.toolbox_gradient_desc), onGradient)
            ToolboxItem(Icons.Filled.GridOn, stringResource(R.string.toolbox_contrast_grid), stringResource(R.string.toolbox_contrast_grid_desc), onGrid)
        }
    }
}

@Composable
private fun ToolboxItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, contentDescription = null) },
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
private fun LessonCard(lesson: Lesson, completed: Boolean, onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    lesson.category.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    lesson.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    lesson.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(12.dp))
            Icon(
                imageVector = if (completed) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = if (completed) stringResource(R.string.completed) else stringResource(R.string.mark_complete),
                tint = if (completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            )
        }
    }
}
