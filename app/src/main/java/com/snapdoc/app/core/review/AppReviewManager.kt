package com.snapdoc.app.core.review

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper around Google Play's In-App Review API.
 *
 * Play decides whether the review card actually appears (it is quota-limited
 * and won't show every time), and the result is intentionally opaque — we
 * cannot tell whether the user reviewed or what rating they gave. Per Play
 * policy we never pre-screen by sentiment or promise a specific star count;
 * we just surface the card at a good moment.
 */
@Singleton
class AppReviewManager @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val manager = ReviewManagerFactory.create(context)

    /** Requests and launches the in-app review flow. Failures are swallowed — never block the user. */
    suspend fun launch(activity: Activity) {
        runCatching {
            val info = manager.requestReviewFlow().await()
            manager.launchReviewFlow(activity, info).await()
        }.onFailure { Timber.w(it, "In-app review flow unavailable") }
    }
}
