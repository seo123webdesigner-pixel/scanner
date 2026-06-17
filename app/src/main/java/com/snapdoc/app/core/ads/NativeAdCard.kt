package com.snapdoc.app.core.ads

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.snapdoc.app.R

/**
 * Renders a native ad. The ad assets MUST live inside a [NativeAdView] with
 * each asset registered (headlineView, mediaView, etc.) so AdMob can track
 * impressions/clicks — that's why this hosts an inflated View via AndroidView
 * rather than drawing the ad in Compose.
 */
@Composable
fun NativeAdCard(nativeAd: NativeAd, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            LayoutInflater.from(context).inflate(R.layout.native_ad_card, null) as NativeAdView
        },
        update = { adView -> bindNativeAd(adView, nativeAd) },
    )
}

private fun bindNativeAd(adView: NativeAdView, ad: NativeAd) {
    val headline = adView.findViewById<TextView>(R.id.ad_headline)
    val advertiser = adView.findViewById<TextView>(R.id.ad_advertiser)
    val body = adView.findViewById<TextView>(R.id.ad_body)
    val icon = adView.findViewById<ImageView>(R.id.ad_app_icon)
    val cta = adView.findViewById<Button>(R.id.ad_call_to_action)
    val media = adView.findViewById<MediaView>(R.id.ad_media)

    headline.text = ad.headline
    adView.headlineView = headline

    adView.advertiserView = advertiser
    val advertiserText = ad.advertiser
    if (advertiserText.isNullOrBlank()) {
        advertiser.visibility = View.GONE
    } else {
        advertiser.visibility = View.VISIBLE
        advertiser.text = advertiserText
    }

    adView.bodyView = body
    val bodyText = ad.body
    if (bodyText.isNullOrBlank()) {
        body.visibility = View.GONE
    } else {
        body.visibility = View.VISIBLE
        body.text = bodyText
    }

    adView.iconView = icon
    val iconImage = ad.icon
    if (iconImage == null) {
        icon.visibility = View.GONE
    } else {
        icon.visibility = View.VISIBLE
        icon.setImageDrawable(iconImage.drawable)
    }

    adView.callToActionView = cta
    val ctaText = ad.callToAction
    if (ctaText.isNullOrBlank()) {
        cta.visibility = View.GONE
    } else {
        cta.visibility = View.VISIBLE
        cta.text = ctaText
    }

    adView.mediaView = media
    ad.mediaContent?.let { media.mediaContent = it }

    adView.setNativeAd(ad)
}
