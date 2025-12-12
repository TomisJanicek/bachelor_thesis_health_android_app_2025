package cz.tomasjanicek.bp.ui.elements.bottomBar

import androidx.lifecycle.ViewModel
import cz.tomasjanicek.bp.ui.screens.settings.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BottomBarViewModel @Inject constructor(
    settingsManager: SettingsManager
) : ViewModel() {
    val enabledSections = settingsManager.enabledSectionsFlow
}