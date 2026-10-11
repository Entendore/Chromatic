package com.example.chromatic.feature.learn

import android.content.Context
import com.example.chromatic.core.data.model.KeyTerm
import com.example.chromatic.core.data.model.Lesson
import com.example.chromatic.core.data.model.Practice
import com.example.chromatic.core.data.model.Section
import com.example.chromatic.core.data.repository.LessonRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

@Singleton
class ResourceLessonRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : LessonRepository {

    // Built once, lazily, off the main thread — startup never pays for lesson bodies.
    private val catalog: List<Lesson> by lazy { buildCatalog() }

    override val lessons: Flow<List<Lesson>> = flow { emit(catalog) }.flowOn(Dispatchers.Default)
    override fun findById(id: Long): Flow<Lesson?> = lessons.map { list -> list.find { it.id == id } }

    private fun buildCatalog(): List<Lesson> = listOf(
        lesson(1, R.string.category_foundations, listOf(Practice.HARMONY, Practice.MIX)) {
            title(R.string.lesson_1_title)
            summary(R.string.lesson_1_summary)
            section(R.string.lesson_1_s1_heading, R.string.lesson_1_s1_body)
            section(R.string.lesson_1_s2_heading, R.string.lesson_1_s2_body)
            term(R.string.lesson_1_t1_term, R.string.lesson_1_t1_def)
            term(R.string.lesson_1_t2_term, R.string.lesson_1_t2_def)
        },
        lesson(2, R.string.category_foundations, listOf(Practice.HARMONY, Practice.PALETTE)) {
            title(R.string.lesson_2_title)
            summary(R.string.lesson_2_summary)
            section(R.string.lesson_2_s1_heading, R.string.lesson_2_s1_body)
            section(R.string.lesson_2_s2_heading, R.string.lesson_2_s2_body, bullets = listOf(
                R.string.lesson_2_s2_b1, R.string.lesson_2_s2_b2, R.string.lesson_2_s2_b3,
            ))
            term(R.string.lesson_2_t1_term, R.string.lesson_2_t1_def)
            term(R.string.lesson_2_t2_term, R.string.lesson_2_t2_def)
            term(R.string.lesson_2_t3_term, R.string.lesson_2_t3_def)
        },
        lesson(3, R.string.category_schemes, listOf(Practice.HARMONY)) {
            title(R.string.lesson_3_title)
            summary(R.string.lesson_3_summary)
            section(R.string.lesson_3_s1_heading, R.string.lesson_3_s1_body)
            section(R.string.lesson_3_s2_heading, R.string.lesson_3_s2_body)
        },
        lesson(4, R.string.category_perception, listOf(Practice.ILLUSION)) {
            title(R.string.lesson_4_title)
            summary(R.string.lesson_4_summary)
            section(R.string.lesson_4_s1_heading, R.string.lesson_4_s1_body)
            section(R.string.lesson_4_s2_heading, R.string.lesson_4_s2_body)
        },
        lesson(5, R.string.category_perception, practices = emptyList()) {
            title(R.string.lesson_5_title)
            summary(R.string.lesson_5_summary)
            section(R.string.lesson_5_s1_heading, R.string.lesson_5_s1_body)
            section(R.string.lesson_5_s2_heading, R.string.lesson_5_s2_body)
        },
        lesson(6, R.string.category_application, listOf(Practice.CONTRAST, Practice.GRID)) {
            title(R.string.lesson_6_title)
            summary(R.string.lesson_6_summary)
            section(R.string.lesson_6_s1_heading, R.string.lesson_6_s1_body)
            section(R.string.lesson_6_s2_heading, R.string.lesson_6_s2_body, bullets = listOf(
                R.string.lesson_6_s2_b1, R.string.lesson_6_s2_b2,
                R.string.lesson_6_s2_b3, R.string.lesson_6_s2_b4,
            ))
            term(R.string.lesson_6_t1_term, R.string.lesson_6_t1_def)
        },
        lesson(7, R.string.category_application, listOf(Practice.PALETTE, Practice.THEME)) {
            title(R.string.lesson_7_title)
            summary(R.string.lesson_7_summary)
            section(R.string.lesson_7_s1_heading, R.string.lesson_7_s1_body)
            section(R.string.lesson_7_s2_heading, R.string.lesson_7_s2_body, bullets = listOf(
                R.string.lesson_7_s2_b1, R.string.lesson_7_s2_b2, R.string.lesson_7_s2_b3,
            ))
        },
    )

    private fun lesson(
        id: Long,
        category: Int,
        practices: List<Practice>,
        block: LessonBuilder.() -> Unit,
    ): Lesson = LessonBuilder(context, id, context.getString(category), practices).apply(block).build()
}

private class LessonBuilder(
    private val context: Context,
    private val id: Long,
    private val category: String,
    private val practices: List<Practice>,
) {
    private var titleRes = 0
    private var summaryRes = 0
    private val sections = mutableListOf<Section>()
    private val terms = mutableListOf<KeyTerm>()

    fun title(res: Int) { titleRes = res }
    fun summary(res: Int) { summaryRes = res }
    fun section(heading: Int, body: Int, bullets: List<Int> = emptyList()) {
        sections += Section(
            context.getString(heading),
            context.getString(body),
            bullets.map(context::getString),
        )
    }
    fun term(term: Int, definition: Int) =
        terms += KeyTerm(context.getString(term), context.getString(definition))

    fun build() = Lesson(
        id = id,
        title = context.getString(titleRes),
        category = category,
        summary = context.getString(summaryRes),
        sections = sections,
        keyTerms = terms,
        practices = practices,
    )
}
