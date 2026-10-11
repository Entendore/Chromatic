package com.example.chromatic.core.designsystem.components

import android.graphics.RenderEffect
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ColorMatrix
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.asFrameworkColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import com.example.chromatic.core.color.ColorVisionSimulator
import com.example.chromatic.core.color.ColorVisionType

@Composable
actual fun Modifier.cvdPreview(type: ColorVisionType?): Modifier {
    if (type == null || type == ColorVisionType.NORMAL) return this
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return this
    val effect = remember(type) {
        RenderEffect.createColorFilterEffect(
            ColorFilter.colorMatrix(ColorMatrix(ColorVisionSimulator.srgbMatrix(type)!!))
                .asFrameworkColorFilter()
        ).asComposeRenderEffect()
    }
    return this.graphicsLayer { renderEffect = effect }
}
