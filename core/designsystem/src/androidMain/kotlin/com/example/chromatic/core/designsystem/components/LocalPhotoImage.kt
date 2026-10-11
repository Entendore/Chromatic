package com.example.chromatic.core.designsystem.components

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.example.chromatic.core.designsystem.util.BitmapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Local (content://) photo decode — Coil's network-fetching capability removed (v17 §3).
 * Hooks are unconditional; the null check lives INSIDE the producer.
 */
@Composable
fun rememberLocalPhoto(uri: Uri?, maxDimension: Int = 1024): ImageBitmap? {
    val context = LocalContext.current
    return produceState<ImageBitmap?>(initialValue = null, uri, maxDimension) {
        value = if (uri == null) null else withContext(Dispatchers.IO) {
            BitmapUtils.decodeScaled(context, uri, maxDimension)?.asImageBitmap()
        }
    }.value
}
