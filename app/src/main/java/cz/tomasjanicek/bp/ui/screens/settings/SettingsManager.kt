package cz.tomasjanicek.bp.ui.screens.settings

import android.content.Context
import android.content.SharedPreferences
import cz.tomasjanicek.bp.ui.elements.bottomBar.AppSection
import cz.tomasjanicek.bp.ui.theme.AppTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("bp_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_GUEST = "is_guest_mode"
        private const val KEY_APP_THEME = "app_theme"
    }

    // --- TÉMA APLIKACE (Flow pro okamžitou reakci) ---
    private val _themeFlow = MutableStateFlow(getSavedTheme())
    val themeFlow: StateFlow<AppTheme> = _themeFlow.asStateFlow()

    fun setAppTheme(theme: AppTheme) {
        prefs.edit().putString(KEY_APP_THEME, theme.name).apply()
        _themeFlow.value = theme // Aktualizujeme Flow -> UI se překreslí
    }

    private fun getSavedTheme(): AppTheme {
        val themeName = prefs.getString(KEY_APP_THEME, AppTheme.SYSTEM.name)
        return try {
            AppTheme.valueOf(themeName ?: AppTheme.SYSTEM.name)
        } catch (e: Exception) {
            AppTheme.SYSTEM
        }
    }

    // --- REŽIM HOSTA ---
    fun setGuestMode(isGuest: Boolean) {
        prefs.edit().putBoolean(KEY_IS_GUEST, isGuest).apply()
    }

    fun isGuestMode(): Boolean {
        return prefs.getBoolean(KEY_IS_GUEST, false)
    }



    // --- SPRÁVA SEKCÍ (Flow pro okamžitou reakci v BottomBaru) ---

    // Flow, které vrací Set<AppSection> obsahující jen povolené sekce
    private val _enabledSectionsFlow = MutableStateFlow(getEnabledSections())
    val enabledSectionsFlow: StateFlow<Set<AppSection>> = _enabledSectionsFlow.asStateFlow()

    fun setSectionEnabled(section: AppSection, enabled: Boolean) {
        prefs.edit().putBoolean("section_${section.name}", enabled).apply()
        _enabledSectionsFlow.value = getEnabledSections() // Aktualizujeme Flow
    }

    private fun getEnabledSections(): Set<AppSection> {
        return AppSection.values().filter { section ->
            // Pokud nejde vypnout, je vždy true. Jinak čteme z prefs (default dle Enumu).
            if (!section.canBeDisabled) true
            else prefs.getBoolean("section_${section.name}", section.defaultEnabled)
        }.toSet()
    }
}