package com.example.chromatic.core.color

import kotlin.random.Random

data class ExtractedSwatch(val rgb: Rgb, val fraction: Double, val name: String)

/** K-means in OKLab — perceptual clustering won't split one perceived color. */
object PaletteExtractor {

    fun extract(
        pixels: List<Rgb>,
        clusterCount: Int = 6,
        iterations: Int = 12,
        seed: Long = 1L,
    ): List<ExtractedSwatch> {
        require(pixels.isNotEmpty()) { "No pixels to analyze" }
        val k = clusterCount.coerceIn(1, pixels.size)
        val points = pixels.map { it.toOkLab() }
        val rng = Random(seed)

        var centroids = List(k) { points[rng.nextInt(points.size)] }
        val assignments = IntArray(points.size)

        repeat(iterations) {
            points.forEachIndexed { i, p ->
                assignments[i] = centroids.indices.minBy { c -> squaredDistance(p, centroids[c]) }
            }
            val sums = Array(k) { DoubleArray(3) }
            val counts = IntArray(k)
            points.forEachIndexed { i, p ->
                val c = assignments[i]
                sums[c][0] += p.l; sums[c][1] += p.a; sums[c][2] += p.b
                counts[c]++
            }
            centroids = centroids.indices.map { c ->
                if (counts[c] == 0) points[rng.nextInt(points.size)]
                else OkLab(sums[c][0] / counts[c], sums[c][1] / counts[c], sums[c][2] / counts[c])
            }
        }

        val counts = IntArray(k)
        assignments.forEach { counts[it]++ }
        val total = points.size.toDouble()

        return centroids
            .mapIndexed { c, lab ->
                val rgb = lab.toRgb()
                ExtractedSwatch(rgb, counts[c] / total, ColorNamer.nameOf(rgb))
            }
            .filter { it.fraction > 0.0 }
            .sortedByDescending { it.fraction }
    }

    private fun squaredDistance(a: OkLab, b: OkLab): Double {
        val dl = a.l - b.l; val da = a.a - b.a; val db = a.b - b.b
        return dl * dl + da * da + db * db
    }
}
