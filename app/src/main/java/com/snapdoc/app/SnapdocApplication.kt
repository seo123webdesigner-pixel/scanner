package com.snapdoc.app

import android.app.Application
import com.snapdoc.app.core.ads.AdsManager
import com.snapdoc.app.core.billing.BillingManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import timber.log.Timber

@HiltAndroidApp
class SnapdocApplication : Application() {

    @Inject lateinit var ads: AdsManager
    @Inject lateinit var billing: BillingManager

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }
        ads.initialize()
        billing.initialize()
    }
}
