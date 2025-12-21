package cz.tomasjanicek.bp.ui.screens.settings

import android.content.Context
import android.content.SharedPreferences
import cz.tomasjanicek.bp.model.ExaminationNotificationTime
import cz.tomasjanicek.bp.model.MedicineNotificationTime
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
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_MEDICINE_NOTIF_TIME = "medicine_notif_time"
        private const val KEY_EXAM_NOTIF_TIME = "exam_notif_time"

        // NOVÉ: Klíč pro onboarding
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }

    // --- ONBOARDING (NOVÉ) ---
    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    // --- TÉMA APLIKACE ---
    private val _themeFlow = MutableStateFlow(getSavedTheme())
    val themeFlow: StateFlow<AppTheme> = _themeFlow.asStateFlow()

    fun setAppTheme(theme: AppTheme) {
        prefs.edit().putString(KEY_APP_THEME, theme.name).apply()
        _themeFlow.value = theme
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

    // --- SPRÁVA SEKCÍ ---
    private val _enabledSectionsFlow = MutableStateFlow(getEnabledSections())
    val enabledSectionsFlow: StateFlow<Set<AppSection>> = _enabledSectionsFlow.asStateFlow()

    fun setSectionEnabled(section: AppSection, enabled: Boolean) {
        prefs.edit().putBoolean("section_${section.name}", enabled).apply()
        _enabledSectionsFlow.value = getEnabledSections()
    }

    private fun getEnabledSections(): Set<AppSection> {
        return AppSection.values().filter { section ->
            if (!section.canBeDisabled) true
            else prefs.getBoolean("section_${section.name}", section.defaultEnabled)
        }.toSet()
    }

    // --- NOTIFIKACE ---

    // a) Hlavní vypínač
    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
        _notificationsEnabled.value = enabled
    }

    // b) Léky
    private val _medicineNotificationTime = MutableStateFlow(getSavedMedicineTime())
    val medicineNotificationTime: StateFlow<MedicineNotificationTime> = _medicineNotificationTime.asStateFlow()

    fun setMedicineNotificationTime(time: MedicineNotificationTime) {
        prefs.edit().putString(KEY_MEDICINE_NOTIF_TIME, time.name).apply()
        _medicineNotificationTime.value = time
    }

    private fun getSavedMedicineTime(): MedicineNotificationTime {
        val name = prefs.getString(KEY_MEDICINE_NOTIF_TIME, MedicineNotificationTime.AT_TIME.name)
        return try { MedicineNotificationTime.valueOf(name!!) } catch (e: Exception) { MedicineNotificationTime.AT_TIME }
    }

    // c) Prohlídky
    private val _examNotificationTime = MutableStateFlow(getSavedExamTime())
    val examNotificationTime: StateFlow<ExaminationNotificationTime> = _examNotificationTime.asStateFlow()

    fun setExamNotificationTime(time: ExaminationNotificationTime) {
        prefs.edit().putString(KEY_EXAM_NOTIF_TIME, time.name).apply()
        _examNotificationTime.value = time
    }

    private fun getSavedExamTime(): ExaminationNotificationTime {
        val name = prefs.getString(KEY_EXAM_NOTIF_TIME, ExaminationNotificationTime.DAY_BEFORE_9_AM.name)
        return try { ExaminationNotificationTime.valueOf(name!!) } catch (e: Exception) { ExaminationNotificationTime.DAY_BEFORE_9_AM }
    }
}