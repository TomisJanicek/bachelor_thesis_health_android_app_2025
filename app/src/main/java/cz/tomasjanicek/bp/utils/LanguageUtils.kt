package cz.tomasjanicek.bp.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LanguageUtils {

    private const val CZECH = "cs"
    private const val ENGLISH = "en"

    /**
     * Returns the current system language code (e.g., "cs" for Czech, "en" for English, etc.).
     */
    fun getCurrentLanguage(): String {
        return Locale.getDefault().language
    }

    /**
     * Checks if the current system language is Czech.
     */
    fun isLanguageCzech(): Boolean {
        return getCurrentLanguage() == CZECH
    }

    /**
     * Sets the language for the application dynamically.
     * @param context Application or Activity context.
     * @param languageCode The language code to set (e.g., "cs" for Czech, "en" for English).
     */
    fun setLanguage(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale) // Nastavení výchozího jazyka

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale) // Nastavení konkrétního regionu
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    /**
     * Returns a human-readable name for a language code.
     * @param languageCode The language code (e.g., "cs", "en").
     */
    fun getLanguageName(languageCode: String): String {
        return when (languageCode) {
            CZECH -> "Čeština"
            ENGLISH -> "English"
            else -> "Unknown"
        }
    }
}