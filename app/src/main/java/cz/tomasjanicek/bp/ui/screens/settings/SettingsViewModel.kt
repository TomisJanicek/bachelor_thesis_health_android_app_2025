package cz.tomasjanicek.bp.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tomasjanicek.bp.database.DatabaseCleaner
import cz.tomasjanicek.bp.model.ExaminationNotificationTime
import cz.tomasjanicek.bp.model.MedicineNotificationTime
import cz.tomasjanicek.bp.services.notification.AlarmScheduler
import cz.tomasjanicek.bp.ui.elements.bottomBar.AppSection
import cz.tomasjanicek.bp.ui.theme.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    private val alarmScheduler: AlarmScheduler,
    private val databaseCleaner: DatabaseCleaner
) : ViewModel() {

    // --- TÉMA ---
    val currentTheme = settingsManager.themeFlow
    fun onThemeSelected(theme: AppTheme) {
        settingsManager.setAppTheme(theme)
    }

    // --- MODULY (SEKCE) ---
    val enabledSections = settingsManager.enabledSectionsFlow
    fun toggleSection(section: AppSection, isEnabled: Boolean) {
        settingsManager.setSectionEnabled(section, isEnabled)
    }

    // --- NOTIFIKACE ---
    val notificationsEnabled = settingsManager.notificationsEnabled
    val medicineNotifTime = settingsManager.medicineNotificationTime
    val examNotifTime = settingsManager.examNotificationTime

    fun toggleNotifications(enabled: Boolean) {
        settingsManager.setNotificationsEnabled(enabled)
    }

    fun setMedicineTime(time: MedicineNotificationTime) {
        settingsManager.setMedicineNotificationTime(time)
    }

    fun setExamTime(time: ExaminationNotificationTime) {
        settingsManager.setExamNotificationTime(time)
    }

    // --- TESTOVACÍ NOTIFIKACE ---
    fun sendTestNotification() {
        val triggerTime = System.currentTimeMillis() + 5000 // za 5 sekund
        alarmScheduler.scheduleNotification(
            id = 999999,
            dateTime = triggerTime,
            title = "Testovací notifikace",
            message = "Skvělé! Upozornění fungují správně. 🔔"
        )
    }
    // --- DATA  ---
    fun clearAllData() {
        viewModelScope.launch {
            databaseCleaner.clearAllData()
            // Zde by bylo dobré např. zobrazit Toast nebo Snackbar o úspěchu,
            // ale to se řeší v UI vrstvě (Screen).
        }
    }
}