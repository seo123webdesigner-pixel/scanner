package com.snapdoc.app

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize
import com.snapdoc.app.core.ads.AdsManager
import com.snapdoc.app.core.ads.NativeAdManager
import com.snapdoc.app.core.billing.BillingManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import timber.log.Timber

@HiltAndroidApp
class SnapdocApplication : Application() {

    @Inject lateinit var ads: AdsManager
    @Inject lateinit var nativeAds: NativeAdManager
    @Inject lateinit var billing: BillingManager

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }
        // Must run before any other Firebase SDK use (GeminiClient's
        // Firebase.ai(...) call included) so App Check can attest requests.
        Firebase.initialize(context = this)
        Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance(),
        )
        ads.initialize()
        nativeAds.preload()
        billing.initialize()
    }
}
