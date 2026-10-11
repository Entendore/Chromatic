package com.example.chromatic.core.designsystem.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.chromatic.core.color.Rgb
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Renders a shareable palette card entirely on the Android graphics stack. */
object PaletteImage {

    fun render(name: String, colors: List<Rgb>, width: Int = 1080): Bitmap {
        val pad = 28f
        val headerHeight = 170f
        val swatchHeight = 230f
        val hexRowHeight = 96f
        val height = (headerHeight + swatchHeight + hexRowHeight).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(0xFF131318.toInt())

        val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFE4E1E9.toInt()
            textSize = 56f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val hex = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF91909A.toInt()
            textSize = 32f
            typeface = Typeface.SANS_SERIF
        }

        canvas.drawText(name, pad, headerHeight - 46f, title)

        val swatchWidth = (width - pad * (colors.size + 1)) / colors.size.toFloat()
        colors.forEachIndexed { index, rgb ->
            val left = pad + index * (swatchWidth + pad)
            canvas.drawRoundRect(
                RectF(left, headerHeight, left + swatchWidth, headerHeight + swatchHeight),
                28f, 28f,
                Paint().apply { color = rgb.toArgbInt() },
            )
            canvas.drawText(rgb.hex, left + 4f, headerHeight + swatchHeight + 52f, hex)
        }
        return bitmap
    }

    /** Render + compress + URI. Bitmap lifetime ends here (v9 §6); failures → null. */
    suspend fun share(context: Context, name: String, colors: List<Rgb>): Uri? =
        withContext(Dispatchers.Default) {
            val bitmap = render(name, colors)
            try {
                val file = File(context.cacheDir, "palette.png")
                file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } catch (t: Throwable) {
                null
            } finally {
                bitmap.recycle()
            }
        }
}
