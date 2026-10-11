package com.example.chromatic.wear

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.NoDataComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.TimeRange
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.example.chromatic.core.color.ColorOfDay
import com.example.chromatic.core.color.ColorNamer
import java.time.LocalDate
import java.time.ZoneId

/** "Color of the day" short-text complication, valid until the next local midnight (v7 §2.4). */
class ColorComplicationService : SuspendingComplicationDataSourceService() {

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val daily = ColorOfDay.today()
        val until = LocalDate.now().plusDays(1)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return ShortTextComplicationData.Builder(
            text = ComplicationText.of(daily.hex),
            contentDescription = ComplicationText.of("Color of the day: ${ColorNamer.nameOf(daily)}"),
        )
            .setValidTimeRange(TimeRange.Builder().setUntilEpochMs(until).build())
            .build()
    }

    /** Framework contract: data for the REQUESTED type, or NoData — never wrong-typed (v7 §0b). */
    override fun getPreviewData(type: ComplicationType): ComplicationData =
        if (type == ComplicationType.SHORT_TEXT) {
            ShortTextComplicationData.Builder(
                text = ComplicationText.of("#3F7FBF"),
                contentDescription = ComplicationText.of("Color of the day preview"),
            ).build()
        } else {
            NoDataComplicationData()
        }
}
