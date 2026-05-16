package com.snapdoc.app

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * Forwards WARN+ logs to Crashlytics, drops everything else.
 *
 * Never log network request/response bodies via Timber — they may contain
 * user document text. See `core/network/GeminiClient.kt` for the
 * redacting interceptor.
 */
class CrashlyticsTree : Timber.Tree() {

    override fun isLoggable(tag: String?, priority: Int): Boolean =
        priority >= Log.WARN

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val crashlytics = runCatching { FirebaseCrashlytics.getInstance() }.getOrNull() ?: return
        crashlytics.log("[$priority] ${tag ?: "Snapdoc"}: $message")
        if (t != null) crashlytics.recordException(t)
    }
}
