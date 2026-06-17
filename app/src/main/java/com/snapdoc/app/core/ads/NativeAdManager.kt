package com.snapdoc.app.core.ads

import android.content.Context
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.snapdoc.app.BuildConfig
import com.snapdoc.app.core.storage.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Loads a small pool of native ads for the Home list (spec §5.1: a native
 * card every 7th item). Each loaded [NativeAd] is bound to exactly one
 * on-screen slot at a time (see NativeAdCard), as AdMob policy requires.
 *
 * Skips loading entirely for "Remove Ads" owners.
 */
@Singleton
class NativeAdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: UserPreferences,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _ads = MutableStateFlow<List<NativeAd>>(emptyList())
    val ads: StateFlow<List<NativeAd>> = _ads.asStateFlow()

    fun preload(count: Int = MAX_ADS) {
        scope.launch {
            if (prefs.removeAdsPurchased.first()) return@launch
            if (_ads.value.isNotEmpty()) return@launch
            val loader = AdLoader.Builder(context, BuildConfig.ADMOB_NATIVE_ID)
                .forNativeAd { ad ->
                    _ads.value = _ads.value + ad
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Timber.w("Native ad load failed: %s", error.message)
                    }
                })
                .build()
            loader.loadAds(AdRequest.Builder().build(), count.coerceIn(1, MAX_ADS))
        }
    }

    /** Releases all loaded ads. Call when ownership changes to Remove Ads. */
    fun clear() {
        _ads.value.forEach { it.destroy() }
        _ads.value = emptyList()
    }

    companion object {
        private const val MAX_ADS = 5
    }
}
