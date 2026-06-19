package com.snapdoc.app.core.storage

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "snapdoc_prefs")

/**
 * Single source of truth for user preferences. Backed by Preferences DataStore.
 */
@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private object Keys {
        val onboardingComplete = booleanPreferencesKey("onboarding_complete")
        val autoOcr = booleanPreferencesKey("auto_ocr")
        val autoAiSuggestion = booleanPreferencesKey("auto_ai_suggestion")
        val defaultScanMode = stringPreferencesKey("default_scan_mode")
        val defaultPageSize = stringPreferencesKey("default_page_size")
        val interstitialSaveCount = intPreferencesKey("interstitial_save_count")
        val lastInterstitialAt = intPreferencesKey("last_interstitial_at_seconds")
        val interstitialShownCount = intPreferencesKey("interstitial_shown_count")
        val paywallPrompted = booleanPreferencesKey("paywall_prompted")
        val removeAdsPurchased = booleanPreferencesKey("remove_ads_purchased")
        val reviewPrompted = booleanPreferencesKey("review_prompted")
    }

    val onboardingComplete: Flow<Boolean> = context.dataStore.data.map { it[Keys.onboardingComplete] ?: false }
    val autoOcr: Flow<Boolean> = context.dataStore.data.map { it[Keys.autoOcr] ?: true }
    val autoAiSuggestion: Flow<Boolean> = context.dataStore.data.map { it[Keys.autoAiSuggestion] ?: false }
    val defaultScanMode: Flow<String> = context.dataStore.data.map { it[Keys.defaultScanMode] ?: "Auto" }
    val defaultPageSize: Flow<String> = context.dataStore.data.map { it[Keys.defaultPageSize] ?: "Auto" }
    val saveCount: Flow<Int> = context.dataStore.data.map { it[Keys.interstitialSaveCount] ?: 0 }
    val lastInterstitialAt: Flow<Int> = context.dataStore.data.map { it[Keys.lastInterstitialAt] ?: 0 }
    val interstitialShownCount: Flow<Int> = context.dataStore.data.map { it[Keys.interstitialShownCount] ?: 0 }
    val paywallPrompted: Flow<Boolean> = context.dataStore.data.map { it[Keys.paywallPrompted] ?: false }
    val removeAdsPurchased: Flow<Boolean> = context.dataStore.data.map { it[Keys.removeAdsPurchased] ?: false }
    val reviewPrompted: Flow<Boolean> = context.dataStore.data.map { it[Keys.reviewPrompted] ?: false }

    suspend fun markOnboardingComplete() = context.dataStore.edit { it[Keys.onboardingComplete] = true }
    suspend fun setAutoOcr(value: Boolean) = context.dataStore.edit { it[Keys.autoOcr] = value }
    suspend fun setAutoAiSuggestion(value: Boolean) = context.dataStore.edit { it[Keys.autoAiSuggestion] = value }
    suspend fun setDefaultScanMode(value: String) = context.dataStore.edit { it[Keys.defaultScanMode] = value }
    suspend fun setDefaultPageSize(value: String) = context.dataStore.edit { it[Keys.defaultPageSize] = value }
    suspend fun incrementSaveCount() = context.dataStore.edit {
        it[Keys.interstitialSaveCount] = (it[Keys.interstitialSaveCount] ?: 0) + 1
    }
    suspend fun setLastInterstitialAt(seconds: Int) = context.dataStore.edit {
        it[Keys.lastInterstitialAt] = seconds
    }
    suspend fun incrementInterstitialShownCount() = context.dataStore.edit {
        it[Keys.interstitialShownCount] = (it[Keys.interstitialShownCount] ?: 0) + 1
    }
    suspend fun setPaywallPrompted(value: Boolean) = context.dataStore.edit {
        it[Keys.paywallPrompted] = value
    }
    suspend fun setRemoveAdsPurchased(value: Boolean) = context.dataStore.edit {
        it[Keys.removeAdsPurchased] = value
    }
    suspend fun setReviewPrompted(value: Boolean) = context.dataStore.edit {
        it[Keys.reviewPrompted] = value
    }
}
