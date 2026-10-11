package com.example.chromatic.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import chromatic.designsystem.res.Res
import chromatic.designsystem.res.cvd_achromatopsia
import chromatic.designsystem.res.cvd_deuteranopia
import chromatic.designsystem.res.cvd_normal
import chromatic.designsystem.res.cvd_protanopia
import chromatic.designsystem.res.cvd_tritanopia
import chromatic.designsystem.res.export_compose
import chromatic.designsystem.res.export_css
import chromatic.designsystem.res.export_json
import chromatic.designsystem.res.export_xml
import com.example.chromatic.core.color.ColorVisionType
import com.example.chromatic.core.color.ExportFormat
import org.jetbrains.compose.resources.stringResource

@Composable
fun ColorVisionType.label(): String = stringResource(when (this) {
    ColorVisionType.NORMAL -> Res.string.cvd_normal
    ColorVisionType.PROTANOPIA -> Res.string.cvd_protanopia
    ColorVisionType.DEUTERANOPIA -> Res.string.cvd_deuteranopia
    ColorVisionType.TRITANOPIA -> Res.string.cvd_tritanopia
    ColorVisionType.ACHROMATOPSIA -> Res.string.cvd_achromatopsia
})

@Composable
fun ExportFormat.label(): String = stringResource(when (this) {
    ExportFormat.JSON -> Res.string.export_json
    ExportFormat.CSS -> Res.string.export_css
    ExportFormat.COMPOSE -> Res.string.export_compose
    ExportFormat.ANDROID_XML -> Res.string.export_xml
})
