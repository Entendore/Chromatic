package com.example.chromatic.core.color

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.random.Random

/** A pure function of the date — phone widget and Wear tile render the same color. */
object ColorOfDay {
    fun forEpochDay(epochDay: Long): Rgb {
        val rng = Random(epochDay)
        return Hsl(
            hue = rng.nextFloat() * 360f,
            saturation = 0.55f + rng.nextFloat() * 0.40f,
            lightness = 0.50f,
        ).toRgb()
    }

    fun today(now: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())): Rgb =
        forEpochDay(now.toEpochDays().toLong())
}
