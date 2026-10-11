package com.example.chromatic.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.chromatic.core.color.ColorConverter
import com.example.chromatic.core.color.Hsv
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Hue = angle, saturation = radius. Reads color through [hsvProvider] IN THE DRAW
 * PHASE (v8): writing hue/saturation invalidates only this canvas, not composition.
 */
@Composable
fun ColorWheel(
    hsvProvider: () -> Hsv,
    onSelected: (hue: Float, saturation: Float) -> Unit,
    modifier: Modifier = Modifier,
    harmonyHues: List<Hsv> = emptyList(),
) {
    val outline = MaterialTheme.colorScheme.outlineVariant

    fun handleTouch(position: Offset, side: Float) {
        val center = Offset(side / 2f, side / 2f)
        val dx = position.x - center.x
        val dy = position.y - center.y
        val radius = min(sqrt(dx * dx + dy * dy), side / 2f)
        val hue = ColorConverter.normalizeHue(atan2(dy, dx) * 180f / PI.toFloat())
        onSelected(hue, (radius / (side / 2f)).coerceIn(0f, 1f))
    }

    Box(
        modifier
            .pointerInput(Unit) {
                detectTapGestures { handleTouch(it, size.width.toFloat()) }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    handleTouch(change.position, size.width.toFloat())
                }
            }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val hsv = hsvProvider()
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCircle(
                brush = Brush.sweepGradient(
                    listOf(
                        Color(0xFFFF0000), Color(0xFFFFFF00), Color(0xFF00FF00),
                        Color(0xFF00FFFF), Color(0xFF0000FF), Color(0xFFFF00FF),
                        Color(0xFFFF0000),
                    ),
                    center = center,
                ),
                radius = radius,
                center = center,
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color.Transparent),
                    center = center,
                    radius = radius,
                ),
                radius = radius,
                center = center,
            )
            drawCircle(color = outline, style = Stroke(1.dp.toPx()), radius = radius, center = center)

            harmonyHues.forEach { h ->
                val angle = h.hue * PI.toFloat() / 180f
                val direction = Offset(cos(angle), sin(angle))
                drawLine(
                    color = h.toRgb().toComposeColor(),
                    start = center + direction * (radius * 0.18f),
                    end = center + direction * radius,
                    strokeWidth = 2.dp.toPx(),
                )
            }

            val angle = hsv.hue * PI.toFloat() / 180f
            val knob = center + Offset(cos(angle), sin(angle)) * (radius * hsv.saturation)
            drawCircle(color = Color.White, radius = 14.dp.toPx(), center = knob)
            drawCircle(
                color = Color.Black.copy(alpha = 0.6f),
                radius = 14.dp.toPx(), center = knob, style = Stroke(2.dp.toPx()),
            )
            drawCircle(color = hsv.toRgb().toComposeColor(), radius = 10.dp.toPx(), center = knob)
        }
    }
}
