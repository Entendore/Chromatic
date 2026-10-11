package com.example.chromatic.wear

import androidx.wear.tiles.ColorBuilders.argb
import androidx.wear.tiles.DimensionBuilders.DpProp
import androidx.wear.tiles.DimensionBuilders.ExpandedDimensionProp
import androidx.wear.tiles.DimensionBuilders.SpProp
import androidx.wear.tiles.LayoutElementBuilders
import androidx.wear.tiles.ModifiersBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.example.chromatic.core.color.ColorOfDay
import com.example.chromatic.core.color.ColorNamer
import com.example.chromatic.core.color.PaletteFactory
import com.example.chromatic.core.color.Rgb
import com.example.chromatic.core.color.toArgbInt
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import java.time.Duration
import java.time.LocalDateTime

class PaletteTileService : TileService() {

    override fun onTileRequest(requestParams: RequestBuilders.TileRequest):
        ListenableFuture<TileBuilders.Tile> {

        val daily = ColorOfDay.today()
        val swatches = PaletteFactory.tonalScale(daily)
            .filter { it.level in SHOWCASE_LEVELS }
            .map { it.rgb }

        val column = LayoutElementBuilders.Column.Builder()
            .addContent(
                LayoutElementBuilders.Row.Builder()
                    .apply { swatches.forEach { addContent(swatch(it)) } }
                    .build()
            )
            .addContent(text("${ColorNamer.nameOf(daily)} · ${daily.hex}", ON_SURFACE, 13f))
            .addContent(text("Color of the day", ON_SURFACE_VARIANT, 10f))
            .build()

        val root = LayoutElementBuilders.Box.Builder()
            .setWidth(expand())
            .setHeight(expand())
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder()
                    .setBackground(
                        ModifiersBuilders.Background.Builder()
                            .setColor(argb(BACKGROUND))
                            .setCorner(corner(12f))
                            .build()
                    )
                    .setPadding(
                        ModifiersBuilders.Padding.Builder()
                            .setStart(dp(12f)).setEnd(dp(12f)).setTop(dp(10f)).setBottom(dp(10f))
                            .build()
                    )
                    .build()
            )
            .addContent(column)
            .build()

        // Freshness expires at the NEXT LOCAL MIDNIGHT (v6 §0a) — never shows yesterday.
        val now = LocalDateTime.now()
        val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
        return Futures.immediateFuture(
            TileBuilders.Tile.Builder()
                .setResourcesVersion(RESOURCES_VERSION)
                .setTileLayout(LayoutElementBuilders.Layout.Builder().setRoot(root).build())
                .setFreshnessIntervalMillis(Duration.between(now, nextMidnight).toMillis())
                .build()
        )
    }

    private fun swatch(rgb: Rgb) = LayoutElementBuilders.Box.Builder()
        .setWidth(dp(16f))
        .setHeight(dp(16f))
        .setModifiers(
            ModifiersBuilders.Modifiers.Builder()
                .setBackground(
                    ModifiersBuilders.Background.Builder()
                        .setColor(argb(rgb.toArgbInt()))
                        .setCorner(corner(4f))
                        .build()
                )
                .build()
        )
        .build()

    private fun text(value: String, color: Long, size: Float) =
        LayoutElementBuilders.Text.Builder()
            .setText(value)
            .setMaxLines(1)
            .setColor(argb(color))
            .setFontSize(SpProp.Builder().setSize(size).build())
            .build()

    private fun corner(radius: Float) =
        ModifiersBuilders.Corner.Builder().setRadius(dp(radius)).build()

    private fun dp(value: Float) = DpProp.Builder(value).build()
    private fun expand() = ExpandedDimensionProp.Builder().build()

    private companion object {
        val SHOWCASE_LEVELS = listOf(100, 300, 500, 700, 900)
        const val RESOURCES_VERSION = "1"
        const val BACKGROUND = 0xFF131318L
        const val ON_SURFACE = 0xFFE4E1E9L
        const val ON_SURFACE_VARIANT = 0xFF91909AL
    }
}
