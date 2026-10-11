package com.example.chromatic.core.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SavedPalette(
    val id: String,
    val name: String,
    val hexColors: List<String>,
    val createdAtEpochMs: Long,
)

/** Tools a lesson links to. Labels are resolved per-locale in :feature:learn (ADR #4). */
enum class Practice { HARMONY, CONTRAST, PALETTE, GRID, ILLUSION, MIX, THEME }

data class Lesson(
    val id: Long,
    val title: String,
    val category: String,
    val summary: String,
    val sections: List<Section>,
    val keyTerms: List<KeyTerm> = emptyList(),
    val practices: List<Practice> = emptyList(),
)

data class Section(val heading: String, val body: String, val bullets: List<String> = emptyList())
data class KeyTerm(val term: String, val definition: String)
