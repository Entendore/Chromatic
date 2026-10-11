package com.example.chromatic.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.chromatic.core.color.ColorVisionType
import com.example.chromatic.core.color.Rgb
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode { SYSTEM, LIGHT, DARK;

    fun resolve(systemDark: Boolean) = when (this) {
        SYSTEM -> systemDark; LIGHT -> false; DARK -> true
    }
}

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val cvdPreview: ColorVisionType? = null,
    val lastReviewPromptMs: Long? = null,
    val appliedThemeSeedHex: String? = null,
    val onboardingDone: Boolean = false,
)

interface SettingsRepository {
    val settings: Flow<UserSettings>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setDynamicColor(enabled: Boolean)
    suspend fun setCvdPreview(type: ColorVisionType?)
    suspend fun markReviewAsked(nowMs: Long)
    suspend fun applyTheme(seed: Rgb)
    suspend fun resetTheme()
    suspend fun completeOnboarding()
}

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    private val themeKey = stringPreferencesKey("theme_mode")
    private val dynamicKey = booleanPreferencesKey("dynamic_color")
    private val cvdKey = stringPreferencesKey("cvd_preview")
    private val reviewAskedKey = longPreferencesKey("review_asked_ms")
    private val appliedSeedKey = stringPreferencesKey("applied_theme_seed")
    private val onboardingKey = booleanPreferencesKey("onboarding_done")

    // v9 policy: no blanket catch — corruption is the factory's job; real bugs crash.
    override val settings: Flow<UserSettings> = dataStore.data.map { prefs ->
        UserSettings(
            themeMode = prefs[themeKey]
                ?.let { stored -> ThemeMode.entries.firstOrNull { it.name == stored } }
                ?: ThemeMode.SYSTEM,
            dynamicColor = prefs[dynamicKey] ?: true,
            cvdPreview = prefs[cvdKey]
                ?.let { stored -> ColorVisionType.entries.firstOrNull { it.name == stored } },
            lastReviewPromptMs = prefs[reviewAskedKey],
            appliedThemeSeedHex = prefs[appliedSeedKey],
            onboardingDone = prefs[onboardingKey] ?: false,
        )
    }

    override suspend fun setThemeMode(mode: ThemeMode) { dataStore.edit { it[themeKey] = mode.name } }
    override suspend fun setDynamicColor(enabled: Boolean) { dataStore.edit { it[dynamicKey] = enabled } }
    override suspend fun setCvdPreview(type: ColorVisionType?) {
        dataStore.edit { prefs -> type?.name?.let { prefs[cvdKey] = it } ?: prefs.remove(cvdKey) }
    }
    override suspend fun markReviewAsked(nowMs: Long) { dataStore.edit { it[reviewAskedKey] = nowMs } }
    override suspend fun applyTheme(seed: Rgb) { dataStore.edit { it[appliedSeedKey] = seed.hex } }
    override suspend fun resetTheme() { dataStore.edit { it.remove(appliedSeedKey) } }
    override suspend fun completeOnboarding() { dataStore.edit { it[onboardingKey] = true } }
}
