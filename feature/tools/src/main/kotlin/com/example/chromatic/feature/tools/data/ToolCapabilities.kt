package com.example.chromatic.feature.tools.data

import android.content.Context
import android.net.Uri
import com.example.chromatic.core.color.ExtractedSwatch
import com.example.chromatic.core.color.PaletteExtractor
import com.example.chromatic.core.color.Rgb
import com.example.chromatic.core.designsystem.util.BitmapUtils
import com.example.chromatic.core.designsystem.util.PaletteImage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface PhotoPaletteExtractor {
    /** Dominant swatches, dominant first; empty when the image can't be decoded. */
    suspend fun extract(uri: Uri, clusterCount: Int = 6): List<ExtractedSwatch>
}

@Singleton
class DefaultPhotoPaletteExtractor @Inject constructor(
    @ApplicationContext private val appContext: Context,
) : PhotoPaletteExtractor {
    override suspend fun extract(uri: Uri, clusterCount: Int): List<ExtractedSwatch> {
        val bitmap = BitmapUtils.decodeScaled(appContext, uri, maxDimension = 200) ?: return emptyList()
        return withContext(Dispatchers.Default) {
            val pixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            PaletteExtractor.extract(
                pixels.map { Rgb((it shr 16) and 0xFF, (it shr 8) and 0xFF, it and 0xFF) }
                    .filterNot { it.red > 248 && it.green > 248 && it.blue > 248 },
                clusterCount = clusterCount,
            )
        }
    }
}

/** PNG share capability — application-scoped so ViewModels stay Context-free (v18 note). */
interface PaletteImageSharer {
    suspend fun share(name: String, colors: List<Rgb>): Uri?
}

@Singleton
class DefaultPaletteImageSharer @Inject constructor(
    @ApplicationContext private val appContext: Context,
) : PaletteImageSharer {
    override suspend fun share(name: String, colors: List<Rgb>): Uri? =
        PaletteImage.share(appContext, name, colors)
}
