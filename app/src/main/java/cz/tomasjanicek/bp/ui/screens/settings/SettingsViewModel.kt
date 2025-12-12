package cz.tomasjanicek.bp.ui.screens.settings

import androidx.lifecycle.ViewModel
import cz.tomasjanicek.bp.ui.elements.bottomBar.AppSection
import cz.tomasjanicek.bp.ui.theme.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsManager: SettingsManager
) : ViewModel() {

    val currentTheme = settingsManager.themeFlow

    fun onThemeSelected(theme: AppTheme) {
        settingsManager.setAppTheme(theme)
    }

    val enabledSections = settingsManager.enabledSectionsFlow

    fun toggleSection(section: AppSection, isEnabled: Boolean) {
        settingsManager.setSectionEnabled(section, isEnabled)
    }
}