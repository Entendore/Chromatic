package com.example.chromatic.feature.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.chromatic.core.data.model.Lesson
import com.example.chromatic.core.data.model.Practice

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LessonDetailContent(
    lesson: Lesson,
    isCompleted: Boolean,
    onToggle: () -> Unit,
    onPractice: (Practice) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            lesson.category.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(lesson.title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            lesson.summary,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        lesson.sections.forEach { section ->
            Spacer(Modifier.height(20.dp))
            Text(
                section.heading,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(Modifier.height(6.dp))
            Text(section.body, style = MaterialTheme.typography.bodyMedium)
            section.bullets.forEach { bullet ->
                Row(Modifier.padding(top = 6.dp)) {
                    Text("•  ", style = MaterialTheme.typography.bodyMedium)
                    Text(bullet, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        if (lesson.keyTerms.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            Text(
                stringResource(R.string.key_terms),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() },
            )
            lesson.keyTerms.forEach { term ->
                ElevatedCard(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(term.term, style = MaterialTheme.typography.titleSmall)
                        Text(
                            term.definition,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        FilterChip(
            selected = isCompleted,
            onClick = onToggle,
            label = {
                Text(if (isCompleted) stringResource(R.string.completed) else stringResource(R.string.mark_complete))
            },
            leadingIcon = if (isCompleted) {
                { Icon(Icons.Filled.Check, contentDescription = null, Modifier.size(18.dp)) }
            } else null,
        )
        if (lesson.practices.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.apply_it),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() },
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp),
            ) {
                lesson.practices.forEach { practice ->
                    Button(onClick = { onPractice(practice) }) { Text(practice.label()) }
                }
            }
        }
    }
}
