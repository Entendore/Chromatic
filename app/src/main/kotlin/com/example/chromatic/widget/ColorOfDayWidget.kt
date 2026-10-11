package com.example.chromatic.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.clickable
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.chromatic.MainActivity
import com.example.chromatic.R
import com.example.chromatic.core.color.ColorOfDay
import com.example.chromatic.core.color.ColorNamer
import com.example.chromatic.core.color.PaletteFactory

class ColorOfDayWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Shared engine — identical to the Wear tile, zero storage (v5 §8).
        val base = ColorOfDay.today()
        val swatches = PaletteFactory.tonalScale(base)
            .filter { it.level in SHOWCASE_LEVELS }
            .map { it.rgb }
        val caption = context.getString(R.string.widget_caption)
        val name = ColorNamer.nameOf(base)

        val onSurface = Color(0xFFE4E1E9)
        val onSurfaceVariant = Color(0xFF91909A)

        provideContent {
            Column(
                GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF131318))
                    .padding(10.dp)
                    .clickable(actionStartActivity<MainActivity>()),
            ) {
                Row(GlanceModifier.fillMaxWidth().height(26.dp)) {
                    swatches.forEach { rgb ->
                        Box(
                            GlanceModifier
                                .defaultWeight()
                                .padding(horizontal = 2.dp)
                                .cornerRadius(6.dp)
                                .background(Color(rgb.toArgbInt()))
                        ) {}
                    }
                }
                Spacer(GlanceModifier.height(8.dp))
                Text(
                    text = "$name · ${base.hex}",
                    style = TextStyle(color = ColorProvider(onSurface), fontSize = 13.sp),
                )
                Text(
                    text = caption,
                    style = TextStyle(color = ColorProvider(onSurfaceVariant), fontSize = 10.sp),
                )
            }
        }
    }

    private companion object {
        val SHOWCASE_LEVELS = listOf(100, 300, 500, 700, 900)
    }
}

class ColorOfDayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ColorOfDayWidget()
}
