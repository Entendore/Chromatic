package com.example.chromatic.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.chromatic.core.color.ColorVisionType

/** Whole-subtree vision preview. Approximate sRGB filter; no-op where unsupported. */
@Composable
expect fun Modifier.cvdPreview(type: ColorVisionType?): Modifier
