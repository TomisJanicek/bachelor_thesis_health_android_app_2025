package cz.tomasjanicek.bp.ui.screens.settings

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("bp_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_GUEST = "is_guest_mode"
    }

    // Uloží, že jsme host
    fun setGuestMode(isGuest: Boolean) {
        prefs.edit().putBoolean(KEY_IS_GUEST, isGuest).apply()
    }

    // Zjistí, jestli jsme host
    fun isGuestMode(): Boolean {
        return prefs.getBoolean(KEY_IS_GUEST, false)
    }
}