package com.example.chromatic.core.color

object CvdSafePalette {

    data class Result(val colors: List<Rgb>, val minSimulatedDeltaE: Double, val adjustmentsMade: Int)

    fun makeDistinguishable(
        colors: List<Rgb>,
        type: ColorVisionType = ColorVisionType.DEUTERANOPIA,
        minDeltaE: Double = 0.10,
        maxSteps: Int = 80,
        stepDegrees: Float = 3f,
    ): Result {
        require(colors.size >= 2) { "Need at least two colors" }
        var current = colors
        var adjustments = 0
        var steps = 0

        fun worstPair(list: List<Rgb>): Triple<Int, Int, Double> {
            val sims = list.map { ColorVisionSimulator.simulate(it, type) }
            var a = 0; var b = 1; var min = Double.MAX_VALUE
            for (i in list.indices) for (j in i + 1 until list.size) {
                val d = deltaEOkLab(sims[i], sims[j])
                if (d < min) { min = d; a = i; b = j }
            }
            return Triple(a, b, min)
        }

        while (steps < maxSteps) {
            steps++
            val (a, b, distance) = worstPair(current)
            if (distance >= minDeltaE) break

            var improved = false
            for (index in listOf(a, b)) {
                val hsv = current[index].toHsv()
                val candidates = listOf(hsv.hue + stepDegrees, hsv.hue - stepDegrees)
                    .map { Hsv(ColorConverter.normalizeHue(it), hsv.saturation, hsv.value).toRgb() }
                val best = candidates
                    .map { candidate -> current.toMutableList().also { it[index] = candidate } }
                    .maxByOrNull { trial -> worstPair(trial).third } ?: continue
                if (worstPair(best).third > distance) {
                    current = best
                    adjustments++
                    improved = true
                    break
                }
            }
            if (!improved) break
        }
        return Result(current, worstPair(current).third, adjustments)
    }
}
