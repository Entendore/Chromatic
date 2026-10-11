package com.example.chromatic.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chromatic.core.color.ColorVisionType
import com.example.chromatic.core.data.BuildInfo
import com.example.chromatic.core.data.crash.CrashReport
import com.example.chromatic.core.data.crash.CrashStore
import com.example.chromatic.core.data.repository.SettingsRepository
import com.example.chromatic.core.data.repository.ThemeMode
import com.example.chromatic.core.data.repository.UserSettings
import com.example.chromatic.core.data.time.AppClock
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    val buildInfo: BuildInfo,
    private val clock: AppClock,
) : ViewModel() {

    val settings: StateFlow<UserSettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserSettings())

    fun onThemeModeSelected(mode: ThemeMode) = viewModelScope.launch { repository.setThemeMode(mode) }
    fun onDynamicColorChanged(enabled: Boolean) = viewModelScope.launch { repository.setDynamicColor(enabled) }
    fun onCvdPreviewSelected(type: ColorVisionType?) = viewModelScope.launch { repository.setCvdPreview(type) }
    fun onResetTheme() = viewModelScope.launch { repository.resetTheme() }
    fun onCompleteOnboarding() = viewModelScope.launch { repository.completeOnboarding() }
}

@HiltViewModel
class CrashReportsViewModel @Inject constructor(
    private val store: CrashStore,
) : ViewModel() {

    private val _reports = MutableStateFlow(store.reports())
    val reports: StateFlow<List<CrashReport>> = _reports.asStateFlow()

    init { refresh() }

    /** Refresh on entry — a crash may have landed while reading a previous one (v18 §0b). */
    fun refresh() { _reports.value = store.reports() }

    fun clear() { store.clear(); _reports.value = emptyList() }

    fun exportText(): String? = _reports.value.joinToString("\n\n———\n\n") { report ->
        buildString {
            appendLine("${report.summary} @ ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(java.util.Date(report.occurredAtEpochMs))}")
            appendLine("app ${report.versionName} (${report.versionCode}) · Android ${report.androidRelease} · ${report.deviceModel}")
            append(report.stackTrace)
        }
    }.ifBlank { null }
}
