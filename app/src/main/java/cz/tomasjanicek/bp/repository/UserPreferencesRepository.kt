package cz.tomasjanicek.bp.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Rozšíření pro získání DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val IS_GUEST_MODE_KEY = booleanPreferencesKey("is_guest_mode")

    // Flow pro sledování, zda jsme v režimu hosta
    val isGuestMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_GUEST_MODE_KEY] ?: false
        }

    suspend fun setGuestMode(isGuest: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_GUEST_MODE_KEY] = isGuest
        }
    }
}