package com.example.chromatic.core.designsystem.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/** Unwraps ContextWrapper chains — defined once (v17 §0b). */
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
