package com.example.chromatic.core.color

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

enum class ExportFormat(val fileExtension: String) { JSON("json"), CSS("css"), COMPOSE("kt"), ANDROID_XML("xml") }

object PaletteExporter {
    private val json = Json { prettyPrint = true }

    fun export(name: String, colors: List<Rgb>, format: ExportFormat): String = when (format) {
        ExportFormat.JSON -> json.encodeToString(mapOf("name" to name, "colors" to colors.map(Rgb::hex)))

        ExportFormat.CSS -> buildString {
            appendLine(":root {")
            colors.forEachIndexed { i, c -> appendLine("  --${kebab(name)}-${i + 1}: ${c.hex.lowercase()};") }
            append("}")
        }

        ExportFormat.COMPOSE -> buildString {
            appendLine("import androidx.compose.ui.graphics.Color")
            appendLine()
            appendLine("object ${pascal(name)}Colors {")
            colors.forEachIndexed { i, c ->
                appendLine("    val Color${i + 1} = Color(0x${c.hex.removePrefix("#")}FF)")
            }
            append("}")
        }

        ExportFormat.ANDROID_XML -> buildString {
            appendLine("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
            appendLine("<resources>")
            colors.forEachIndexed { i, c ->
                appendLine("    <color name=\"${snake(name)}_${i + 1}\">${c.hex.uppercase()}</color>")
            }
            append("</resources>")
        }
    }

    /** Modern CSS gradients declare their interpolation space. */
    fun cssLinearGradient(from: Rgb, to: Rgb, space: BlendSpace): String {
        val interpolation = when (space) {
            BlendSpace.SRGB -> "in srgb"
            BlendSpace.LINEAR_SRGB -> "in srgb-linear"
            BlendSpace.OKLAB -> "in oklab"
        }
        return "linear-gradient($interpolation, ${from.hex.lowercase()}, ${to.hex.lowercase()})"
    }

    internal fun kebab(value: String) = value.trim()
        .replace(Regex("([a-z])([A-Z])"), "$1-$2")
        .replace(Regex("[^A-Za-z0-9]+"), "-")
        .trim('-')
        .lowercase()
        .ifEmpty { "palette" }

    internal fun pascal(value: String) =
        kebab(value).split("-").joinToString("") { it.replaceFirstChar(Char::uppercaseChar) }

    internal fun snake(value: String) = kebab(value).replace('-', '_')
}
