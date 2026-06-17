package com.snapdoc.app.core.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.snapdoc.app.BuildConfig
import com.snapdoc.app.core.storage.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import timber.log.Timber

/**
 * Single entry point for AdMob. Spec §5.1 frequencies:
 *   - Banner: always visible on Home (rendered via [BannerAd]).
 *   - Native: every 7th item (rendered inline).
 *   - Interstitial: every 3rd save, 60s cooldown.
 *   - Rewarded: one per AI summary in free tier.
 *
 * When the user owns the "Remove Ads" IAP, every method here short-circuits.
 */
@Singleton
class AdsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: UserPreferences,
) {

    private var interstitial: InterstitialAd? = null
    private var rewarded: RewardedAd? = null

    fun initialize() {
        MobileAds.initialize(context) {
            Timber.i("AdMob initialized. Using test IDs = ${BuildConfig.USING_TEST_AD_IDS}")
        }
        preloadInterstitial()
        preloadRewarded()
    }

    private fun preloadInterstitial() {
        InterstitialAd.load(
            context,
            BuildConfig.ADMOB_INTERSTITIAL_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) { interstitial = ad }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Timber.w("Interstitial load failed: %s", error.message)
                    interstitial = null
                }
            },
        )
    }

    private fun preloadRewarded() {
        RewardedAd.load(
            context,
            BuildConfig.ADMOB_REWARDED_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) { rewarded = ad }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Timber.w("Rewarded load failed: %s", error.message)
                    rewarded = null
                }
            },
        )
    }

    /**
     * Call after each document save. Shows interstitial every 3rd save with a
     * 60s cooldown. Both the save count and the last-shown timestamp are read
     * from DataStore, so the cadence persists across app restarts.
     *
     * The count is incremented in SaveDocumentViewModel before this is called,
     * so we only read it here (no double counting).
     */
    suspend fun maybeShowInterstitial(activity: Activity) {
        if (prefs.removeAdsPurchased.first()) return
        if (prefs.saveCount.first() % 3 != 0) return
        val nowSeconds = (System.currentTimeMillis() / 1000).toInt()
        if (nowSeconds - prefs.lastInterstitialAt.first() < COOLDOWN_SECONDS) return
        val ad = interstitial ?: return
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitial = null
                preloadInterstitial()
            }
        }
        prefs.setLastInterstitialAt(nowSeconds)
        ad.show(activity)
    }

    /** Shows a rewarded ad before the AI summary; result is true if the user earned the reward. */
    suspend fun showRewarded(activity: Activity): Boolean {
        if (prefs.removeAdsPurchased.first()) return true
        val ad = rewarded ?: run {
            Timber.w("Rewarded ad not ready; skipping gate")
            return true
        }
        var earned = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewarded = null
                preloadRewarded()
            }
        }
        ad.show(activity) { earned = true }
        return earned
    }

    companion object {
        private const val COOLDOWN_SECONDS = 60
    }
}
